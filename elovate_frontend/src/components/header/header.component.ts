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
import {Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {Base64StringDto} from "../../dtos/base64StringDto";
import {UserService} from "../../services/users/user.service";
import {DialogService} from "../../services/shared/dialog.service";
import {ToastService} from "../../services/shared/toast.service";
import {AuthService} from "../../services/users/auth.service";
import {LobbyDto} from "../../dtos/Lobby/lobbyDto";
import {interval, Subscription, switchMap, takeWhile} from "rxjs";
import {LobbyService} from "../../services/matchmaking/lobby.service";
import {AvailableLanguages, LanguageService} from "../../services/i18n/LanguageService";

interface Option {
  name: string;
  routing: string;
}

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
  providers: [DialogService]
})
export class HeaderComponent implements OnInit, OnDestroy {
  availableLanguages: AvailableLanguages;
  selectedLanguage: string;
  userId: number;
  private pollingSubscription: Subscription;
  private pollingInterval = 10000; // 10 seconds
  activeLobby: LobbyDto | undefined;
  imageBase64: string = '';
  sidebarVisible: boolean = false;
  langButton: boolean = false; // true -> en, false -> de


  constructor(public authService: AuthService,
              private router: Router,
              private toastService: ToastService,
              private lobbyService: LobbyService,
              private userService: UserService,
              private translateService: TranslateService,
              private dialogService: DialogService,
              protected languageService: LanguageService) {
  }

  ngOnInit(): void {
    this.authService.isLoggedInSubject.subscribe((loggedIn: boolean) => {
      if (loggedIn) {
        let userId = this.authService.getLoggedInUserId();
        if (userId) {
          this.userId = userId;
          setTimeout(() => {
          }, 5000);
          this.lobbyService.getLobbyByUserId(this.userId).subscribe(value => {
            if (value.length > 0) {
              console.log("Found Lobby", value);
              this.activeLobby = value[0];
              this.stopPolling();
            } else {
              this.startPolling();
            }
          });
        } else {
          this.toastService.addError("elovate.error.unknown-error")
        }
      }
    });
    if (this.authService.isLoggedIn()) {
      this.setProfilePicture();
    }
    this.getLanguages();
  }

  ngOnDestroy() {
    this.stopPolling();
  }

  onSelectChange(event: string): void {
    this.languageService.updateLanguage(event);
  }

  async clickLogout() {
    const bool = await this.dialogService.addDialog(
      this.translateService.instant('eloveate.header.signout-dialog.message'),
      this.translateService.instant('eloveate.header.signout-dialog.header')
    );
    if (bool) {
      this.authService.logout();
      this.toastService.addSuccess(this.translateService.instant('eloveate.header.signout-dialog.toast'));
      this.router.navigate(["home"]);
      this.sidebarVisible = false;
    }
  }

  setProfilePicture(): void {
    const userId: any = this.authService.getLoggedInUserId();
    this.userService.getImageFromUser(userId).subscribe({
      next: (data: Base64StringDto) => {
        this.imageBase64 = data.base64String;
      },
      error: (error: any) => {
        console.error('Could not load profile picture into sidebar. Maybe user has not set one yet.');
      }
    })
  }

  navigateToPage(url: string): void {
    this.router.navigate([url]);
    this.sidebarVisible = false;
  }

  navigateToLobby() {
    if (!this.activeLobby) {
      this.toastService.addError("LOBBY_NOT_FOUND");
      return;
    }
    const navigationUrl = 'lobby/' + this.activeLobby.id;
    console.log(navigationUrl)
    this.router.navigate([navigationUrl]);
  }

  private startPolling(): void {
    console.log("Starting Polling");
    this.pollingSubscription = interval(this.pollingInterval)
      .pipe(
        switchMap(() => this.lobbyService.getLobbyByUserId(this.userId)),
        takeWhile(value => value.length === 0)
      )
      .subscribe(value => {
          if (value.length > 0) {
            console.log("Found Lobby", value);
            this.activeLobby = value[0];
          }
          console.log(value)
        }
      );
  }

  private stopPolling(): void {
    console.log("Stopping polling");
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
  }

  protected readonly self = self;

  private getLanguages() {
    this.availableLanguages = this.languageService.getAvailableLanguages();
  }
}
