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
import {HttpClient, HttpParams} from "@angular/common/http";
import {EloByDayDto} from "../../dtos/Statistics/eloByDayDto";
import {Observable} from "rxjs";
import {LeaderboardPlayerDto} from "../../dtos/Statistics/LeaderboardPlayerDto";
import {RankDistributionDto} from "../../dtos/Statistics/RankDistributionDto";
import {FinishedGameDto} from "../../dtos/finishedGameDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private apiUrl = environment.backendUrl + '/api/v1/statistics/';

  constructor(private httpClient: HttpClient) {
  }

  getEloOverTimeForUser(gameId: number): Observable<EloByDayDto[]> {
    return this.httpClient.get<EloByDayDto[]>(this.apiUrl + "game/" + gameId + "/eloOverTime");
  }

  getLeaderboard(gameId: number, countryCode?: string, count?: number): Observable<LeaderboardPlayerDto[]> {
    let params = new HttpParams();
    if (countryCode) {
      params = params.set('countryCode', countryCode)
    }
    if (count) {
      params = params.set('count', count);
    }
    return this.httpClient.get<LeaderboardPlayerDto[]>(this.apiUrl + gameId + "/leaderboard", {params: params});
  }

  getUserInLeaderboard(gameId: number, userId: number, withCountry: boolean): Observable<LeaderboardPlayerDto> {
    let params = new HttpParams().set('withCountry', withCountry)
    return this.httpClient.get<LeaderboardPlayerDto>(this.apiUrl+ gameId + "/leaderboard/" + userId, {params: params});
  }

  getRankDistribution(gameId: number) {
    return this.httpClient.get<RankDistributionDto>(this.apiUrl + gameId + "/distribution");
  }

  getUserInLobby(lobbyId: string): Observable<LeaderboardPlayerDto[]> {
    return this.httpClient.get<LeaderboardPlayerDto[]>(this.apiUrl+ "lobby/" + lobbyId);
  }
}
