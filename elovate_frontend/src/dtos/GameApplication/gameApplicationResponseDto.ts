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
import {Genre} from "../enums/genre";
import {GameApplicationStatus} from "../enums/gameApplicationStatus";

export interface GameApplicationResponseDto {
  id: number,
  name: string,
  genre: Genre,
  drawPossible: boolean,
  playersPerTeam: number,
  image: string | null,
  status: GameApplicationStatus,
  developerId: number;
  gameId: number;
}
