import {Component, OnInit} from '@angular/core';
import {GameApplicationService} from "../../../services/game-application/game-application.service";
import {Router} from "@angular/router";
import {GameApplicationStatus, GameApplicationStatus2LabelMapping} from "../../../dtos/enums/gameApplicationStatus";
import {GameApplicationPagedFilterDto} from "../../../dtos/GameApplication/gameApplicationPagedFilterDto";
import {GameApplicationResponsePagedDto} from "../../../dtos/GameApplication/gameApplicationResponsePagedDto";
import {ToastService} from "../../../services/shared/toast.service";

@Component({
  selector: 'app-game-application-admin-overview',
  templateUrl: './game-application-admin-overview.component.html',
  styleUrls: ['./game-application-admin-overview.component.scss']
})
export class GameApplicationAdminOverviewComponent implements OnInit {

  // response dto and splitting
  gameApplicationResponseDto: GameApplicationResponsePagedDto = {} as GameApplicationResponsePagedDto;
  chunkedGameApplications: any[][] = [];

  // selection and mapping
  selectedGameApplicationStatus = GameApplicationStatus.PENDING
  gameApplicationStatus2LabelMapping = GameApplicationStatus2LabelMapping;
  gameApplicationStatuses = Object.values(GameApplicationStatus);

  // request dto
  gameApplicationPagedFilterDto: GameApplicationPagedFilterDto = {} as GameApplicationPagedFilterDto;

  // pagination
  currentPage = 1;
  maxPage = 1;

  constructor(
    private gameApplicationService: GameApplicationService,
    private router: Router,
    private toastService: ToastService
  ) {
  }

  ngOnInit(): void {
    this.gameApplicationPagedFilterDto.page = 0;
    this.gameApplicationPagedFilterDto.size = 10;
    this.gameApplicationPagedFilterDto.status = GameApplicationStatus.PENDING;
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
    this.maxPage = this.gameApplicationResponseDto.totalPages - 1;
  }

  getGameApplicationsByStatus(gameApplicationPagedFilterDTO: GameApplicationPagedFilterDto): void {
    this.gameApplicationService.getGameApplicationsByStatus(gameApplicationPagedFilterDTO)
      .subscribe({
        next: games => {
          this.gameApplicationResponseDto = games;
          console.log(this.gameApplicationResponseDto);
          this.chunkedGameApplications = this.chunkArray(this.gameApplicationResponseDto.values, 2);
        },
        error: err => {
          this.toastService.addError(err, undefined, '/home')
        }
      });
  }

  selectionOnChange(event: any) {
    console.log(this.selectedGameApplicationStatus.toString())
    this.gameApplicationPagedFilterDto.status = this.selectedGameApplicationStatus;
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
  }

  onPageChange(page: number) {
    this.gameApplicationPagedFilterDto.page = page - 1;
    this.currentPage = page;
    console.log(this.currentPage)
    this.getGameApplicationsByStatus(this.gameApplicationPagedFilterDto);
  }

  redirectToGameApplicationDetailView(id: number) {
    this.router.navigate(['admin/game-application/' + id]);
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
