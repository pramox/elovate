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
import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FinishedGameDto, GameEndResult} from "../../dtos/finishedGameDto";
import {StatisticsService} from "../../services/statistics/statistics.service";
import {UserService} from "../../services/users/user.service";
import {AuthService} from "../../services/users/auth.service";
import {GameService} from "../../services/game/game.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ToastService} from "../../services/shared/toast.service";
import {TranslateModule} from "@ngx-translate/core";
import {LobbyService} from "../../services/matchmaking/lobby.service";

@Component({
  selector: 'app-post-game-view',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './post-game-view.component.html',
  styleUrl: './post-game-view.component.scss'
})

export class PostGameViewComponent {

  finishedGame: FinishedGameDto;
  userId: 1;
  lobbyId: string | null;

  constructor(private lobbyService: LobbyService,
              private router: Router,
              private activatedRoute: ActivatedRoute,
              private toaster: ToastService,
              public authService: AuthService) {
  }

  ngOnInit(): void {
    this.activatedRoute.paramMap.subscribe(params => {
      this.lobbyId = params.get('id');
    });
    if (!this.lobbyId) {
      this.router.navigate(["home"])
      this.toaster.addError({errorKey: "LOBBY_NOT_FOUND", message: "Lobby id is incorrect or could not be found"}, undefined, "/home")
    } else {
      this.lobbyService.getPostGameStats(this.lobbyId).subscribe(
        data => {
          this.finishedGame = data
          console.log(this.finishedGame)
        },
        error => {
          this.toaster.addError(error, undefined, "/home")
        }
      );
    }
  }

  teamAIsWinner() {
    return this.finishedGame.gameEndResult.toString() === GameEndResult[GameEndResult.TEAM_A_WINNER]
  }

  navigateToHome() {
    this.router.navigate(["/home"]);
  }

  getColumnNames() {
    return this.finishedGame.teamA[0].customStats.map(e => e.statName);
  }

  protected readonly Math = Math;
  protected readonly GameEndResult = GameEndResult;
}
