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
import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RatingParametersService} from "../../../services/rating/rating-parameters.service";
import {Glicko2RatingParametersDto} from "../../../dtos/rating/glicko2RatingParametersDto";
import {ActivatedRoute} from "@angular/router";
import {FormsModule} from "@angular/forms";
import {ToastService} from "../../../services/shared/toast.service";
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'app-rating-parameters',
  standalone: true,
  imports: [CommonModule, FormsModule, TranslateModule],
  templateUrl: './glicko2-rating-parameters.component.html',
  styleUrl: './glicko2-rating-parameters.component.scss'
})
export class Glicko2RatingParametersComponent implements OnInit {

  toastService = inject(ToastService);
  activatedRoute = inject(ActivatedRoute);
  ratingParameterService = inject(RatingParametersService);
  glicko2RatingParametersDto: Glicko2RatingParametersDto | null = null;
  gameId: number | null;

  ngOnInit(): void {
    this.activatedRoute.paramMap.subscribe(params => {
      const idParam = params.get('id');
      if (idParam) {
        const parsedId = parseInt(idParam, 10);
        if (!isNaN(parsedId)) {
          this.gameId = parsedId;
          this.fetchGlicko2RatingParameters();
        }
      }
    }, error => {
      this.toastService.addError(error);
    });
  }

  private fetchGlicko2RatingParameters(): void {
    if (this.gameId !== null) {
      this.ratingParameterService.getGlicko2RatingParametersForGame(this.gameId)
        .subscribe(
          (data: Glicko2RatingParametersDto) => {
            this.glicko2RatingParametersDto = data;
          },
          (error) => {
            this.toastService.addError(error)
          }
        );
    }
  }

  saveGlicko2RatingParameters() {
    if (!this.gameId || !this.glicko2RatingParametersDto) {
      return;
    }
    this.ratingParameterService.updateGlicko2RatingParametersForGame(this.gameId, this.glicko2RatingParametersDto)
      .subscribe(
        (data: Glicko2RatingParametersDto) => {
          this.glicko2RatingParametersDto = data;
          this.toastService.addSuccess('SUCCESSFULLY_UPDATED_RATING_PARAMETERS')
        },
        (error) => {
          this.toastService.addError(error)
        }
      );
  }
}
