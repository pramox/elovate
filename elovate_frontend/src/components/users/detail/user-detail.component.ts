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
import {ActivatedRoute, Router} from '@angular/router';
import {UserService} from "../../../services/users/user.service";
import {AdminService} from "../../../services/users/admin.service";
import {ExtendedUserDto} from "../../../dtos/extendedUserDto";
import {AuthService} from "../../../services/users/auth.service";
import {Role} from "../../../dtos/enums/role";
import {UserStatus} from "../../../dtos/enums/userStatus";
import {ToastService} from "../../../services/shared/toast.service";
import {TranslateService} from "@ngx-translate/core";

@Component({
  selector: 'app-user-detail',
  templateUrl: './user-detail.component.html',
  styleUrls: ['./user-detail.component.scss']
})
export class UserDetailComponent implements OnInit {
  user: ExtendedUserDto | null = null;

  private readonly maxFileSize = 2 * 1024 * 1024;

  error: string | null = null;

  success: string | null = null;

  viewerIsAdmin: boolean;
  viewerIsSameUser: boolean;

  isAdminView: boolean;

  countryCodes = ['AT', 'DE', 'EN']; // todo: use proper enum
  statuses = Object.values(UserStatus);

  email: string;

  constructor(private route: ActivatedRoute,
              private authService: AuthService,
              private userService: UserService,
              private adminService: AdminService,
              private toastService: ToastService,
              private router: Router,
              private translateService: TranslateService) {
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.isAdminView = data['isAdminView'];
    });

    let userId;

    if (this.isAdminView) {
      const idParam = this.route.snapshot.paramMap.get('id');
      userId = idParam ? parseInt(idParam) : null;
    } else {
      userId = this.authService.getLoggedInUserId();
    }

    this.viewerIsAdmin = this.authService.isAdmin();
    this.viewerIsSameUser = this.authService.getLoggedInUserId() === userId;

    if (!userId) {
      this.toastService.addError("elovate.error.unknown-error");
    }

    if (userId) {
      this.userService.getExtendedUserbyId(userId).subscribe({
        next: (data) => {
          console.log(data);
          this.user = data;
          this.email = this.user.email;
        },
        error: (error) => {
          this.toastService.addError(error);
        }
      });
    }
  }

  makeUserAdmin(): void {
    if (this.user?.id) {
      this.adminService.promoteToAdmin(this.user.id).subscribe({
        next: admin => {
          this.user = admin;
          this.toastService.addSuccess(this.translateService.instant('elovate.user-detail.admin-promote.success'))
        },
        error: (error) => {
          this.toastService.addError(error);
        }
      });
    }
  }

  makeAdminUser() {
    if (this.user?.id) {
      this.adminService.demoteToUser(this.user.id).subscribe({
        next: user => {
          this.user = user;
          this.toastService.addSuccess(this.translateService.instant('elovate.user-detail.user-demote.success'))
        },
        error: (error) => {
          this.toastService.addError(error);
        }
      });
    }
  }

  saveUserDetails() {
    if (this.user) {
      this.userService.updateUser(this.user).subscribe({
        next: (data) => {
          this.user = data;
          this.toastService.addSuccess(this.translateService.instant('elovate.user-detail.save-changes.success'));
          if (this.user.email !== this.email && this.viewerIsSameUser) {
            //email was changed, so we need to relogin
            this.authService.logout();
            this.router.navigate(['/login']);
            this.toastService.addSuccess(this.translateService.instant('elovate.email-reset.success'));
          }
          else {
            window.location.reload();
          }
        },
        error: (error) => {
          this.toastService.addError(error);
        }
      });
    }
  }

  viewedUserIsAdmin() {
    return this.user?.roles.some(role => role === Role.ADMIN);
  }

  onFileSelected(event: any) {
    if (this.user && event.target.files?.[0]) {
      const file = event.target.files[0];
      this.user.image = file;
      if (this.user.image && this.user.image.size > this.maxFileSize) {
        console.error("File too large")
        this.toastService.addError("elovate.user-detail.upload-img.too-large");
        this.user.image = null;
      } else {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => {
          let result = reader.result;
          if (this.user && result) {
            if (typeof result === 'string') {
              this.user.imageBase64 = result;
            } else {
              result = new TextDecoder('utf-8').decode(result);
              this.user.imageBase64 = result;
            }
          }
        }
      }
    }
  }

  goToPasswordReset() {
    this.router.navigate(['/reset-password'])
  }

}
