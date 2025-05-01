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
import {GameApplicationService} from "../../../services/game-application/game-application.service";
import {ActivatedRoute, Router} from "@angular/router";
import {GameApplicationResponseDto} from "../../../dtos/GameApplication/gameApplicationResponseDto";
import {Genre, Genre2LabelMapping} from "../../../dtos/enums/genre";
import {GameApplicationStatus} from "../../../dtos/enums/gameApplicationStatus";
import {GameApplicationCommentRequestDto} from "../../../dtos/GameApplication/Comment/gameApplicationCommentRequestDto";
import {CommentResponseDto} from "../../../dtos/GameApplication/Comment/commentResponseDto";
import {CommentResponse} from "../../../dtos/GameApplication/Comment/commentResponse";
import {ToastService} from "../../../services/shared/toast.service";

@Component({
  selector: 'app-game-application-detail-view',
  templateUrl: './game-application-detail-view.component.html',
  styleUrls: ['./game-application-detail-view.component.scss']
})
export class GameApplicationDetailViewComponent implements OnInit {

  gameApplicationResponseDto: GameApplicationResponseDto = {} as GameApplicationResponseDto;

  selectedGenre: Genre | null;
  genre2LabelMapper = Genre2LabelMapping;

  gameApplicationStatus: string

  gameApplicationStatuses = Object.values(GameApplicationStatus);

  commentContent: '';

  loadedComments: Array<CommentResponse>


  constructor(
    private gameApplicationService: GameApplicationService,
    private route: ActivatedRoute,
    private toastService: ToastService,
    private router: Router
  ) {
  }

  ngOnInit(): void {
    this.getGameApplication();
  }

  getGameApplication(): void {
    const id = this.route.snapshot.paramMap.get('id');
    console.log(Number(id));
    this.gameApplicationService.getGameApplicationById(Number(id)).subscribe({
      next: (game) => {
        this.gameApplicationResponseDto = game;
        console.log(this.gameApplicationResponseDto)
        this.selectedGenre = this.gameApplicationResponseDto.genre;
        this.gameApplicationStatus = this.ensureStatus(this.gameApplicationResponseDto);
        this.loadComments(game.comments);
      }, error: (error) => {
        this.toastService.addError(error, undefined, '/game-application')
      }
    });

  }

  loadComments(comments: Array<CommentResponseDto>) {
    let newComments: Array<CommentResponse> = new Array<CommentResponse>();
    comments.forEach(comment => {
      newComments.push({
        content: comment.content,
        timestamp: comment.timestamp,
        nickName: comment.nickName
      })
    });
    this.loadedComments = newComments;
  }

  leaveComment(id: string) {
    console.log(id);
    if (id !== "cancelComment") {
      const ga_id = this.gameApplicationResponseDto.id;
      const gameApplicationCommentRequestDTO: GameApplicationCommentRequestDto = {
        commentContent: this.commentContent
      };
      this.gameApplicationService.commentOnGameApplication(ga_id, gameApplicationCommentRequestDTO).subscribe({
          next: (game) => {
            this.loadComments(game.comments);
          },
          error: (error) => {
            this.toastService.addError(error);
          }
        }
      );
    }
    this.commentContent = '';
  }

  goToGameDetails(): void {
    this.router.navigate(['/game/' + this.gameApplicationResponseDto.gameId + '/details']);
  }

  private ensureStatus(game: GameApplicationResponseDto): string {
    const status = this.gameApplicationStatuses
      .find(status => status === game.status);
    if(status !== undefined) {
      return status
    } else {
      throw new TypeError('Status was undefined')
    }
  }

  protected readonly Genre2LabelMapping = Genre2LabelMapping;
}
