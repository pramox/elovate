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

import com.tuwien.elovate.dtos.game.GameAccessDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.repositories.game.GameRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class GameAccessMapper {

    @Autowired
    protected GameRepository gameRepository;

    @Mapping(target = "user", source = "user", qualifiedByName = "mapUserToId")
    @Mapping(target = "game", source = "game", qualifiedByName = "mapGameToId")
    @Mapping(target = "gameName", source = "game", qualifiedByName = "mapGameToGameName")
    public abstract GameAccessDto gameAccessToGameAccessDto(GameAccess gameAccess);

    @Mapping(target = "user", source = "user", qualifiedByName = "mapIdToUser")
    @Mapping(target = "game", source = "game", qualifiedByName = "mapIdToGame")
    public abstract GameAccess gameAccessDtoToGameAccess(GameAccessDto gameAccessDTO);

    @Named("mapUserToId")
    Long mapUserToId(User user) {
        return user.getId();
    }

    @Named("mapIdToUser")
    User mapIdToUser(Long userId) {
        User user = new User();
        user.setId(userId);
        return user;
    }

    @Named("mapGameToId")
    Long mapGameToId(Game game) {
        return game.getId();
    }

    @Named("mapIdToGame")
    Game mapIdToGame(Long gameId) {
        Game game = new Game();
        game.setId(gameId);
        Optional<Game> gameOptional = gameRepository.findById(gameId);
        gameOptional.ifPresent(value -> game.setName(value.getName()));
        return game;
    }

    @Named("mapGameToGameName")
    String mapGameToGameName(Game game) {
        return game.getName();
    }
}
