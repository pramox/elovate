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
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError} from 'rxjs';
import {AuthService} from "../services/users/auth.service";
import {environment} from "../environments/environment";
import {Router} from "@angular/router";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private router: Router) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const loginUrl = environment.backendUrl + '/api/v1/users/login';
    const registerUrl = environment.backendUrl + '/api/v1/users/register';
    const oauthUrl = environment.backendUrl + '/api/v1/users/oauth/token';
    const translationUrl = "/assets/i18n/*";

    // Do not intercept authentication requests
    if (req.url === loginUrl || req.url === registerUrl || req.url === oauthUrl || req.url === translationUrl) {
      return next.handle(req);
    } else {
      const authReq = req.clone({
        headers: req.headers.set('Authorization', 'Bearer ' + this.authService.fetchToken())
      });

      return next.handle(authReq).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 403) {
            console.error('403 Forbidden Error:', error);
            this.authService.logout();
            this.router.navigate(['/login']);
            return throwError(() => new Error('Forbidden'));
          }
          return throwError(() => error);
        })
      );
    }
  }
}


