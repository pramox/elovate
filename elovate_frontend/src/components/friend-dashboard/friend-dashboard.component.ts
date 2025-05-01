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
import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {UserService} from "../../services/users/user.service";
import {UserResponseDto} from "../../dtos/userResponseDto";
import {UserResponsePagedDto} from "../../dtos/userResponsePagedDto";
import {PagedRequestDto} from "../../dtos/pagedRequestDto";
import {Router} from "@angular/router";

@Component({
  selector: 'app-friend-dashboard',
  templateUrl: './friend-dashboard.component.html',
  styleUrl: './friend-dashboard.component.scss'
})
export class FriendDashboardComponent implements OnInit{
  @ViewChild('input') searchInput: ElementRef;


  users: UserResponseDto[] = [];
  requests: UserResponseDto[] = [];
  usersPaged: UserResponsePagedDto = {} as UserResponsePagedDto;
  usersPagedRequest: PagedRequestDto = {} as PagedRequestDto;
  images: { [id: string]: string } = {}
  showFriends = false;
  showBlocked = false;
  showFriendRequests = false;
  addFriends = false;
  currentPage = 1;
  maxPage = 1;
  isNicknameSearch = false;
  private timeout?: number;



  constructor(private userService: UserService, private router: Router) {
  }

  ngOnInit(): void {
    this.usersPagedRequest.page = 0;
    this.usersPagedRequest.size = 10;
    this.showFriends = true;
    this.fetchAllFriends(false);
  }

  public fetchAllFriends(reset: boolean) {
    if (reset) {
      this.reset();
    }
    this.showFriends = true;
    this.userService.getAllFriends(this.usersPagedRequest).subscribe({
      next: users => {
        this.usersPaged = users;
        this.fetchImagesForUsers();
      }
    });
  }

  public fetchAllFriendRequests(reset: boolean) {
    if (reset) {
      this.reset();
    }
    this.showFriendRequests = true;
    this.userService.getFriendRequests(this.usersPagedRequest).subscribe({
      next: users => {
        this.usersPaged = users;
        this.fetchImagesForUsers();
      }
    });
  }

  public fetchAllBlocked(reset: boolean) {
    if (reset) {
      this.reset();
    }
    this.showBlocked = true;
    this.userService.getAllBlocked(this.usersPagedRequest).subscribe({
      next: users => {
        this.usersPaged = users;
        this.fetchImagesForUsers();
      }
    });
  }

  public findFriends(reset: boolean) {
    if (reset) {
      this.reset();
    }
    this.addFriends = true;
    this.fetchByNickname("", false);
  }

  public fetchByNickname(nickname: string, reset: boolean) {
    if (reset) {
      this.currentPage = 1;
      this.usersPagedRequest.page = 0;
    }
    this.isNicknameSearch = true;
    console.log(nickname)
    window.clearTimeout(this.timeout);
    this.timeout = window.setTimeout(() => {
      this.userService.getUsersByNickname(nickname, this.showBlocked, this.showFriends, this.usersPagedRequest).subscribe({
        next: users => {
          this.usersPaged = users;
          this.fetchImagesForUsers();
        }
      });
    }, 500);
  }

  public onPageChange(page: number) {
    this.usersPagedRequest.page= page - 1;
    this.currentPage = page;
    if(this.showFriends && !this.isNicknameSearch) {
      this.fetchAllFriends(false);
    } else if(this.showBlocked && !this.isNicknameSearch) {
      this.fetchAllBlocked(false);
    } else if (this.addFriends && !this.isNicknameSearch) {
      this.fetchByNickname(this.searchInput.nativeElement.value, false);
    } else if (this.isNicknameSearch) {
      this.fetchByNickname(this.searchInput.nativeElement.value, false);
    }
  }

  public redirectToProfile(id: number) {
    this.router.navigate(['/users', id]);
  }

  private fetchImagesForUsers() {
    this.usersPaged.values.forEach(user => {
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

  private reset() {
    this.resetSearchInput();
    this.resetResponse();
    this.showFriends = false;
    this.showBlocked =  false;
    this.addFriends = false;
    this.showFriendRequests = false;
    this.isNicknameSearch = false;
  }

  private resetSearchInput(): void {
    if (this.searchInput && this.searchInput.nativeElement) {
      this.searchInput.nativeElement.value = '';
    }
    this.usersPagedRequest.page = 0;
    this.currentPage = 1;
  }

  private resetResponse(): void {
    this.usersPaged.values = [];
    this.usersPaged.totalElements = 0;
    this.usersPaged.totalPages = 0;
  }

}
