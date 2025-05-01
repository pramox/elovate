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
import {GameApplicationService} from "../../../services/game-application/game-application.service";
import {ActivatedRoute, Router} from "@angular/router";
import {GameApplicationStatus, GameApplicationStatus2LabelMapping} from "../../../dtos/enums/gameApplicationStatus";
import {GameApplicationPagedFilterDto} from "../../../dtos/GameApplication/gameApplicationPagedFilterDto";
import {GameApplicationResponsePagedDto} from "../../../dtos/GameApplication/gameApplicationResponsePagedDto";
import {AuthService} from "../../../services/users/auth.service";
import {ToastService} from "../../../services/shared/toast.service";

@Component({
  selector: 'app-game-application-overview',
  templateUrl: './game-application-overview.component.html',
  styleUrls: ['./game-application-overview.component.scss']
})
export class GameApplicationOverviewComponent implements OnInit {

  // response dto and splitting
  gameApplicationResponseDto: GameApplicationResponsePagedDto = {} as GameApplicationResponsePagedDto;
  chunkedGameApplications: any[][] = [];

  // selection and mapping
  selectedGameApplicationStatus = GameApplicationStatus.ACCEPTED
  gameApplicationStatus2LabelMapping = GameApplicationStatus2LabelMapping;
  gameApplicationStatuses = Object.values(GameApplicationStatus);

  // request dto
  gameApplicationPagedFilterDto: GameApplicationPagedFilterDto = {} as GameApplicationPagedFilterDto;

  // pagination
  currentPage = 1;
  maxPage = 1;

  constructor(
    private gameApplicationService: GameApplicationService,
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService,
    private toastService: ToastService
  ) {
  }

  ngOnInit(): void {
    this.gameApplicationPagedFilterDto.page = 0;
    this.gameApplicationPagedFilterDto.size = 10;
    this.gameApplicationPagedFilterDto.status = GameApplicationStatus.ACCEPTED;
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
  }

  getGameApplicationsByStatus(gameApplicationPagedFilterDTO: GameApplicationPagedFilterDto): void {
    this.gameApplicationService.getGameApplicationsByStatus(gameApplicationPagedFilterDTO)
      .subscribe({
        next: games => {
          this.gameApplicationResponseDto = games;
          this.maxPage = this.gameApplicationResponseDto.totalPages - 1;
          this.chunkedGameApplications = this.chunkArray(this.gameApplicationResponseDto.values, 2);
        },
        error: err => {
          this.toastService.addError(err, undefined, '/home')
        }
      });
  }

  selectionOnChange(event: any) {
    this.gameApplicationPagedFilterDto.status = this.selectedGameApplicationStatus;
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
  }

  onPageChange(page: number) {
    this.gameApplicationPagedFilterDto.page = page - 1;
    this.currentPage = page;
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
  }

  redirectToGameApplicationOrGameDetailView(id: number) {
    const isAccepted = this.selectedGameApplicationStatus === GameApplicationStatus.ACCEPTED;
    if (isAccepted) {
      this.gameApplicationService.getGameByGameApplicationId(id).subscribe({
        next: id => {
          this.router.navigate(['game/' + id + "/details/"])
        },
        error: err => {
          this.toastService.addError(err)
        }
      })
    } else if (this.authService.isAdmin()) {
      this.router.navigate(['admin/game-application/' + id]);
    } else {
      this.router.navigate(['game-application/' + id]);
    }
  }

  private chunkArray(array: any[], size: number): any[][] {
    return array.reduce((acc, val, i) => {
      let idx = Math.floor(i / size);
      let page = acc[idx] || (acc[idx] = []);
      page.push(val);
      return acc;
    }, []);
  }
}
