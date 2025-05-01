package com.tuwien.elovate.controllers.game;

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
import com.tuwien.elovate.dtos.game.GameDetailDto;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.services.game.GameService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller class for managing game-related operations.
 * This class provides endpoints for retrieving game details, getting a list of games, and handling game access requests.
 * Endpoints are secured with role-based access control to ensure that only authorized users can perform certain operations.
 */
@RestController
@RequestMapping("/api/v1/games")
@AllArgsConstructor
public class GameController {

    private final GameService gameService;
    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    /**
     * Retrieves detailed information about a specific game by ID.
     *
     * @param id The ID of the game to retrieve.
     * @return ResponseEntity containing the GameDetailDto for the specified game.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GameDetailDto> getGameById(@PathVariable Long id) {
        log.info("GET request for /api/v1/games/{id} received with id: {}", id);
        return new ResponseEntity<>(gameService.getGameDtoById(id), HttpStatus.OK);
    }

    /**
     * Retrieves a list of games, optionally filtered by user ID and activation status.
     *
     * @param userId    Optional parameter for filtering games by user ID.
     * @param activated Optional parameter for filtering games by activation status.
     * @return ResponseEntity containing a list of GameDetailDto for the requested games.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping()
    @Transactional
    public ResponseEntity<List<GameDetailDto>> getAll(@RequestParam(required = false) Long userId,
                                                      @RequestParam(required = false) Boolean activated) {
        log.info("GET request for /api/v1/games received for user with id: {}", userId);
        List<GameDetailDto> result = gameService.getAll(userId, activated).toList();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Requests access to a specific game.
     *
     * @param gameAccessDTO The GameAccessDto containing user and game information.
     * @return ResponseEntity containing the GameAccessDto with access details.
     * @throws ValidationException if the access request is invalid.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/access")
    public ResponseEntity<GameAccessDto> getGameAccessId(@RequestBody GameAccessDto gameAccessDTO) throws ValidationException {
        log.info("POST request for /api/v1/games/access received for user with id: {} and game: {}", gameAccessDTO.getUser(), gameAccessDTO.getGame());
        return new ResponseEntity<>(gameService.getGameAccessId(gameAccessDTO), HttpStatus.OK);
    }
}
