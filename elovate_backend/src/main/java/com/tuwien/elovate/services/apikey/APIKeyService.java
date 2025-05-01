package com.tuwien.elovate.services.apikey;

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
import com.tuwien.elovate.entities.apikey.ApiKey;
import com.tuwien.elovate.entities.apikey.ApiKeyId;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.repositories.apikey.APIKeyRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.services.session.SessionUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class APIKeyService {

    private final APIKeyRepository apiKeyRepository;
    private final GameRepository gameRepository;
    private final SessionUtils sessionUtils;

    /**
     * Generates an API key for the specified game, provided the authenticated user is the developer of the game. Hash used is SHA256
     *
     * @param gameId The ID of the game for which the API key is generated.
     * @return ApiKeyDto containing the generated API key details.
     * @throws EntityNotFoundException     if the specified game is not found.
     * @throws InvalidCredentialsException if the authenticated user is not authorized to create an API key for the game.
     */
    public ApiKeyDto generateAPIKey(Long gameId) {
        User active = sessionUtils.getActiveUser();
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
        if (!Objects.equals(game.getGameApplication().getDeveloper().getId(), active.getId())) {
            throw new InvalidCredentialsException(Message.NOT_AUTHORIZED_TO_CREATE_API_KEY);

        }
        ApiKeyDto apiKeyDTO = new ApiKeyDto();
        String plainApiKeyString = UUID.randomUUID().toString();
        String hashedApiKeyString = DigestUtils.sha256Hex(plainApiKeyString);
        ApiKeyId apiKeyId = new ApiKeyId(gameId, active.getId());
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKeyRepository::delete);

        Instant instant = Instant.now();
        ApiKey apiKey = new ApiKey(apiKeyId, hashedApiKeyString, instant);
        apiKeyRepository.save(apiKey);

        apiKeyDTO.setKey(plainApiKeyString);
        apiKeyDTO.setGameId(gameId);
        apiKeyDTO.setTimestamp(LocalDateTime.ofInstant(instant, ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString());

        return apiKeyDTO;
    }

    public void deactivateAPIKey(Long gameId) {
        User active = sessionUtils.getActiveUser();
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
        if (!Objects.equals(game.getGameApplication().getDeveloper().getId(), active.getId())) {
            throw new InvalidCredentialsException(Message.NOT_AUTHORIZED_TO_DELETE_API_KEY);

        }
        ApiKeyId apiKeyId = new ApiKeyId(gameId, active.getId());
        apiKeyRepository.findById(apiKeyId).ifPresent(apiKeyRepository::delete);
    }

    public boolean validateAPIKey(Long gameId, Long userId, String key) {
        final ApiKeyId apiKeyId = new ApiKeyId(gameId, userId);
        final Optional<ApiKey> apiKey = apiKeyRepository.findById(apiKeyId);
        return apiKey.isPresent() && DigestUtils.sha256Hex(key).equals(apiKey.get().getKey());
    }

    public boolean validateAPIKey(final String key) {
        final Optional<ApiKey> apiKey = apiKeyRepository.findByKey(DigestUtils.sha256Hex(key));
        return apiKey.isPresent();
    }

    public boolean validateAPIKey(final String key, final Long gameId) {
        final Optional<ApiKey> apiKey = apiKeyRepository.findByKey(DigestUtils.sha256Hex(key));
        return apiKey.isPresent() && gameId.equals(apiKey.get().getId().getGameId());
    }

    public ApiKeyDto getApiKeyHash(Long gameId) {
        User active = sessionUtils.getActiveUser();
        Game game = gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
        if (!Objects.equals(game.getGameApplication().getDeveloper().getId(), active.getId())) {
            throw new InvalidCredentialsException(Message.NOT_AUTHORIZED_TO_RETRIEVE_API_KEY);

        }
        ApiKeyId apiKeyId = new ApiKeyId(gameId, active.getId());
        Optional<ApiKey> apiKey = apiKeyRepository.findById(apiKeyId);
        if (apiKey.isEmpty()) {
            throw new EntityNotFoundException(Message.NO_API_KEY_FOUND);
        }

        ApiKeyDto apiKeyDTO = new ApiKeyDto();
        apiKeyDTO.setKey(apiKey.get().getKey());
        apiKeyDTO.setGameId(gameId);
        apiKeyDTO.setTimestamp(LocalDateTime.ofInstant(apiKey.get().getTimestamp(), ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString());
        return apiKeyDTO;
    }

}

