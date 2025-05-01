package com.tuwien.elovate.services.mapper;

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

import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.dtos.user.UserDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.io.IOException;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

    ExtendedUserResponseDto mapUserToExtendedUserResponseDto(User user);

    UserResponseDto mapUserToUserResponseDto(User user);

    User mapExtendedUserResponseDtoToUser(ExtendedUserResponseDto updateDto);

    @Mapping(target = "name", source = "nickName")
    UserDto mapUserToUserDto(User user);

    @AfterMapping
    default void afterExtendedUserResponseDtoToUserMapping(@MappingTarget User user, ExtendedUserResponseDto updateDto) {
        if (updateDto.getImage() != null) {
            try {
                Image image = new Image();
                image.setFileContent(updateDto.getImage().getBytes());
                image.setName(updateDto.getImage().getName());
                image.setOriginalFilename(updateDto.getNickName() + "_" + "image");
                image.setContentType(updateDto.getImage().getContentType());
                user.setImage(image);
            } catch (IOException e) {
                throw new ValidationException(Message.IMAGE_COULD_NOT_BE_SAVED);
            }
        }
    }

    @AfterMapping
    default void afterUserToExtendedUserDtoMapping(User user, @MappingTarget ExtendedUserResponseDto updateDto) {
        if (user.getImage() != null) {
            updateDto.setImage(null);
            updateDto.setImageBase64(user.getImage().getImageBase64());
        }
    }
}
