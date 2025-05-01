package com.tuwien.elovate.controllers.gameapplication;

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

import com.tuwien.elovate.dtos.gameapplication.GameApplicationContractDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationPagedFilterDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationRequestDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDetailDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponsePagedDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationUpdateRequestDto;
import com.tuwien.elovate.dtos.gameapplication.comment.CommentRequestDto;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.services.gameapplication.GameApplicationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing game applications.
 * This class provides endpoints for retrieving game application contracts, adding new game applications,
 * retrieving all game applications of an active user, retrieving details of a specific game application,
 * updating the status of a game application, and adding comments to a game application.
 * Endpoints are secured with role-based access control to ensure that only authorized users can perform certain operations.
 */
@RestController
@RequestMapping("/api/v1/game-applications")
@AllArgsConstructor
public class GameApplicationController {

    private final GameApplicationService gameApplicationService;
    private static final Logger log = LoggerFactory.getLogger(GameApplicationController.class);

    /**
     * Retrieves the game application contract.
     *
     * @return ResponseEntity containing the GameApplicationContractDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER')")
    @GetMapping("/getGameApplicationContract")
    public ResponseEntity<GameApplicationContractDto> getGameApplicationContract() {
        log.info("GET request for /api/v1/game-applications received");
        return new ResponseEntity<>(gameApplicationService.getGameApplicationContract(), HttpStatus.OK);
    }

    /**
     * Adds a new game application.
     *
     * @param gameApplicationRequestDto The GameApplicationRequestDto containing details of the new game application.
     * @return ResponseEntity containing the GameApplicationResponseDto for the added game application.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<GameApplicationResponseDto> addGameApplication(@Valid GameApplicationRequestDto gameApplicationRequestDto) {
        log.info("POST request for /api/v1/game-applications/multipart/form-data received with name: {}", gameApplicationRequestDto.getName());
        return new ResponseEntity<>(gameApplicationService.save(gameApplicationRequestDto), HttpStatus.CREATED);
    }

    /**
     * Retrieves all game applications of an active user, optionally filtered.
     *
     * @param filter The GameApplicationPagedFilterDto containing filter parameters.
     * @return ResponseEntity containing the GameApplicationResponsePagedDto for the requested game applications.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<GameApplicationResponsePagedDto> getAllGameApplicationsOfActiveUser(@Valid GameApplicationPagedFilterDto filter) {
        log.info("GET request for /api/v1/game-applications received with developerId: {}", filter.getDeveloperId());
        return new ResponseEntity<>(gameApplicationService.getAllGameApplicationsForActiveUser(filter), HttpStatus.OK);
    }

    /**
     * Retrieves details of a specific game application by ID.
     *
     * @param id The ID of the game application to retrieve.
     * @return ResponseEntity containing the GameApplicationResponseDetailDto for the specified game application.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<GameApplicationResponseDetailDto> getGameApplicationById(@PathVariable Long id) {
        log.info("GET request for /api/v1/game-applications/{id} received with id {}", id);
        return new ResponseEntity<>(gameApplicationService.getGameApplicationById(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("getGameByGameApplicationId/{id}")
    public ResponseEntity<Long> getGameIdByGameApplicationId(@PathVariable Long id) {
        log.info("GET request for /api/v1/game-applications/getGameByGameApplicationId/{id} received with id {}", id);
        return new ResponseEntity<>(gameApplicationService.getGameByGameApplicationId(id), HttpStatus.OK);
    }

    /**
     * Updates the status of a specific game application by ID.
     *
     * @param id                              The ID of the game application to update.
     * @param gameApplicationUpdateRequestDTO The GameApplicationUpdateRequestDto containing the updated status.
     * @return ResponseEntity containing the GameApplicationResponseDto for the updated game application.
     * @throws ValidationException if the update request is invalid.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<GameApplicationResponseDto> updateGameApplicationStatus(
            @PathVariable Long id, @Valid GameApplicationUpdateRequestDto gameApplicationUpdateRequestDTO) throws ValidationException {
        log.info("PUT request for /api/v1/game-applications/multipart/form-data received with status {}", gameApplicationUpdateRequestDTO.getStatus().toString());
        return new ResponseEntity<>(gameApplicationService.updateGameApplicationStatusById(id, gameApplicationUpdateRequestDTO),
                HttpStatus.OK);
    }

    /**
     * Adds a comment to a specific game application by ID.
     *
     * @param id                The ID of the game application to comment on.
     * @param commentRequestDTO The CommentRequestDto containing the comment content.
     * @return ResponseEntity containing the GameApplicationResponseDetailDto for the updated game application with the added comment.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping(value = "/{id}/comment", consumes = {"multipart/form-data"})
    public ResponseEntity<GameApplicationResponseDetailDto> commentOnGameApplication(
            @PathVariable Long id, @Valid CommentRequestDto commentRequestDTO) {
        log.info("PUT request for /api/v1/game-applications/{id}/comment received with comment {}", commentRequestDTO.getCommentContent());
        return new ResponseEntity<>(gameApplicationService.commentOnGameApplication(id, commentRequestDTO),
                HttpStatus.OK);
    }
}
