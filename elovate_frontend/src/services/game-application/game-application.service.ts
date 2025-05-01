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
import {GameApplicationResponseDto} from "../../dtos/GameApplication/gameApplicationResponseDto";
import {GameApplicationPagedFilterDto} from "../../dtos/GameApplication/gameApplicationPagedFilterDto";
import {GameApplicationResponsePagedDto} from "../../dtos/GameApplication/gameApplicationResponsePagedDto";
import {GameApplicationUpdateRequestDto} from "../../dtos/GameApplication/gameApplicationUpdateRequestDto";
import {GameApplicationCommentRequestDto} from "../../dtos/GameApplication/Comment/gameApplicationCommentRequestDto";
import {GameApplicationResponseDetailDto} from "../../dtos/GameApplication/gameApplicationResponseDetailDto";
import {GameApplicationRequestDto} from "../../dtos/GameApplication/gameApplicationRequestDto";
import {GameApplicationContractDto} from "../../dtos/GameApplication/gameApplicationContractDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class GameApplicationService {

  private apiUrl = environment.backendUrl + '/api/v1/game-applications';


  constructor(private httpClient: HttpClient) {
  }


  createGameApplication(gameApplicationRequestDto: GameApplicationRequestDto): Observable<GameApplicationResponseDto> {
    const formData = new FormData();
    formData.append('name', gameApplicationRequestDto.name);
    formData.append('genre', gameApplicationRequestDto.genre);
    formData.append('drawPossible', '' + gameApplicationRequestDto.drawPossible);
    formData.append('playersPerTeam', '' + gameApplicationRequestDto.playersPerTeam);
    formData.append('agreedToContract', '' + gameApplicationRequestDto.agreedToContract);
    if (gameApplicationRequestDto.image != null) {
      formData.append('image', gameApplicationRequestDto.image, gameApplicationRequestDto.image.name);
    }

    return this.httpClient.post<GameApplicationResponseDto>(this.apiUrl, formData);
  }


  getGameApplicationsByStatus(gameApplicationPagedFilterDTO: GameApplicationPagedFilterDto): Observable<GameApplicationResponsePagedDto> {
    let params = new URLSearchParams();
    params.set('page', gameApplicationPagedFilterDTO.page.toString());
    params.set('size', gameApplicationPagedFilterDTO.size.toString());
    params.set('status', gameApplicationPagedFilterDTO.status.toString().toUpperCase());

    return this.httpClient.get<GameApplicationResponsePagedDto>(this.apiUrl + "?" + params.toString());
  }

  getGameApplicationById(id: number): Observable<GameApplicationResponseDetailDto> {
    return this.httpClient.get<GameApplicationResponseDetailDto>(this.apiUrl + "/" + id);
  }

  updateGameApplicationStatus(id: number, gameApplicationUpdateRequestDTO: GameApplicationUpdateRequestDto): Observable<GameApplicationResponseDto> {
    const formData = new FormData();
    formData.append('status', gameApplicationUpdateRequestDTO.status);
    return this.httpClient.put<GameApplicationResponseDto>(this.apiUrl + "/" + id, formData);
  }

  commentOnGameApplication(id: number, gameApplicationCommentRequestDTO: GameApplicationCommentRequestDto): Observable<GameApplicationResponseDetailDto> {
    const formData = new FormData();
    formData.append('commentContent', gameApplicationCommentRequestDTO.commentContent);
    return this.httpClient.put<GameApplicationResponseDetailDto>(
      this.apiUrl + "/" + id + "/comment",
      formData,
    );
  }

  getGameApplicationContract() {
    return this.httpClient.get<GameApplicationContractDto>(this.apiUrl + "/getGameApplicationContract");
  }

  getGameByGameApplicationId(id: number) {
    return this.httpClient.get<number>(this.apiUrl + "/getGameByGameApplicationId/" + id);
  }
}
