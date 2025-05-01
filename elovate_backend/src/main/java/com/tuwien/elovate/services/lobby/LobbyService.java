package com.tuwien.elovate.services.lobby;

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

import com.tuwien.elovate.dtos.lobby.FinishedGameDto;
import com.tuwien.elovate.dtos.lobby.LobbyCreationDto;
import com.tuwien.elovate.dtos.lobby.LobbyDto;
import com.tuwien.elovate.dtos.lobby.SimpleLobbyDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.lobby.LobbyRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.mapper.GameMapper;
import com.tuwien.elovate.services.mapper.LobbyMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LobbyService {

    private static final Logger log = LoggerFactory.getLogger(LobbyService.class);
    private final LobbyRepository lobbyRepository;
    private final LobbyMapper lobbyMapper;
    private final GameMapper gameMapper;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final FinishedGameRepository finishedGameRepository;

    public LobbyDto findLobbyById(final String id) {
        return lobbyMapper.mapLobbyToDto(
                lobbyRepository
                        .findById(id)
                        .orElseThrow(() -> new EntityNotFoundException(Message.LOBBY_NOT_FOUND)));
    }

    public List<SimpleLobbyDto> findLobbiesByGameId(final Long gameId) {
        final Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
        return lobbyRepository
                .findLobbiesByGame(game)
                .stream()
                .map(lobby -> new SimpleLobbyDto(
                        lobby.getId(),
                        lobby.getGame().getId(),
                        lobby.getTeamA().stream().map(user -> new UserResponseDto(user.getId(), user.getEmail(), user.getNickName())).collect(Collectors.toList()),
                        lobby.getTeamB().stream().map(user -> new UserResponseDto(user.getId(), user.getEmail(), user.getNickName())).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public LobbyDto createLobby(final LobbyCreationDto lobbyCreationDto) {
        StringBuilder logInfo = new StringBuilder();
        logInfo.append("Lobby created with: \t\tTeam A [");
        for (Long id : lobbyCreationDto.getTeamA()) {
            logInfo.append(" ").append(id);
        }
        logInfo.append(" ] <-vs-> [");
        for (Long id : lobbyCreationDto.getTeamB()) {
            logInfo.append(" ").append(id);
        }
        logInfo.append(" ] Team B");
        log.info(logInfo.toString());

        final List<User> usersTeamA = new ArrayList<>();
        for (Long id : lobbyCreationDto.getTeamA()) {
            usersTeamA.add(userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND)));
        }

        final List<User> usersTeamB = new ArrayList<>();
        for (Long id : lobbyCreationDto.getTeamB()) {
            usersTeamB.add(userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND)));
        }
        final Game game = gameRepository.findById(lobbyCreationDto.getGameId())
                .orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
        final Lobby lobby = lobbyRepository.save(new Lobby(null, game, usersTeamA, usersTeamB, LocalDateTime.now()));
        return lobbyMapper.mapLobbyToDto(lobby);
    }

    public List<LobbyDto> findLobbyForUser(final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        List<Lobby> lobbies = lobbyRepository.findValidLobbyForUser(user, LocalDateTime.now().minusHours(2));
        return lobbies.stream().map(lobbyMapper::mapLobbyToDto).collect(Collectors.toList());
    }

    public void deleteById(final String id) {
        lobbyRepository.deleteById(id);
    }

    public Boolean isLobbyActive(final String lobbyId) {
        return lobbyRepository.findById(lobbyId).isPresent();
    }

    public FinishedGameDto getFinishedGameByLobbyId(final String lobbyId) {
        Optional<FinishedGame> finishedGame = finishedGameRepository.findByDeletedLobby(lobbyId);
        if (finishedGame.isEmpty()) {
            throw new EntityNotFoundException(Message.LOBBY_NOT_FOUND);
        } else {
            return mapFinishedGameToFinishedGameDto(finishedGame.get());
        }
    }

    private FinishedGameDto mapFinishedGameToFinishedGameDto(FinishedGame finishedGame) {
        FinishedGameDto dto = new FinishedGameDto();
        dto.setGame(gameMapper.gameToGameDto(finishedGame.getGame()));
        dto.setFinishTime(finishedGame.getFinishTime());
        dto.setTeamA(finishedGame.getTeamA().stream().map(lobbyMapper::mapPlayerStatsPostGameToPlayerStatsPostGameDto).collect(Collectors.toList()));
        dto.setTeamB(finishedGame.getTeamB().stream().map(lobbyMapper::mapPlayerStatsPostGameToPlayerStatsPostGameDto).collect(Collectors.toList()));
        dto.setDeletedLobby(finishedGame.getDeletedLobby());
        dto.setGameEndResult(finishedGame.getGameEndResult());
        return dto;
    }
}
