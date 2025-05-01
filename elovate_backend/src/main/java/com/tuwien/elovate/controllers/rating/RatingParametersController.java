package com.tuwien.elovate.controllers.rating;

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

import com.tuwien.elovate.dtos.rating.Glicko2RatingParametersDto;
import com.tuwien.elovate.services.rating.RatingParameterService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing rating parameters.
 * This class provides endpoints for retrieving and updating Glicko2 rating parameters for a specific game.
 * Endpoints are secured with role-based access control to ensure that only authorized users can perform certain operations.
 */
@RestController
@RequestMapping("/api/v1/rating-parameters")
@AllArgsConstructor
public class RatingParametersController {

    private final RatingParameterService ratingParameterService;
    private static final Logger log = LoggerFactory.getLogger(RatingParametersController.class);

    /**
     * Retrieves Glicko2 rating parameters for a specific game.
     *
     * @param gameId The ID of the game for which to retrieve rating parameters.
     * @return Glicko2RatingParametersDto containing the Glicko2 rating parameters for the specified game.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_ADMIN')")
    @GetMapping("/glicko2/{gameId}")
    public Glicko2RatingParametersDto getGlicko2RatingParametersForGame(@PathVariable Long gameId) {
        log.info("GET request for /api/v1/rating-parameters/glicko2/{gameId} received with id: {}", gameId);
        return ratingParameterService.getGlicko2RatingParametersForGame(gameId);
    }

    /**
     * Updates Glicko2 rating parameters for a specific game.
     *
     * @param gameId The ID of the game for which to update rating parameters.
     * @param dto    The Glicko2RatingParametersDto containing the updated rating parameters.
     * @return Glicko2RatingParametersDto containing the updated Glicko2 rating parameters for the specified game.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_ADMIN')")
    @PutMapping("/glicko2/{gameId}")
    public Glicko2RatingParametersDto updateGlicko2RatingParametersForGame(@PathVariable Long gameId, @RequestBody Glicko2RatingParametersDto dto) {
        log.info("PUT request for /api/v1/rating-parameters/glicko2/{gameId} received with id: {}", gameId);
        return ratingParameterService.updateGlicko2RatingParametersForGame(gameId, dto);
    }
}
