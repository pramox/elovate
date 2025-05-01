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
import {GameDetailDto} from "../../dtos/Game/gameDetailDto";
import {GameAccessDto} from "../../dtos/Game/gameAccessDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class GameService {

  private apiUrl = environment.backendUrl + '/api/v1/games';


  constructor(private httpClient: HttpClient) {
  }

  getGameById(id: number): Observable<GameDetailDto> {
    return this.httpClient.get<GameDetailDto>(this.apiUrl + '/' + id);
  }

  getAllGames(activated: boolean): Observable<GameDetailDto[]> {
    return this.httpClient.get<GameDetailDto[]>(this.apiUrl + '?activated=' + activated);
  }

  getGenerateUuid(gameAccess: GameAccessDto): Observable<GameAccessDto> {
    return this.httpClient.post<GameAccessDto>(this.apiUrl + '/access', gameAccess);
  }
}
