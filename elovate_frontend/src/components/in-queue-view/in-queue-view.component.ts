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
import {Component, inject, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {QueueService} from "../../services/matchmaking/queue.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {HighlightJsDirective} from "ngx-highlight-js";
import {GameDetailDto} from "../../dtos/Game/gameDetailDto";
import {GameService} from "../../services/game/game.service";
import {ToastService} from "../../services/shared/toast.service";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {from, Subject, takeUntil} from "rxjs";
import {ProgressSpinnerModule} from "primeng/progressspinner";

@Component({
  selector: 'app-in-queue-view',
  standalone: true,
  imports: [CommonModule, FormsModule, HighlightJsDirective, TranslateModule, ProgressSpinnerModule],
  templateUrl: './in-queue-view.component.html',
  styleUrl: './in-queue-view.component.scss'
})
export class InQueueViewComponent implements OnInit, OnDestroy {

  private destroy$ = new Subject<void>();

  queueService = inject(QueueService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  gameService = inject(GameService);
  toastService = inject(ToastService);
  translateService = inject(TranslateService);

  playersInQueue: number | null = null;
  gameDetailDto: GameDetailDto;

  startTime: number;
  timeInQueue: string;
  timerInterval: any;

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const gameId = idParam ? parseInt(idParam) : null;

    if (!gameId) {
      console.log("ID is not number!")
      return;
    }

    this.gameService.getGameById(Number(gameId))
      .subscribe({
        next: game => {
          this.gameDetailDto = game;
          console.log(this.gameDetailDto)
        },
        error: err => {
          this.toastService.addError(err, undefined, '/home');
        }
      });

    this.runQueue(gameId);

    this.startTimer();
  }

  ngOnDestroy(): void {
    from(this.queueService.disconnect()).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      console.log("DISCONNECTED FROM QUEUE");
      // Additional cleanup if needed
    });

    // Signal that the component is destroyed
    this.destroy$.next();
    this.destroy$.complete();

    // Clear interval if it exists
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  private startTimer(): void {
    this.startTime = Date.now();
    this.timerInterval = setInterval(() => {
      const elapsed = Date.now() - this.startTime;
      this.timeInQueue = this.formatTime(elapsed);
    }, 1000);
  }

  private formatTime(milliseconds: number): string {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;

    return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  }

  runQueue(gameId: number) {
    this.queueService.disconnect().then(() => {
      this.queueService.connect().then(() => {

        this.queueService.sendQueueJoinMessageForGame(gameId).subscribe({
          error: (err) => {
            this.toastService.addError(err)
          },
          complete: () => {
            this.toastService.addSuccess(this.translateService.instant("elovate.in-queue.successful"))
          }
        });

        // Subscribe to public messages
        this.queueService.getPublicMessagesAsObservable().subscribe({
          next: (publicMessage) => {
            console.log('Received public message:', publicMessage);

            if (this.queueService.isQueueingAmountMessage(publicMessage)) {
              const queueingPlayers = this.queueService.getQueueingAmount(publicMessage);
              this.playersInQueue = queueingPlayers;
            }

          },
          error: (err) => {
            this.toastService.addError(err)
          }
        });

        // Subscribe to private messages
        this.queueService.getPrivateMessagesAsObservable().subscribe({
          next: (privateMessage) => {
            console.log('Received private message:', privateMessage);

            if (this.queueService.isMatchStartingMessage(privateMessage)) {
              const matchNumber = this.queueService.getMatchLobbyId(privateMessage)
              this.redirectToLobby(matchNumber);
            } else if (this.queueService.isQueueingAmountMessage(privateMessage)) {
              const queueingPlayers = this.queueService.getQueueingAmount(privateMessage);
              this.playersInQueue = queueingPlayers;
            }

          },
          error: (err) => {
            this.toastService.addError(err)
          }
        });

      }).catch(error => {
        this.toastService.addError(error)
      });
    })
  }

  private redirectToLobby(lobbyId: string) {
    this.router.navigate(["lobby", lobbyId])
  }
}
