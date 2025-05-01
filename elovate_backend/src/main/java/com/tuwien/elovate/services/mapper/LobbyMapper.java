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

import com.tuwien.elovate.dtos.external.CustomStatDto;
import com.tuwien.elovate.dtos.game.GameDto;
import com.tuwien.elovate.dtos.lobby.LobbyDto;
import com.tuwien.elovate.dtos.lobby.PlayerStatsPostGameDto;
import com.tuwien.elovate.dtos.user.UserDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.stats.CustomStat;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class LobbyMapper {

    @Autowired
    protected GameAccessRepository gameAccessRepository;

    @Mapping(target = "teamA", expression = "java(mapTeamToId(lobby.getTeamA(), lobby.getGame()))")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "teamB", expression = "java(mapTeamToId(lobby.getTeamB(), lobby.getGame()))")
    @Mapping(target = "game", source = "game", qualifiedByName = "mapGameToDto")
    public abstract LobbyDto mapLobbyToDto(Lobby lobby);

    @Mapping(target = "user", source = "user", qualifiedByName = "mapUserToDto")
    public abstract PlayerStatsPostGameDto mapPlayerStatsPostGameToPlayerStatsPostGameDto(PlayerStatsPostGame playerStatsPostGame);

    public abstract CustomStatDto mapCustomStatToCustomStatDto(CustomStat customStat);

    @Named("mapGameToDto")
    public GameDto mapGameToId(Game game) {
        try {
            if (game == null) {
                return null;
            }
            final Image image = game.getGameApplication() != null ? game.getGameApplication().getImage() : null;
            return new GameDto(
                    game.getId(),
                    game.getName(),
                    image != null ? Base64.getEncoder().encodeToString(image.getBytes()) : null);
        } catch (IOException e) {
            return null;
        }
    }

    @Named("mapTeamToId")
    public List<UserDto> mapTeamToId(List<User> users, Game game) {
        List<GameAccess> gameAccesses = gameAccessRepository.findByUser_IdInAndGame_Id(users.stream().map(User::getId).collect(Collectors.toList()), game.getId());
        return users.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getNickName(),
                        gameAccesses.stream().filter(ac -> ac.getUser().equals(user)).findFirst().get().getRating().getRating(),
                        user.getCountryCode()))
                .collect(Collectors.toList());
    }

    @Named("mapUserToDto")
    public UserDto mapUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setCountryCode(user.getCountryCode());
        userDto.setName(user.getNickName());
        return userDto;
    }
}
