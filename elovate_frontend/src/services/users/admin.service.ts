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
import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ExtendedUserDto} from "../../dtos/extendedUserDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private apiUrl = environment.backendUrl + '/api/v1/admins/';

  token: string | null;

  constructor(private httpClient: HttpClient) {
    this.token = localStorage.getItem('authToken');
  }

  promoteToAdmin(userId: number): Observable<ExtendedUserDto> {
    return this.httpClient.patch<ExtendedUserDto>(this.apiUrl + "promote/" + userId, null);
  }

  demoteToUser(userId: number): Observable<ExtendedUserDto> {
    return this.httpClient.patch<ExtendedUserDto>(this.apiUrl + "demote/" + userId, null);
  }
}
