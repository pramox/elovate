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
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../../../services/users/auth.service";
import {UserService} from "../../../services/users/user.service";
import {AdminService} from "../../../services/users/admin.service";
import {ToastService} from "../../../services/shared/toast.service";
import {TranslateService} from "@ngx-translate/core";
import {UserProfileDto} from "../../../dtos/userProfileDto";
import {UserPagedRequestDto} from "../../../dtos/userPagedRequestDto";
import {UserGameRankingPagedDto} from "../../../dtos/userGameRankingPagedDto";

@Component({
  selector: 'app-user-view',
  templateUrl: './user-view.component.html',
  styleUrl: './user-view.component.scss'
})
export class UserViewComponent implements OnInit{

  user: UserProfileDto | null = null;
  userId = 0;
  image: string | null = null;
  clicked = false;
  rankingResponsePaged: UserGameRankingPagedDto = {} as UserGameRankingPagedDto;
  gamesPagedRequest: UserPagedRequestDto = {} as UserPagedRequestDto;
  currentPage = 0;

  constructor(private route: ActivatedRoute,
              private authService: AuthService,
              private userService: UserService,
              private adminService: AdminService,
              private toastService: ToastService,
              private router: Router,
              private translateService: TranslateService) {
  }

  ngOnInit(): void {
    // get user id from route
    this.userId = Number(this.route.snapshot.paramMap.get('id'));

    if (!this.userId) {
      this.toastService.addError("elovate.error.unknown-error");
    }

    this.gamesPagedRequest.page = 0;
    this.gamesPagedRequest.size = 5;
    this.gamesPagedRequest.userId = this.userId;

    if (this.userId) {
      this.userService.getProfileById(this.userId).subscribe({
        next: (data) => {
          this.user = data;
          if (this.user.self) {
            this.router.navigate(['/profile']);
          }
          this.fetchImagesForUsers();
        },
        error: (error) => {
          this.toastService.addError(error);
        }
      });
      this.getAllRankedGames();
    }
  }

  public onPageChange(page: number) {
    this.gamesPagedRequest.page= page - 1;
    this.currentPage = page;
    this.getAllRankedGames();
  }

  public getAllRankedGames() {
    this.userService.getRankedGames(this.gamesPagedRequest).subscribe({
      next: games => {
        this.rankingResponsePaged = games;
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }

  public block(): void {
    this.userService.blockUser(this.userId).subscribe({
      next: (data) => {
        console.log(data);
        this.user = data;
        this.toastService.addSuccess(this.translateService.instant("elovate.user-view.toast.blocked")
          + data.nickName + this.translateService.instant("elovate.user-view.toast.blocked-2") );
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }

  public unblock(): void {
    this.userService.unblockUser(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.toastService.addSuccess(this.translateService.instant("elovate.user-view.toast.unblocked")
          + data.nickName + this.translateService.instant("elovate.user-view.toast.unblocked-2"));
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }

  public add(): void {
    this.userService.addFriend(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        console.log(data)
        this.toastService.addSuccess(this.translateService.instant("elovate.user-view.toast.sent-request")
          +  data.nickName);
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }

  public remove(): void {
    this.userService.removeFriend(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.toastService.addSuccess(this.translateService.instant("elovate.user-view.toast.unfriend")
          +  data.nickName);
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }

  public confirm(): void {
    this.userService.confirm(this.userId).subscribe({
      next: (data) => {
        this.user = data;
        this.toastService.addSuccess(this.translateService.instant("elovate.user-view.toast.confirmed")
          +  data.nickName);
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }


  private fetchImagesForUsers() {
    this.getImageByUser(this.userId).then(image => this.image = image);
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

  redirectToGame(id: number) {
    this.router.navigate(['/game/' + id + '/details']);
  }
}
