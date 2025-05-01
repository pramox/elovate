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
import {CommonModule, NgOptimizedImage} from '@angular/common';
import {ActivatedRoute, Router} from "@angular/router";
import {LobbyService} from "../../services/matchmaking/lobby.service";
import {LobbyDto} from "../../dtos/Lobby/lobbyDto";
import {UserDto} from "../../dtos/userDto";
import {TranslateModule} from "@ngx-translate/core";
import {UserService} from "../../services/users/user.service";
import {ToastService} from "../../services/shared/toast.service";
import {interval, Subscription, switchMap} from "rxjs";

@Component({
  selector: 'app-lobby',
  standalone: true,
  imports: [CommonModule, NgOptimizedImage, TranslateModule],
  templateUrl: './lobby.component.html',
  styleUrl: './lobby.component.scss'
})
export class LobbyComponent implements OnInit {
  lobby: LobbyDto;
  images: { [id: string]: string } = {}
  averageRankingTeamA: number;
  averageRankingTeamB: number;
  private pollingSubscription: Subscription;
  private pollingInterval = 2000;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private lobbyService: LobbyService,
              private userService: UserService,
              private toastService: ToastService,
  ) {
  }

  ngOnInit() {
    this.getLobbyByUrl();
    this.startPolling();
  }

  getLobbyByUrl(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log(id);
    if (!id) {
      this.toastService.addError("Could not find Lobby");
      this.router.navigate(['']);
      return;
    }
    this.lobbyService.getLobbyById(id).subscribe({
      next: value => {
        console.log(value);
        this.lobby = value;
        this.averageRankingTeamA = this.calculateAverageRanking(this.lobby.teamA);
        this.averageRankingTeamB = this.calculateAverageRanking(this.lobby.teamB);
        this.fetchImagesForUsers(this.lobby);
      },
      error: err => {
        console.error(err);
        this.toastService.addError(err);
      }
    });
  }

  calculateAverageRanking(users: UserDto[]): number {
    if (users.length === 0) {
      return 0;
    }
    let totalAge = users.reduce((sum, user) => sum + user.rating, 0);
    return Number((totalAge / users.length).toFixed(2));
  }

  getRoundedRating(rating: number) {
    return Number(rating.toFixed(0));
  }

  private fetchImagesForUsers(lobby: LobbyDto) {
    lobby.teamA.forEach(user => {
      this.getImageByUser(user.id).then(image => this.images[user.id] = image);
    });
    lobby.teamB.forEach(user => {
      this.getImageByUser(user.id).then(image => this.images[user.id] = image);
    });
  }

  private async getImageByUser(id: number): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      this.userService.getImageFromUser(id).subscribe({
        next: value => {
          resolve(value.base64String);
        },
        error: err => {
          reject(err);
        }
      });
    });
  }

  private startPolling(): void {
    console.log("Starting Polling");
    this.pollingSubscription = interval(this.pollingInterval)
      .pipe(
        switchMap(() => this.lobbyService.getLobbyActivity(this.lobby.id)),
      )
      .subscribe(value => {
          if (!value) {
            console.log("Lobby has ended", value);
            this.pollingSubscription.unsubscribe()
            this.router.navigate(["post-game", this.lobby.id])
          }
        }
      );
  }
}
