package com.tuwien.elovate.controllers.lobby;

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
import com.tuwien.elovate.services.lobby.LobbyService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller class for managing lobby-related operations.
 * This class provides endpoints for finding lobbies by ID, finding lobbies for a specific user, and creating new lobbies.
 * Endpoints are secured with role-based access control to ensure that only authorized users can perform certain operations.
 */
@RestController
@RequestMapping("/api/v1/lobby")
@RequiredArgsConstructor
public class LobbyController {

    private final LobbyService lobbyService;
    private static final Logger log = LoggerFactory.getLogger(LobbyController.class);

    /**
     * Finds a lobby by its ID.
     *
     * @param id The ID of the lobby to retrieve.
     * @return ResponseEntity containing the LobbyDto for the specified lobby.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<LobbyDto> findLobbyById(@PathVariable("id") String id) {
        log.info("GET request for /api/v1/lobby/:id received with id: " + id);
        return new ResponseEntity<>(lobbyService.findLobbyById(id), HttpStatus.OK);
    }

    /**
     * Finds lobbies for a specific user.
     *
     * @param userId The ID of the user to retrieve lobbies for.
     * @return ResponseEntity containing a list of LobbyDto for the user's lobbies.
     */
    @PreAuthorize("#userId == authentication.principal.id")
    @GetMapping("/user/{id}")
    public ResponseEntity<List<LobbyDto>> findLobbyForUser(@PathVariable("id") @AuthenticationPrincipal Long userId) {
        log.info("GET request for /api/v1/lobby/user/:id received with id: " + userId);
        return new ResponseEntity<>(lobbyService.findLobbyForUser(userId), HttpStatus.OK);
    }

    /**
     * Creates a new lobby.
     *
     * @param lobbyCreationDto The LobbyCreationDto containing details for creating the new lobby.
     * @return ResponseEntity containing the LobbyDto for the created lobby.
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<LobbyDto> createLobby(@RequestBody LobbyCreationDto lobbyCreationDto) {
        log.info("POST request for /api/v1/lobby/create");
        return new ResponseEntity<>(lobbyService.createLobby(lobbyCreationDto), HttpStatus.CREATED);
    }

    /**
     * Checks the validity of a lobby based on its activity. Endpoint used for polling lobby information
     *
     * @param lobbyId The ID of the lobby to check for activity.
     * @return ResponseEntity containing a boolean indicating whether the lobby is active or not.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/activity/{lobbyId}")
    public ResponseEntity<Boolean> checkForLobbyValidity(@PathVariable String lobbyId) {
        log.info("GET request for /api/v1/lobby/activity/{lobbyId} received with lobby id: {}", lobbyId);
        return new ResponseEntity<>(lobbyService.isLobbyActive(lobbyId), HttpStatus.OK);
    }

    /**
     * Retrieves information about a finished game associated with the specified lobby.
     *
     * @param lobbyId The ID of the lobby associated with the finished game.
     * @return ResponseEntity containing a {@link FinishedGameDto} with details of the finished game.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/finished-game/{lobbyId}")
    public ResponseEntity<FinishedGameDto> getFinishedGameByLobby(@PathVariable String lobbyId) {
        log.info("GET request for /api/v1/lobby/finished-game/{lobbyId} received with lobby id: {}", lobbyId);
        return new ResponseEntity<>(lobbyService.getFinishedGameByLobbyId(lobbyId), HttpStatus.OK);
    }
}
