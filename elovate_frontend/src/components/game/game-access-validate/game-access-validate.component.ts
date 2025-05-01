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
import {GameAccessDto} from "../../../dtos/Game/gameAccessDto";
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../services/users/auth.service";
import {GameDetailDto} from "../../../dtos/Game/gameDetailDto";
import {ToastService} from "../../../services/shared/toast.service";

@Component({
  selector: 'app-game-access-validate',
  templateUrl: './game-access-validate.component.html',
  styleUrl: './game-access-validate.component.scss'
})
export class GameAccessValidateComponent implements OnInit {

  gameAccess: GameAccessDto

  constructor(private gameService: GameService,
              private route: ActivatedRoute,
              private router: Router,
              private authService: AuthService,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    let gameId = this.route.snapshot.paramMap.get('id')
    let userId = this.authService.getLoggedInUserId()
    if (!gameId || !userId) {
      this.router.navigate(['home'])
    } else {
      this.gameAccess = {
        uuid: null,
        game: parseInt(gameId),
        gameName: '',
        user: userId,
        apiKey: null
      };
      this.gameService.getGenerateUuid(this.gameAccess).subscribe({
        next: data => {
          this.gameAccess = data;
        },
        error: err => {
          this.toastService.addError(err);
        }
      });
    }
  }

  copyToClipboard() {
    navigator.clipboard.writeText(this.gameAccess.uuid ? this.gameAccess.uuid : "")
      .then(_ => console.log('written to clipboard'));
  }
}
