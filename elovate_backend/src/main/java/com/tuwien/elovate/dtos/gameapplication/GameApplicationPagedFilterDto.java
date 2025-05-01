package com.tuwien.elovate.dtos.gameapplication;

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

import com.tuwien.elovate.dtos.helpers.PageableRequestDto;
import com.tuwien.elovate.enums.GameApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GameApplicationPagedFilterDto extends PageableRequestDto {

    @NotNull(message = "GAME_APPLICATION_HAS_INVALID_STATUS")
    private GameApplicationStatus status;

    private Long developerId;

}
