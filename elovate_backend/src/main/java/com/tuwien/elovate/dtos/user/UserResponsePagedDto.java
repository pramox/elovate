package com.tuwien.elovate.dtos.user;

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
public class UserResponsePagedDto extends PagedDto<UserResponseDto> {

    public UserResponsePagedDto(Page<UserResponseDto> page) {
        super(page);
    }
}

