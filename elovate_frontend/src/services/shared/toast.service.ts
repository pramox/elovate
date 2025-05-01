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
import {MessageService} from "primeng/api";
import {Router} from "@angular/router";
import {TranslateService} from "@ngx-translate/core";

@Injectable()
export class ToastService {

  static readonly life = 5000;

  messageService = inject(MessageService);
  router = inject(Router);
  translateService = inject(TranslateService);

  addError(error: any, title?: string, redirectUrl?: string) {

    if (!title) {
      title = this.translateService.instant("ERROR_TITLE");
    } else {
      title = this.translateService.instant(title);
    }

    console.log(error);

    if (typeof error === 'string') {
      const translated = this.translateService.instant(error);
      this.addMessage('error', translated, title ? title : '');
      return;
    }

    let errorArray = error.error;
    let errorString = '';

    if (typeof errorArray === 'string') {
      errorString = errorArray;
    } else if (Array.isArray(errorArray)) {
      for (const element of errorArray) {
        if (element?.message && element?.errorKey) {
          let translatedMessage = this.translateService.instant(element?.errorKey)
          if (translatedMessage) {
            errorString += translatedMessage + "\n";
          }
        }
      }
    } else if (errorArray && typeof errorArray.message === 'string') {
      // Handle case where errorArray is an object with a message property
      errorString = errorArray.message;
    }

    // Set errorString to "Unknown Error" if it's empty or contains only whitespace
    if (!errorString.trim()) {
      errorString = this.translateService.instant("elovate.error.unknown-error");
    }

    const detail = this.translateService.instant("elovate.error.detail-prefix") + errorString;

    this.addMessage('error', detail, title ? title : '');

    if (error.status === 404) {
      this.router.navigate([redirectUrl]);
    }
  }

  addSuccess(message: string, title?: string) {
    if (!title) {
      title = "Success";
    }
    const translated = this.translateService.instant(message);
    const translatedTitle = this.translateService.instant(title);
    this.addMessage('success', translated, translatedTitle);
  }

  private addMessage(severity: string, message: string, title: string) {
    if (!title) {
      title = message;
    }

    this.messageService.add({
      severity: severity,
      summary: title,
      detail: message,
      life: ToastService.life
    })
  }
}
