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
import {CommonModule} from '@angular/common';
import {StatisticsService} from "../../services/statistics/statistics.service";
import {UserService} from "../../services/users/user.service";
import {AuthService} from "../../services/users/auth.service";
import {LeaderboardPlayerDto} from "../../dtos/Statistics/LeaderboardPlayerDto";
import {ActivatedRoute, Router} from "@angular/router";
import {GameService} from "../../services/game/game.service";
import {RankDistributionDto} from "../../dtos/Statistics/RankDistributionDto";
import {GameDetailDto} from "../../dtos/Game/gameDetailDto";
import {Genre} from "../../dtos/enums/genre";
import {ToastService} from "../../services/shared/toast.service";
import {LinePlotData} from "./linePlotData";

enum StatsStatus {
  USER_HAS_THIS_GAME = 0,
  USER_DOES_NOT_HAVE_THIS_GAME = 1,
  USER_HAS_NO_GAMES = 2,
  LOADING = 3
}

@Component({
  selector: 'app-statistics',
  providers: [CommonModule],
  templateUrl: './statistics.component.html',
  styleUrl: './statistics.component.scss'
})
export class StatisticsComponent implements OnInit {
  showStats: boolean = false;
  statsStatus: StatsStatus = StatsStatus.LOADING;
  globalLeaderboard: LeaderboardPlayerDto[]
  countryLeaderboard: LeaderboardPlayerDto[]
  rankDistributionForNgx: any[];
  linePlotData: LinePlotData | undefined = undefined;
  yScaleMin: number;

  myGames: GameDetailDto[] = Array(0).fill({
    id: -1,
    name: "",
    playersPerTeam: 0,
    genre: Genre.OTHER,
    image: "shimmer",
    developerDetails: {
      id: 0,
      nickname: "",
      email: ""
    }
  });


  userId: number;
  currentGameId: number;
  currentGameName: string;
  countryCode: string;

  userInGlobalLeaderboard: LeaderboardPlayerDto;
  userInCountryLeaderboard: LeaderboardPlayerDto;
  showUserSeparatelyGlobal: boolean;
  showUserSeparatelyCountry: boolean;
  scoreTitles: string[] = ['Place', 'Player', 'Rating', 'Country'];

  rankDistribution: RankDistributionDto;
  distributionColors: any[];

  constructor(private statisticsService: StatisticsService,
              private userService: UserService,
              private authService: AuthService,
              private gameService: GameService,
              private route: ActivatedRoute,
              private router: Router,
              private toastService: ToastService) {
  }

  ngOnInit() {
    const userId = this.authService.getLoggedInUserId();
    if (userId == null) {
      this.authService.logout()
      this.router.navigate(['home'])
    } else {
      this.userId = userId;
    }
    this.initComponent();
  }

  private async initComponent() {
    await this.getAllGames();
    this.statsStatus = this.getCurrentGameInfo();
    if (this.statsStatus === StatsStatus.USER_DOES_NOT_HAVE_THIS_GAME) {
      const nameOfOtherGame = this.returnGameNameIfUserHasAny();
      if (nameOfOtherGame === "") {
        this.statsStatus = StatsStatus.USER_HAS_NO_GAMES;
      } else {
        this.routeToGame(nameOfOtherGame);
      }
    } else if (this.statsStatus === StatsStatus.USER_HAS_THIS_GAME) {
      this.showStats = true;
      this.gameService.getGameById(this.currentGameId).subscribe(gameDetailDto => {
        this.currentGameName = gameDetailDto.name;
      });
      this.getEloOverTime();
      this.getGlobalLeaderboard(10);
      this.getCountryLeaderboard(10);
      this.getUserInGlobalLeaderboard(this.currentGameId, this.userId);
      this.getUserInCountryLeaderboard(this.currentGameId, this.userId);
    }
  }

  private getCurrentGameInfo() {
    let gameId = Number(this.route.snapshot.paramMap.get('gameId'));
    if (this.isGameIdInMyGames(gameId)) {
      this.currentGameId = gameId;
      return StatsStatus.USER_HAS_THIS_GAME;
    }
    return StatsStatus.USER_DOES_NOT_HAVE_THIS_GAME;
  }

  private isGameIdInMyGames(gameId: number): boolean {
    if (gameId === null || gameId === 0) return false;
    return this.myGames.find(game => game.id === gameId) !== undefined;
  }

  private returnGameNameIfUserHasAny() {
    if (this.myGames.length > 0) {
      return this.myGames[0].name;
    }
    return "";
  }

