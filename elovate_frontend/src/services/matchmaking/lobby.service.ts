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
import {LobbyDto} from "../../dtos/Lobby/lobbyDto";
import {FinishedGameDto} from "../../dtos/finishedGameDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class LobbyService {

  private apiUrl = environment.backendUrl + '/api/v1/lobby/';


  constructor(private httpClient: HttpClient) {
  }

  getLobbyById(id: string) {
    return this.httpClient.get<LobbyDto>(this.apiUrl + id);
  }

  getLobbyByUserId(userId: number) {
    return this.httpClient.get<LobbyDto[]>(this.apiUrl + "user/" + userId);
  }

  getLobbyActivity(lobbyId: string) {
    return this.httpClient.get<boolean>(this.apiUrl + "activity/" + lobbyId);
  }

  getPostGameStats(lobbyId: string) {
    return this.httpClient.get<FinishedGameDto>(this.apiUrl + "finished-game/" + lobbyId)
  }
}
