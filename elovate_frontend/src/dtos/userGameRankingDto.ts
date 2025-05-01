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
import {UserResponseDto} from "./userResponseDto";
import {GameDto} from "./Game/gameDto";

export class UserGameRankingDto {
  user: UserResponseDto;
  game: GameDto;
  ranking: number;
}
