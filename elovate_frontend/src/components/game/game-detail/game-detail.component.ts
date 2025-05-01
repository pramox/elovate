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
import {Component, OnInit} from '@angular/core';
import {GameService} from "../../../services/game/game.service";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../services/users/auth.service";
import {Genre2LabelMapping} from "../../../dtos/enums/genre";
import {GameDetailDto} from "../../../dtos/Game/gameDetailDto";
import {ToastService} from "../../../services/shared/toast.service";

@Component({
  selector: 'app-game-detail',
  templateUrl: './game-detail.component.html',
  styleUrls: ['./game-detail.component.scss']
})
export class GameDetailComponent implements OnInit {
  gameDetailDTO: GameDetailDto = {} as GameDetailDto;
  genre2LabelMapper = Genre2LabelMapping;


  languages = {java: true, python: false, cpp: false}

  constructor(private gameService: GameService,
              private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    this.getGameById();
  }

  getGameById(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.gameService.getGameById(Number(id)).subscribe({
      next: game => {
        this.gameDetailDTO = game;
        console.log(this.gameDetailDTO)
      },
      error: err => {
        this.toastService.addError(err, undefined, '/home');
      }
    });
  }

  isDeveloper(): boolean {
    if (this.gameDetailDTO?.developerDetails?.id) {
      return this.authService.userIdMatchesJWT(this.gameDetailDTO.developerDetails.id);
    }
    return false;
  }

  setLanguage(lang: any) {
    for (const language in this.languages) {
      if (language in this.languages) {
        this.languages[language as keyof typeof this.languages] = false;
      }
    }
    if (lang in this.languages) {
      this.languages[lang as keyof typeof this.languages] = true;
    }
  }

  isAdmin() {
    return this.authService.isAdmin();
  }

  redirectToParams() {
    this.router.navigate(["game/" + this.gameDetailDTO.id + "/parameters"])
  }
}
