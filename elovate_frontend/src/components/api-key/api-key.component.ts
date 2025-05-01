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
import {Component, Input, OnInit} from '@angular/core';
import {ApiKeyDto} from "../../dtos/GameApplication/apiKeyDto";
import {ApiKeyService} from "../../services/api-key/api-key.service";
import {ConfirmationService} from 'primeng/api';
import {ToastService} from "../../services/shared/toast.service";
import {TranslateService} from "@ngx-translate/core";
import {DialogService} from "../../services/shared/dialog.service";


@Component({
  selector: 'app-api-key',
  templateUrl: './api-key.component.html',
  styleUrls: ['./api-key.component.scss'],
  providers: [ConfirmationService]
})
export class ApiKeyComponent implements OnInit {
  @Input() gameId: number;
  apiKey: ApiKeyDto | null;
  visible = false;
  hash = false;

  constructor(private apiKeyService: ApiKeyService,
              private confirmationService: ConfirmationService,
              private toastService: ToastService,
              private translateService: TranslateService,
              private dialogService: DialogService) {
  }

  ngOnInit(): void {
    // not needed
  }

  generateKey(): void {
    if (this.gameId) {
      this.apiKeyService.generateApiKey(this.gameId).subscribe({
        next: data => {
          this.apiKey = data;
        },
        error: err => this.toastService.addError(err)
      });
    } else {
      console.error('Game ID is required');
    }
  }

  showApiKeyHash(): void {
    this.visible = false;
    this.hash = true;
    if (this.gameId) {
      this.apiKeyService.getApiKeyHash(this.gameId).subscribe({
        next: data => {
          this.apiKey = data;
          this.visible = true;
        },
        error: err => {
          this.toastService.addError(err);
        }
      });
    } else {
      console.error('Game ID is required');
    }
  }

  deleteKey(): void {
    this.hash = false;
    if (this.gameId) {
      this.apiKeyService.deleteApiKey(this.gameId).subscribe({
        next: () => {
          this.apiKey = null;
        },
        error: err => {
          this.toastService.addError(err);
        }
      });
    } else {
      console.error('Game ID is required');
    }
  }

  copyToClipboard() {
    navigator.clipboard.writeText(this.apiKey?.key ? this.apiKey.key : "")
      .then(_ => console.log('written to clipboard'));
  }

  async confirmDelete() {
    this.hash = false;
    this.visible = false;
    const bool = await this.dialogService.addDialog(
      this.translateService.instant('elovate.api-key.delete-dialog.message'),
      this.translateService.instant('elovate.api-key.delete-dialog.header')
    );
    if(bool){
      this.deleteKey()
      this.toastService.addSuccess( this.translateService.instant('elovate.api-key.delete-dialog.toast'));
    }
  }

  async confirmGenerate() {
    this.hash = false;
    this.visible = false;
    const bool = await this.dialogService.addDialog(
      this.translateService.instant('elovate.api-key.generate-dialog.message'),
      this.translateService.instant('elovate.api-key.generate-dialog.header')
    );
    if(bool){
      this.generateKey();
      this.visible = true;
      this.toastService.addSuccess( this.translateService.instant('elovate.api-key.generate-dialog.toast'));
    }
  }

}
