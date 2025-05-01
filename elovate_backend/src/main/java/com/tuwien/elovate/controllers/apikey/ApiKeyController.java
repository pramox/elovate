package com.tuwien.elovate.controllers.apikey;

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

import com.tuwien.elovate.dtos.apikey.ApiKeyDto;
import com.tuwien.elovate.services.apikey.APIKeyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller class for managing API keys.
 * This class provides endpoints for generating, retrieving, and deleting API keys.
 * Endpoints are secured with role-based access control.
 */
@RestController
@RequestMapping("/api/v1/api-keys")
@AllArgsConstructor
public class ApiKeyController {

    private final APIKeyService apiKeyService;
    private static final Logger log = LoggerFactory.getLogger(ApiKeyController.class);

    /**
     * Generates a new API key for the specified game.
     *
     * @param gameId The ID of the game for which the API key is generated.
     * @return ResponseEntity containing the generated API key DTO.
     */
    @PostMapping("/generateApiKey")
    @PreAuthorize("hasRole('ROLE_GAME_DEVELOPER')")
    public ResponseEntity<ApiKeyDto> generateApiKey(@RequestParam Long gameId) {
        log.info("POST request for /api/v1/api-keys/generateApiKey received with gameId: {}", gameId);
        ApiKeyDto apiKeyDTO = apiKeyService.generateAPIKey(gameId);
        return ResponseEntity.ok(apiKeyDTO);
    }

    /**
     * Retrieves the hash of the API key with the specified ID.
     *
     * @param id The ID of the API key to retrieve.
     * @return ResponseEntity containing the API key DTO with the hash.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_GAME_DEVELOPER')")
    public ResponseEntity<ApiKeyDto> getApiKeyHash(@PathVariable Long id) {
        log.info("GET request for /api/v1/api-keys/{id} received with id: {}", id);
        ApiKeyDto apiKeyDTO = apiKeyService.getApiKeyHash(id);
        return ResponseEntity.ok(apiKeyDTO);
    }

    /**
     * Deactivates the API key with the specified ID.
     *
     * @param id The ID of the API key to deactivate.
     * @return ResponseEntity with no content to indicate a successful deletion.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_GAME_DEVELOPER')")
    public ResponseEntity<Void> deleteApiKey(@PathVariable Long id) {
        log.info("DELETE request for /api/v1/api-keys/{id} received with id: {}", id);
        apiKeyService.deactivateAPIKey(id);
        return ResponseEntity.noContent().build();
    }
}
