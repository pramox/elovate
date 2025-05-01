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
import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {UserResponseDto} from "../../dtos/userResponseDto";
import {ExtendedUserDto} from "../../dtos/extendedUserDto";
import {PasswordResetDto} from "../../dtos/passwordResetDto";
import {Observable} from "rxjs";
import {Base64StringDto} from "../../dtos/base64StringDto";
import {environment} from "../../environments/environment";
import {PagedRequestDto} from "../../dtos/pagedRequestDto";
import {UserResponsePagedDto} from "../../dtos/userResponsePagedDto";
import {UserProfileDto} from "../../dtos/userProfileDto";
import {UserPagedRequestDto} from "../../dtos/userPagedRequestDto";
import {UserGameRankingPagedDto} from "../../dtos/userGameRankingPagedDto";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  token: string | null;

  private apiUrl = environment.backendUrl + '/api/v1/users';

  constructor(private httpClient: HttpClient) {
    this.token = localStorage.getItem('authToken');
  }

  getExtendedUserbyId(id: number) {
    return this.httpClient.get<ExtendedUserDto>(this.apiUrl + "/extended/" + id);
  }

  updateUser(user: ExtendedUserDto) {
    let data = new FormData();

    data.append('id', user.id.toString());
    data.append('email', user.email);
    data.append('nickName', user.nickName);
    data.append('status', user.status.toString());
    data.append('countryCode', user.countryCode);
    user.roles.forEach((role) => {
      data.append('roles', role);
    });
    if (user.image) {
      data.append('image', user.image, user.image.name);
    }

    return this.httpClient.put<ExtendedUserDto>(this.apiUrl + "/" + user.id, data);
  }

  resetPassword(passwordResetDto: PasswordResetDto): Observable<UserResponseDto> {
    return this.httpClient.put<UserResponseDto>(this.apiUrl + "/reset-password", passwordResetDto);
  }

  getImageFromUser(id: number): Observable<Base64StringDto> {
    return this.httpClient.get<Base64StringDto>(`${this.apiUrl}/${id}/image`);
  }

  getUsersByNickname(nickname: string, blocked: boolean, friends: boolean, pagedRequest: PagedRequestDto): Observable<UserResponsePagedDto> {
    let query = "?nickname=" + nickname;
    if (blocked) {
      query += "&blocked=true";
    }
    if (friends) {
      query += "&friends=true";
    }
    if (pagedRequest.page) {
      query += "&page=" + pagedRequest.page;
    }
    if (pagedRequest.size) {
      query += "&size=" + pagedRequest.size;
    }
    return this.httpClient.get<UserResponsePagedDto>(this.apiUrl + query);
  }

  getAllFriends(pagedRequest: PagedRequestDto): Observable<UserResponsePagedDto> {
    let query = "?";
    if (pagedRequest.page) {
      query += "page=" + pagedRequest.page;
    }
    if (pagedRequest.size) {
      query += "&size=" + pagedRequest.size;
    }
    return this.httpClient.get<UserResponsePagedDto>(this.apiUrl + "/friends" + query);
  }

  getAllBlocked(pagedRequest: PagedRequestDto): Observable<UserResponsePagedDto> {
    let query = "?";
    if (pagedRequest.page) {
      query += "page=" + pagedRequest.page;
    }
    if (pagedRequest.size) {
      query += "&size=" + pagedRequest.size;
    }
    return this.httpClient.get<UserResponsePagedDto>(this.apiUrl + "/blocked" + query);
  }

  getFriendRequests(pagedRequest: PagedRequestDto): Observable<UserResponsePagedDto> {
    let query = "?";
    if (pagedRequest.page) {
      query += "page=" + pagedRequest.page;
    }
    if (pagedRequest.size) {
      query += "&size=" + pagedRequest.size;
    }
    return this.httpClient.get<UserResponsePagedDto>(this.apiUrl + "/requests" + query);
  }

  getProfileById(id: number): Observable<UserProfileDto> {
    return this.httpClient.get<UserProfileDto>(this.apiUrl + "/profile/" + id);
  }

  addFriend(id: number): Observable<UserProfileDto> {
    return this.httpClient.post<UserProfileDto>(this.apiUrl + "/befriend/" + id, null);
  }

  confirm(id: number): Observable<UserProfileDto> {
    return this.httpClient.post<UserProfileDto>(this.apiUrl + "/confirm/" + id, null);
  }

  removeFriend(id: number): Observable<UserProfileDto> {
    return this.httpClient.post<UserProfileDto>(this.apiUrl + "/unfriend/" + id, null);
  }

  blockUser(id: number): Observable<UserProfileDto> {
    return this.httpClient.post<UserProfileDto>(this.apiUrl + "/block/" + id, null);
  }

  unblockUser(id: number): Observable<UserProfileDto> {
    return this.httpClient.post<UserProfileDto>(this.apiUrl + "/unblock/" + id, null);
  }

  getRankedGames(pagedRequest: UserPagedRequestDto): Observable<UserGameRankingPagedDto> {
    let query = "?";
    if (pagedRequest.page) {
      query += "page=" + pagedRequest.page;
    }
    if (pagedRequest.size) {
      query += "&size=" + pagedRequest.size;
    }
    return this.httpClient.get<UserGameRankingPagedDto>(this.apiUrl + "/profile/" + pagedRequest.userId + "/games" + query);
  }
}
