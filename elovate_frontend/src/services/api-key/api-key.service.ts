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
import {ApiKeyDto} from "../../dtos/GameApplication/apiKeyDto";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class ApiKeyService {

  private apiUrl = environment.backendUrl + '/api/v1/api-keys';


  constructor(private http: HttpClient) {
  }

  generateApiKey(gameId: number): Observable<ApiKeyDto> {
    return this.http.post<ApiKeyDto>(`${this.apiUrl}/generateApiKey?gameId=${gameId}`, {});
  }

  getApiKeyHash(id: number): Observable<ApiKeyDto> {
    return this.http.get<ApiKeyDto>(`${this.apiUrl}/${id}`);
  }

  deleteApiKey(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
