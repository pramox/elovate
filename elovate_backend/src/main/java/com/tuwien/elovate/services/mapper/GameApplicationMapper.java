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

import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDetailDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CommentMapper.class)
public interface GameApplicationMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "genre", source = "genre")
    @Mapping(target = "drawPossible", source = "drawPossible")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "developerId", source = "developer.id")
    @Mapping(target = "gameId", source = "game.id")
    GameApplicationResponseDto gameApplicationToGameApplicationResponseDTO(GameApplication gameApplication);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "genre", source = "genre")
    @Mapping(target = "drawPossible", source = "drawPossible")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "developerId", source = "developer.id")
    @Mapping(target = "comments", source = "comments")
    @Mapping(target = "gameId", source = "game.id")
    GameApplicationResponseDetailDto gameApplicationToGameApplicationResponseDetailDTO(GameApplication gameApplication);

    default String map(Image value) {
        if (value == null) {
            return null;
        }
        return value.getImageBase64();
    }
}
