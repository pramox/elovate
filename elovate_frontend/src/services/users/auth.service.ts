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
import {Injectable, Injector} from "@angular/core";
import {BehaviorSubject} from "rxjs";
import {HttpClient} from "@angular/common/http";
import {JwtAuthenticationResponseDto} from "../../dtos/jwtAuthenticationResponseDto";
import {RegisterRequestDto} from "../../dtos/registerRequestDto";
import {Router} from "@angular/router";
import {LoginRequestDto} from "../../dtos/loginRequestDto";
import * as jose from 'jose'
import {decodeJwt} from 'jose'
import {QueueService} from "../matchmaking/queue.service";
import {environment} from "../../environments/environment";

@Injectable()
export class AuthService {
  public isLoggedInSubject = new BehaviorSubject<boolean>(false);
  private hasLoginErrorSubject = new BehaviorSubject<boolean>(false);
  private hasRegisterErrorSubject = new BehaviorSubject<string>("");

  private apiUrl = environment.backendUrl + '/api/v1/users/';


  token: string | null;
  hasLoginError = this.hasLoginErrorSubject.asObservable();
  hasRegisterError = this.hasRegisterErrorSubject.asObservable();

  constructor(private httpClient: HttpClient, private router: Router) {
    this.token = localStorage.getItem('authToken');
    this.isLoggedInSubject.next(!!this.token);
  }

  fetchToken(): string | null {
    return this.token;
  }

  login(requestDTO: LoginRequestDto) {
    this.hasLoginErrorSubject.next(false);
    this.httpClient.post<JwtAuthenticationResponseDto>(this.apiUrl + "login", requestDTO).subscribe({
      next: (value) => {
        this.handleIncomingToken(value.token);
        this.router.navigate(['/home']);
      },
      error: (error) => {
        console.log(error);
        this.hasLoginErrorSubject.next(true);
      }
    });
  }


  register(requestDTO: RegisterRequestDto) {
    this.httpClient.post<JwtAuthenticationResponseDto>(this.apiUrl + "register", requestDTO).subscribe({
      next: value => {
        this.handleIncomingToken(value.token)
        this.router.navigate([''])
      },
      error: err => {
        this.hasRegisterErrorSubject.next(err.error[0].errorKey);
      }
    });
  }

  logout() {
    this.isLoggedInSubject.next(false);
    this.token = null;
    localStorage.setItem('authToken', "");
    localStorage.setItem('userId', "");
  }

  isLoggedIn(): boolean {
    if (!this.token || !this.isLoggedInSubject.value) {
      return false;
    }
    return !AuthService.tokenExpired(this.token);
  }

  handleSuccessfulOauth(jwt: string) {
    this.handleIncomingToken(jwt);
    this.router.navigate(['home']);
  }

  private handleIncomingToken(token: string) {
    if (!token) {
      console.error("No token found in response")
      return;
    }
    this.token = token;
    localStorage.setItem('authToken', token);
    this.isLoggedInSubject.next(true);
  }

  isAdmin(): boolean {
    let isAdmin = false;
    if (this.token) {
      const roles: any = jose.decodeJwt(this.token)['roles'];
      for (const role of roles) {
        if (role.authority === 'ROLE_ADMIN') {
          isAdmin = true;
          break;
        }
      }
    }
    return isAdmin;
  }

  userIdMatchesJWT(id: number): boolean {
    if (this.token) {
      const userId: any = jose.decodeJwt(this.token)['id'];
      if (userId === Number(id)) {
        return true;
      }
    }
    return false;
  }

  getLoggedInUserId(): number | null {
    const token = localStorage.getItem('authToken')
    if (token) {
      const userId = decodeJwt(token)['id'];
      const numericUserId = Number(userId);
      if (!Number.isNaN(numericUserId)) {
        return numericUserId;
      }
    }
    return null;
  }

  getLoggedInUsername(): string | null {
    if (!this.isLoggedIn()) {
      return null;
    }
    const token = this.fetchToken();
    if (token) {
      const username = decodeJwt(token)['sub'];
      if (typeof username === 'string') {
        return username
      } else {
        return null
      }
    }
    return null
  }

  private static tokenExpired(token: string) {
    const expiry = (JSON.parse(atob(token.split('.')[1]))).exp;
    return (Math.floor((new Date).getTime() / 1000)) >= expiry;
  }
}
