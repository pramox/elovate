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
import {DeveloperDto} from "./developerDto";

export interface GameDetailDto {
  id: number,
  name: string,
  genre: Genre,
  playersPerTeam: number,
  image: string | null,
  developerDetails: DeveloperDto;
}
