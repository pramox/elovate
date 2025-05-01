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
import {Component, inject} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from "@angular/forms";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {AuthService} from "../../services/users/auth.service";
import {UserService} from "../../services/users/user.service";
import {ToastService} from "../../services/shared/toast.service";
import {PasswordResetDto} from "../../dtos/passwordResetDto";
import {Router} from "@angular/router";

@Component({
  selector: 'app-password-reset',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './password-reset.component.html',
  styleUrl: './password-reset.component.scss'
})
export class PasswordResetComponent {

  private authService = inject(AuthService);
  private userService = inject(UserService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private translateService = inject(TranslateService);

  passwordResetDto: PasswordResetDto;

  ngOnInit(): void {
    this.passwordResetDto = {
      oldPassword: '',
      newPassword: '',
      newPasswordRepeated: ''
    }
  }

  resetPassword() {
    this.userService.resetPassword(this.passwordResetDto).subscribe({
      next: user => {
        this.toastService.addSuccess(this.translateService.instant("elovate.password-reset.success"))
        this.authService.logout();
        this.router.navigate(['/home'])
      },
      error: (error) => {
        this.toastService.addError(error);
      }
    });
  }
}
