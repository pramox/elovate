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

import com.tuwien.elovate.dtos.game.GameDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface GameMapper {

    @Mapping(target = "image", source = "image", qualifiedByName = "mapImageToString")
    GameDto gameToGameDto(Game game);

    @Named("mapImageToString")
    default String map(Image value) {
        if (value == null) {
            return null;
        }
        return value.getImageBase64();
    }
}
