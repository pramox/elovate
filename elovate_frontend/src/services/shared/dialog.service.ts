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
import {ConfirmationService} from "primeng/api";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";

@Injectable({
  providedIn: 'root'
})
export class DialogService {

  confirmationService = inject(ConfirmationService);
  router = inject(Router);

  constructor(private translationService: TranslateService) {
  }

  async addDialog(message: string, _header?: string): Promise<boolean> {
    const header = _header || this.translationService.instant('elovate.dialog.header');
    return new Promise<boolean>((resolve) => {
      this.confirmationService.confirm({
        key: 'elovate-dialog',
        header: header,
        message: message,
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          resolve(true);
        },
        reject: () => {
          resolve(false);
        }
      });
    });
  }
}
