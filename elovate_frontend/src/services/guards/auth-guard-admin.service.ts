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
import {CanActivate, Router} from "@angular/router";
import {AuthService} from "../users/auth.service";
import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class AuthGuardAdminService implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {
  }

  canActivate(): boolean {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['']);
    }
    if (!this.auth.isAdmin()) {
      this.router.navigate(['']);
    }
    return true;
  }

}
