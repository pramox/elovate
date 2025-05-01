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
import {GameDto} from "./Game/gameDto";
import {Time} from "@angular/common";
import {UserDto} from "./userDto";

export interface CustomStat {
  statName: string,
  statValue: string
}

export interface PlayerStatsPostGameDto {
  user: UserDto
  ratingBefore: number
  ratingAfter: number
  customStats: CustomStat[]
}

export interface FinishedGameDto {
  game: GameDto,
  gameEndResult: GameEndResult,
  teamA: PlayerStatsPostGameDto[],
  teamB: PlayerStatsPostGameDto[],
  finishTime: Date,
  deletedLobby: string;
}

export enum GameEndResult {
  TEAM_A_WINNER,
  TEAM_B_WINNER,
  DRAW
}
