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
import {Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/users/auth.service";
import {ActivatedRoute, Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";
import {ToastService} from "../../services/shared/toast.service";
import {environment} from "../../environments/environment";

@Component({
  selector: 'app-login-page',
  templateUrl: './login-register.component.html',
  styleUrls: ['./login-register.component.scss']
})

export class LoginRegisterComponent implements OnInit {

  isLogin = true;

  loginUser = {
    nickname: '',
    password: ''
  };

  registerUser = {
    nickname: '',
    email: '',
    password: '',
    passwordReentered: '',
  }

  constructor(private authService: AuthService,
              private toastService: ToastService,
              private route: ActivatedRoute,
              private router: Router,
              private translateService: TranslateService) {
    this.authService.hasLoginError.subscribe(
      (hasError) => {
        if (hasError) {
          this.toastService.addError('INVALID_CREDENTIALS');
        }
      });
    this.authService.hasRegisterError.subscribe(
      (errorKey) => {
        if (errorKey) {
          console.log(errorKey)
          this.toastService.addError(this.translateService.instant(errorKey));
        }
      });
  }

  ngOnInit(): void {
    const urlParams = new URLSearchParams(window.location.search);
    const jwtToken = urlParams.get('jwt');
    if (jwtToken) {
      this.authService.handleSuccessfulOauth(jwtToken);
    }
    this.route.data.subscribe(data => {
      this.isLogin = data['isLogin'];
    });
  }

  navigateToPage(url: string): void {
    window.open(url, '_blank');
  }

  onSubmitLogin() {
    console.log('User submitted:', this.loginUser);
    if (this.loginUser.nickname && this.loginUser.password) {
      this.authService.login({
        nickname: this.loginUser.nickname,
        password: this.loginUser.password
      });
    } else {
      this.toastService.addError("MISSING_CREDENTIALS");
    }
  }

  onSubmitRegister() {
    console.log('User submitted for register:', this.registerUser);
    if (this.registerUser.nickname && this.registerUser.email && this.registerUser.password && this.registerUser.passwordReentered
      && this.registerUser.password === this.registerUser.passwordReentered) {
      this.authService.register({
        nickname: this.registerUser.nickname,
        password: this.registerUser.password,
        email: this.registerUser.email
      });
    } else {
      this.toastService.addError(this.translateService.instant("PASSWORD_NOT_MATCHING"));
    }
  }

  logout() {
    this.authService.logout();
  }

  redirectToOauth() {
    window.location.href = environment.backendUrl + "/api/v1/users/oAuth/login"
  }
}
