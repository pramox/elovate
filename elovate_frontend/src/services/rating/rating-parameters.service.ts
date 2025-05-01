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
import {inject, Injectable} from '@angular/core';
import {Glicko2RatingParametersDto} from "../../dtos/rating/glicko2RatingParametersDto";
import {Observable} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class RatingParametersService {

  httpClient = inject(HttpClient);
  apiUrl = environment.backendUrl + '/api/v1/rating-parameters';
  glicko2ApiUrl = this.apiUrl + "/glicko2/";

  getGlicko2RatingParametersForGame(gameId: number): Observable<Glicko2RatingParametersDto> {
    return this.httpClient.get<Glicko2RatingParametersDto>(this.glicko2ApiUrl + gameId);
  }

  updateGlicko2RatingParametersForGame(gameId: number, glicko2RatingParametersDto: Glicko2RatingParametersDto) {
    return this.httpClient.put<Glicko2RatingParametersDto>(this.glicko2ApiUrl + gameId, glicko2RatingParametersDto);
  }
}
