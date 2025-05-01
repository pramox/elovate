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

export interface GameApplicationRequestDto {
  name: string,
  genre: Genre,
  drawPossible: boolean,
  playersPerTeam: number,
  image: File | null,
  agreedToContract: boolean
}
