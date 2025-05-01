/*-
 * #%L
 * ELOvate
 * %%
 * Copyright (C) 2024 ELOvate GmbH.
 * %%
 * Copyright (C) 2024 ELOvate GmbH. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * #L%
 */
import {CanActivate, Router} from "@angular/router";
import {AuthService} from "../users/auth.service";
import {Injectable} from "@angular/core";
import {map, Observable} from "rxjs";
import {TranslateService} from "@ngx-translate/core";
import {HttpClient} from "@angular/common/http";

export interface AvailableLanguages {
    selected: string;
    supported: string[];
}

@Injectable({
providedIn: 'root'
})
export class LanguageService implements CanActivate {


    private languageFilesDirectory = 'assets/i18n';
    availableLanguages: AvailableLanguages = {
        selected: 'en',
        supported: []
    };
    private audio: HTMLAudioElement;
    constructor(private translateService: TranslateService,
                private http: HttpClient
    ) {
        this.translateService.setDefaultLang('en');
        this.setLanguage();
        this.audio = new Audio('assets/music/cyka_blyat.mp3');
    }

    canActivate() {
        this.scanLanguageFiles();
        return true;
    }

    private setLanguage() {
        let localLang: string | null = localStorage.getItem('lang');
        if(localLang === null) {
            localLang = 'en';
            localStorage.setItem('lang', localLang);
        }
        this.availableLanguages.selected = localLang.toString();
        if (localLang.toString() === 'ru') {
          if (LanguageService.getRandomNumber(40, 45) === 42) {
            this.audio.play();
          }
        }
        this.translateService.use(localLang);
    }

    private scanLanguageFiles() {
        this.getLanguageFiles()
            .subscribe(
                languages => {
                    this.availableLanguages.supported = languages;
                    console.log('Supported Languages:', this.availableLanguages);
                },
                error => {
                    console.error('Error loading supported languages:', error);
                }
            );
    }

    private getLanguageFiles(): Observable<string[]> {
        return this.http.get(`${this.languageFilesDirectory}/i18n-config.json`).pipe(
            // Extract JSON filenames from the configuration file
            map((languagesConfig: any) => languagesConfig.supportedLanguages)
        );
    }

    public getAvailableLanguages(): AvailableLanguages {
        return this.availableLanguages;
    }

    public updateLanguage(newLang: string) {
        if (!this.availableLanguages.supported.includes(newLang)) {
            return;
        }
        this.audio.pause();
        localStorage.setItem('lang', newLang);
        this.setLanguage();
    }

    private static getRandomNumber(min: number, max: number): number {
        return Math.floor(Math.random() * (max - min + 1) + min);
    }

}
