package com.tuwien.elovate.services;

import com.tuwien.elovate.TestData;
import com.tuwien.elovate.dtos.apikey.ApiKeyDto;
import com.tuwien.elovate.entities.apikey.ApiKey;
import com.tuwien.elovate.entities.apikey.ApiKeyId;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.repositories.apikey.APIKeyRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.services.apikey.APIKeyService;
import com.tuwien.elovate.services.session.SessionUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class APIKeyServiceUnitTest {

    @Mock
    private APIKeyRepository apiKeyRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private SessionUtils sessionUtils;

    @InjectMocks
    private APIKeyService apiKeyService;

    private User mockDeveloper;

    private Game mockGame;

    private ApiKey apiKey;

    @BeforeEach
    void setup() {
        this.mockDeveloper = User.builder()
                .id(1L)
                .nickName("developer")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();

        this.mockGame = Game.builder()
                .id(1L)
                .name("game")
                .gameApplication(GameApplication.builder()
                        .developer(mockDeveloper)
                        .drawPossible(false)
                        .playersPerTeam(5).build())
                .drawPossible(false)
                .playersPerTeam(5).build();

        this.apiKey = ApiKey.builder()
                .id(new ApiKeyId(mockGame.getId(), mockDeveloper.getId()))
                .key(DigestUtils.sha256Hex(UUID.randomUUID().toString()))
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void generate_apiKey_successful() {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));
        when(apiKeyRepository.findById(any())).thenReturn(Optional.empty());

        ApiKeyDto apiKeyDTO = apiKeyService.generateAPIKey(mockGame.getId());

        verify(gameRepository, times(1)).findById(mockGame.getId());
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
        verify(apiKeyRepository, times(1)).findById(any(ApiKeyId.class));

        assertNotNull(apiKeyDTO);
        assertEquals(mockGame.getId(), apiKeyDTO.getGameId());
        assertNotNull(apiKeyDTO.getKey());
        assertEquals(TestData.UUID_4_LENGTH, apiKeyDTO.getKey().length());
    }

    @Test
    void generate_apiKey_deletes_old_key_successful() {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));
        when(apiKeyRepository.findById(any())).thenReturn(Optional.of(apiKey));

        ApiKeyDto apiKeyDTO = apiKeyService.generateAPIKey(mockGame.getId());

        verify(gameRepository, times(1)).findById(mockGame.getId());
        verify(apiKeyRepository, times(1)).save(any(ApiKey.class));
        verify(apiKeyRepository, times(1)).delete(apiKey);
        verify(apiKeyRepository, times(1)).findById(any(ApiKeyId.class));

        assertNotNull(apiKeyDTO);
        assertEquals(mockGame.getId(), apiKeyDTO.getGameId());
        assertNotNull(apiKeyDTO.getKey());
        assertEquals(TestData.UUID_4_LENGTH, apiKeyDTO.getKey().length());
    }

    @Test
    void generate_apiKey_for_different_user_throws_unauthorizedException() {

        User developer = User.builder()
                .id(2L)
                .nickName("developer")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();

        when(sessionUtils.getActiveUser()).thenReturn(developer);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));

        Long id = mockGame.getId();
        assertThrows(InvalidCredentialsException.class, () -> apiKeyService.generateAPIKey(id));
    }

    @Test
    void generate_apiKey_for_unknown_game_throws_entityNotFoundException() {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.empty());

        Long id = mockGame.getId();
        assertThrows(EntityNotFoundException.class, () -> apiKeyService.generateAPIKey(id));
    }

    @Test
    void get_apiKeyHash_successful() {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);

        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));

        when(apiKeyRepository.findById(any())).thenReturn(Optional.of(apiKey));

        ApiKeyDto apiKeyDTO = apiKeyService.getApiKeyHash(mockGame.getId());

        verify(gameRepository, times(1)).findById(mockGame.getId());
        verify(apiKeyRepository, times(1)).findById(any(ApiKeyId.class));

        assertNotNull(apiKeyDTO);
        assertEquals(mockGame.getId(), apiKeyDTO.getGameId());
        assertNotNull(apiKeyDTO.getKey());
        assertNotEquals(TestData.UUID_4_LENGTH, apiKeyDTO.getKey().length());
    }


    @Test
    void get_apiKeyHash_for_different_user_throws_unauthorizedException() {

        User developer = User.builder()
                .id(2L)
                .nickName("developer")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();

        when(sessionUtils.getActiveUser()).thenReturn(developer);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));

        Long id = mockGame.getId();
        assertThrows(InvalidCredentialsException.class, () -> apiKeyService.getApiKeyHash(id));
    }

    @Test
    void deactivate_apiKey_for_different_user_throws_unauthorizedException() {

        User developer = User.builder()
                .id(2L)
                .nickName("developer")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();

        when(sessionUtils.getActiveUser()).thenReturn(developer);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));

        Long id = mockGame.getId();
        assertThrows(InvalidCredentialsException.class, () -> apiKeyService.deactivateAPIKey(id));
    }

    @Test
    void deactivate_apiKey_successful() {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);
        when(gameRepository.findById(mockGame.getId())).thenReturn(Optional.of(mockGame));
        when(apiKeyRepository.findById(any())).thenReturn(Optional.of(apiKey));

        apiKeyService.deactivateAPIKey(mockGame.getId());

        verify(apiKeyRepository, times(1)).delete(apiKey);

    }
}
