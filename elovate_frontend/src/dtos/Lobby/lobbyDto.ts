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
import {GameDto} from "../Game/gameDto";
import {UserDto} from "../userDto";

export interface LobbyDto {
  id: string;
  game: GameDto,
  teamA: UserDto[]
  teamB: UserDto[]
}

