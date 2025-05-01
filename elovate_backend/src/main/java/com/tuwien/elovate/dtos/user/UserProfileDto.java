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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserProfileDto extends UserResponseDto {

    private boolean isFriend;

    private boolean isRequested;

    private boolean isRequesting;

    private boolean isSelf;

    private boolean isBlocked;

    public UserProfileDto(UserResponseDto userResponseDto) {
        super(userResponseDto.getId(), userResponseDto.getEmail(), userResponseDto.getNickName());
    }
}
