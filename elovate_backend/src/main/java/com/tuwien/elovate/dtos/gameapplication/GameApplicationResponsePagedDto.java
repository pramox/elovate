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

import com.tuwien.elovate.dtos.helpers.PagedDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;


@AllArgsConstructor
public class GameApplicationResponsePagedDto extends PagedDto<GameApplicationResponseDto> {

    public GameApplicationResponsePagedDto(Page<GameApplicationResponseDto> page) {
        super(page);
    }
}