  private async getAllGames() {
    return new Promise<void>((resolve, reject) => {
      this.gameService.getAllGames(true).subscribe({
        next: data => {
          this.myGames = data;
          resolve();
        },
        error: err => {
          this.toastService.addError(err)
        }
      });
    })
  }

  private getEloOverTime() {
    this.statisticsService.getEloOverTimeForUser(this.currentGameId).subscribe(
      (eloOverTimeDtoList) => {
        this.linePlotData = Array(1).fill({
          name: "Elo",
          series: eloOverTimeDtoList.map(dto => ({
            name: this.dateToString(dto.finishTime),
            value: Math.round(dto.ratingAfter)
          }))
        });
        const min = eloOverTimeDtoList.reduce((prev, curr) => prev.ratingAfter < curr.ratingAfter ? prev : curr).ratingAfter
        const max = eloOverTimeDtoList.reduce((prev, curr) => prev.ratingAfter > curr.ratingAfter ? prev : curr).ratingAfter
        this.yScaleMin = min - (max - min);
      }
    );
  }

  getGlobalLeaderboard(count: number): void {
    this.statisticsService.getLeaderboard(this.currentGameId, undefined, count).subscribe(
      data => {
        this.globalLeaderboard = data
        this.showUserSeparatelyGlobal = !this.globalLeaderboard.map(e => e.userId).includes(this.userId);
        this.getRankDistribution(this.currentGameId);
      })
  }

  getCountryLeaderboard(count: number): void {
    this.userService.getExtendedUserbyId(this.userId).subscribe(
      data => {
        this.countryCode = data.countryCode;
        this.statisticsService.getLeaderboard(this.currentGameId, this.countryCode, count).subscribe(
          data => {
            this.countryLeaderboard = data
            this.showUserSeparatelyCountry = !this.countryLeaderboard.map(e => e.userId).includes(this.userId);
          })
      }
    )
  }

  getUserInGlobalLeaderboard(gameId: number, userId: number) {
    this.statisticsService.getUserInLeaderboard(gameId, userId, false).subscribe(
      data => {
        this.userInGlobalLeaderboard = data
        if (data == null) {
          this.showUserSeparatelyGlobal = false;
        }
      }
    );
  }

  getUserInCountryLeaderboard(gameId: number, userId: number) {
    this.statisticsService.getUserInLeaderboard(gameId, userId, true).subscribe(
      data => {
        this.userInCountryLeaderboard = data
        if (data == null) {
          this.showUserSeparatelyCountry = false;
        }
      }
    );
  }

  getRankDistribution(gameId: number) {
    this.statisticsService.getRankDistribution(gameId).subscribe(
      data => {
        this.rankDistributionForNgx = data.rankDistribution.map((e) => {
          return {
            name: e.first,
            value: this.truncate(e.second)
          }
        });
        this.rankDistribution = data;
        this.distributionColors = this.customColors();
      }
    )
  }

  public calculatePercentage(): string {
    let percentage = 0;
    if (!this.rankDistribution) return "";
    for (let i = 0; i < this.rankDistribution.rankDistribution.length; i++) {
      percentage += this.rankDistribution.rankDistribution[i].second;
      if (this.rankDistribution.rankDistribution[i].first > this.userInGlobalLeaderboard.rating) {
        return this.truncate(percentage);
      }
    }
    return this.truncate(percentage)
  }

  public getTotalPlayersOfThisGame(): string {
    if (!this.rankDistribution) return "";
    return this.rankDistribution.totalPlayers.toString();
  }

  private truncate(number: number): string {
    return (Math.round(number * 100) / 100).toFixed(2);
  }

  private customColors() {
    return this.rankDistribution.rankDistribution.map(e => {
      if (e.first <= this.userInGlobalLeaderboard?.rating) {
        return {
          name: e.first.toString(),
          value: "#DF005E"
        }
      } else {
        return {
          name: e.first.toString(),
          value: "#7e7e7e"
        }
      }
    })
  }

  dateToString(date: any) {
    return date[2] + ". " + date[1] + ". " + date[0];
  }

  public routeToGame(newGameName: string): void {
    const game = this.myGames.find(game => game.name === newGameName.toString());
    if (!game) {
      return
    }
    this.currentGameId = game.id;
    this.statsStatus = StatsStatus.LOADING;
    this.router.navigate(['statistics/' + this.currentGameId]).then(re => {
      location.reload();
    })
  }

  protected readonly StatsStatus = StatsStatus;
}
