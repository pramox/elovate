package com.tuwien.elovate.controllers.external;

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

import com.tuwien.elovate.dtos.external.ExternalLobbyProvideDto;
import com.tuwien.elovate.dtos.external.ExternalMatchmakingDto;
import com.tuwien.elovate.dtos.external.ExternalMatchmakingDtoWithParams;
import com.tuwien.elovate.dtos.external.ExternalUserInfoDto;
import com.tuwien.elovate.dtos.external.GameEndInfoDto;
import com.tuwien.elovate.dtos.external.UserUnlockDto;
import com.tuwien.elovate.dtos.lobby.SimpleLobbyDto;
import com.tuwien.elovate.services.external.ExternalGameApiService;
import jakarta.annotation.security.PermitAll;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller class for handling external game API requests.
 * This class provides endpoints for unlocking users, retrieving lobbies, updating ended games, and performing Glicko matchmaking.
 * Endpoints are annotated with {@code @PermitAll} to allow unrestricted access.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/external")
public class ExternalGameApiController {

    private static final Logger log = LoggerFactory.getLogger(ExternalGameApiController.class);
    private final ExternalGameApiService externalGameApiService;

    /**
     * Unlocks a user based on the provided UserUnlockDto.
     *
     * @param userUnlockDto The UserUnlockDto containing the UUID and API key for unlocking the user.
     * @return ResponseEntity containing the ExternalUserInfoDto for the unlocked user.
     */
    @PermitAll
    @PostMapping("/unlock-user")
    public ResponseEntity<ExternalUserInfoDto> unlockUser(@RequestBody UserUnlockDto userUnlockDto) {
        log.info("POST request for /api/v1/external/unlock-user received with UUID: {}", userUnlockDto.getUuid());
        externalGameApiService.validateApiKey(userUnlockDto.getApiKey());
        return new ResponseEntity<>(externalGameApiService.unlockUser(userUnlockDto.getUuid(), userUnlockDto.getApiKey()), HttpStatus.OK);
    }

    /**
     * Retrieves all lobbies for a given game.
     *
     * @param externalLobbyProvideDto The ExternalLobbyProvideDto containing game information and API key for validation.
     * @return ResponseEntity containing a list of LobbyCreationDto for all lobbies of the specified game.
     */
    @PermitAll
    @GetMapping("/lobbies")
    public ResponseEntity<List<SimpleLobbyDto>> getAllLobbies(@RequestBody ExternalLobbyProvideDto externalLobbyProvideDto) {
        log.info("GET request for /api/v1/external/lobbies received for game with Id: {}", externalLobbyProvideDto.getGameId());
        externalGameApiService.validateApiKey(externalLobbyProvideDto.getApiKey(), externalLobbyProvideDto.getGameId());
        return new ResponseEntity<>(externalGameApiService.getAllLobbies(externalLobbyProvideDto.getGameId()), HttpStatus.OK);
    }

    /**
     * Updates information about an ended game in the specified lobby.
     *
     * @param lobbyId        The ID of the lobby for the ended game.
     * @param gameEndInfoDto The GameEndInfoDto containing information about the ended game and API key for validation.
     * @return ResponseEntity with no content to indicate a successful update.
     */
    @PermitAll
    @PostMapping("/game-end/{lobbyId}")
    public ResponseEntity<Void> updateEndedGame(@PathVariable @NonNull String lobbyId,
                                                @RequestBody @NonNull GameEndInfoDto gameEndInfoDto) {
        log.info("POST request for /api/v1/external/game-end/:lobbyId received with id: {}", lobbyId);
        externalGameApiService.validateApiKey(gameEndInfoDto.getApiKey(), gameEndInfoDto.getGameId());
        externalGameApiService.updateUsersRatings(lobbyId, gameEndInfoDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Performs Glicko matchmaking based on the provided game information.
     *
     * @param gameInfo The ExternalMatchmakingDto containing information for Glicko matchmaking.
     * @return ResponseEntity containing the result of Glicko matchmaking.
     */
    @PermitAll
    @GetMapping("/matchmaking/glicko")
    public ResponseEntity<ExternalMatchmakingDto> performGlickoMatchmaking(@RequestBody ExternalMatchmakingDto gameInfo) {
        log.info("POST request for /api/v1/external/matchmaking/glicko");
        return new ResponseEntity<>(externalGameApiService.performGlickoMatchmaking(gameInfo), HttpStatus.OK);
    }

    /**
     * Performs Glicko matchmaking with custom parameters.
     *
     * @param info The ExternalMatchmakingDtoWithParams containing game information and custom parameters for Glicko matchmaking.
     * @return ResponseEntity containing the result of Glicko matchmaking with custom parameters.
     */
    @PermitAll
    @GetMapping("/matchmaking/glicko-params")
    public ResponseEntity<ExternalMatchmakingDto> performGlickoMatchmakingWithCustomParams(@RequestBody ExternalMatchmakingDtoWithParams info) {
        log.info("POST request for /api/v1/external/matchmaking/glicko-params");
        return new ResponseEntity<>(externalGameApiService.performGlickoMatchmaking(info.getInfo(), info.getParams()), HttpStatus.OK);
    }
}
