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
import {GameDetailDto} from "../../../dtos/Game/gameDetailDto";
import {ToastService} from "../../../services/shared/toast.service";


@Component({
  selector: 'app-game-access',
  templateUrl: './game-access.component.html',
  styleUrl: './game-access.component.scss'
})
export class GameAccessComponent implements OnInit {

  games: GameDetailDto[] = [];

  constructor(private gameService: GameService,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    this.getAllGames();
  }

  private getAllGames() {
    console.log(" games")
    this.gameService.getAllGames(false).subscribe({
      next: data => {
        this.games = data
      },
      error: err => {
        this.toastService.addError(err);
      }
    });
  }

}
