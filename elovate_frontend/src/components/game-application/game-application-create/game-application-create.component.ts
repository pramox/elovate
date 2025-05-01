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
import {Genre, Genre2LabelMapping} from "../../../dtos/enums/genre";
import {GameApplicationRequestDto} from "../../../dtos/GameApplication/gameApplicationRequestDto";
import {GameApplicationService} from "../../../services/game-application/game-application.service";
import {Router} from "@angular/router";
import {ImageCroppedEvent, LoadedImage} from "ngx-image-cropper";
import {ToastService} from "../../../services/shared/toast.service";
import {GameApplicationContractDto} from "../../../dtos/GameApplication/gameApplicationContractDto";

@Component({
  selector: 'app-game-application-create',
  templateUrl: './game-application-create.component.html',
  styleUrls: ['./game-application-create.component.scss']
})
export class GameApplicationCreateComponent implements OnInit {

  constructor(private gameApplicationService: GameApplicationService,
              private router: Router,
              private toastService: ToastService) {
  }

  ngOnInit(): void {
    // not needed
  }

  genres = Object.values(Genre);

  genre2LabelMapper = Genre2LabelMapping;

  gameApplicationRequestDto: GameApplicationRequestDto = {
    name: '',
    genre: Genre.MOBA,
    drawPossible: false,
    image: null,
    playersPerTeam: 1,
    agreedToContract: false
  };

  imageChangedEvent: any = '';
  croppedImage = null;
  fileType: any = '';
  file: File | null = null;
  inImageEditMode: boolean = false;
  url: any = ''

  onSubmit() {
    console.log('User submitted:', this.gameApplicationRequestDto);
    if (this.gameApplicationRequestDto.name === '') {
      this.toastService.addError('GAME_NAME_INVALID');
    }
    if (!this.gameApplicationRequestDto.agreedToContract) {
      this.toastService.addError('elovate.error.contract.not-agreed');
    }

    if (this.gameApplicationRequestDto.name &&
      this.gameApplicationRequestDto.genre &&
      this.gameApplicationRequestDto.agreedToContract) {
      console.log(this.gameApplicationRequestDto);
      this.gameApplicationService.createGameApplication(this.gameApplicationRequestDto).subscribe({
        next: (response) => {
          console.log(response);
          this.router.navigate(['game-application/' + response.id])
        },
        error: err => {
          this.toastService.addError(err);
        }
      });
    } else {
      this.toastService.addError('elovate.error.game-application-create.invalid-data')
    }
  }

  checkBoxChange() {
    this.gameApplicationRequestDto.agreedToContract = !this.gameApplicationRequestDto.agreedToContract;
  }

  fileChangeEvent(event: any): void {
    this.imageChangedEvent = event;
    this.fileType = event.target.files[0].type;
    this.inImageEditMode = true;
  }

  imageCropped(event: ImageCroppedEvent) {
    if(event.blob) {
      this.file = new File([event.blob], "my_image.png",{type: this.fileType, lastModified:new Date().getTime()})
    }
  }

  saveImage(event: any) {
    this.gameApplicationRequestDto.image = this.file;
    this.inImageEditMode = false;
    const reader = new FileReader();

    reader.onload = (event: any) => {
      this.url = event.target.result;
    };

    reader.onerror = (event: any) => {
      console.log("File could not be read: " + event.target.error.code);
      this.toastService.addError('elovate.error.game-application-create.file-invalid')
    };

    if (this.file) {
      reader.readAsDataURL(this.file);
    }
  }

  cancelImage(event: any) {
    this.file = null;
    this.inImageEditMode = false;
  }

  openContract() {
    this.gameApplicationService.getGameApplicationContract().subscribe({
      next: (dto: GameApplicationContractDto) => {
        const base64Content: string = dto.data;
        const byteCharacters = atob(base64Content);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], {type: 'application/pdf'});
        const pdfUrl = URL.createObjectURL(blob);
        window.open(pdfUrl, '_blank');
      },
      error: err => {
        console.error('Error fetching PDF data:', err);
        this.toastService.addError(err);
      }
    });
  }

  imageLoaded(image: LoadedImage) {
    // show cropper
  }

  cropperReady() {
    // cropper ready
  }

  loadImageFailed() {
    // show message
  }

}
