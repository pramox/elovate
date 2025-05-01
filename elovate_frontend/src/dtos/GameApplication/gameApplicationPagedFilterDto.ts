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
import {PagedRequestDto} from "../pagedRequestDto";
import {GameApplicationStatus} from "../enums/gameApplicationStatus";

export class GameApplicationPagedFilterDto extends PagedRequestDto {
  status: GameApplicationStatus;
  developerId: number;
}
