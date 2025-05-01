package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.TestData;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.apikey.ApiKeyDto;
import com.tuwien.elovate.entities.apikey.ApiKey;
import com.tuwien.elovate.entities.apikey.ApiKeyId;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.repositories.apikey.APIKeyRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@AutoConfigureMockMvc
class APIKeyControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameApplicationRepository gameApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private APIKeyRepository apiKeyRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataClearer dataClearer;

    private User developer;

    private Game game;

    private ApiKey apiKey;

    private static final String URL = TestData.BASE_URL + TestData.API_KEY_URL;

    @BeforeEach
    void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        dataClearer.clear();


        developer = User.builder()
                .nickName("developer")
                .email("developer@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .password("password")
                .build();

        developer = userRepository.save(developer);

        GameApplication gameApplication = GameApplication.builder()
                .developer(developer)
                .drawPossible(false)
                .playersPerTeam(1)
                .build();

        gameApplication = gameApplicationRepository.save(gameApplication);

        game = Game.builder()
                .name("game")
                .gameApplication(gameApplication)
                .drawPossible(false)
                .playersPerTeam(1)
                .build();

        game = gameRepository.save(game);

        gameApplication.setGame(game);
        gameApplicationRepository.save(gameApplication);

        apiKey = ApiKey.builder()
                .id(new ApiKeyId(game.getId(), developer.getId()))
                .key(DigestUtils.sha256Hex(UUID.randomUUID().toString()))
                .timestamp(Instant.now())
                .build();

    }


    @Test
    void get_apiKeyHash_if_not_generated_returns_404() throws Exception {
        String token = jwtService.generateToken(developer);
        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    void get_apiKeyHash_successful() throws Exception {
        String token = jwtService.generateToken(developer);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);

        String response = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        ApiKeyDto apiKeyDTO = mapper.readValue(response, ApiKeyDto.class);

        assertEquals(apiKey.getKey(), apiKeyDTO.getKey());
        assertEquals(apiKey.getId().getGameId(), apiKeyDTO.getGameId());
        assertEquals(LocalDateTime.ofInstant(apiKey.getTimestamp(), ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).toString(), apiKeyDTO.getTimestamp());
    }


    @Test
    void generate_apiKey_successful() throws Exception {
        String token = jwtService.generateToken(developer);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.post(URL + "/generateApiKey?gameId=" + game.getId())
                .header("Authorization", "Bearer " + token);

        String response = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        ApiKeyDto apiKeyDTO = mapper.readValue(response, ApiKeyDto.class);

        assertNotNull(apiKeyDTO);
        assertNotNull(apiKeyDTO.getKey());
        assertEquals(TestData.UUID_4_LENGTH, apiKeyDTO.getKey().length());
        assertEquals(game.getId(), apiKeyDTO.getGameId());
    }

    @Test
    void generate_apiKey_for_unknown_game_returns_404() throws Exception {
        String token = jwtService.generateToken(developer);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.post(URL + "/generateApiKey?gameId=9999")
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request).andExpect(status().isNotFound());
    }


    @Test
    void generate_apiKey_for_other_user_returns_401() throws Exception {
        User developer2 = User.builder()
                .nickName("developer2")
                .email("developer2@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();
        userRepository.save(developer2);

        String token = jwtService.generateToken(developer2);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.post(URL + "/generateApiKey?gameId=" + game.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }


    @Test
    void get_apiKeyHash_for_other_user_returns_401() throws Exception {
        User developer2 = User.builder()
                .nickName("developer2")
                .email("developer2@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();
        userRepository.save(developer2);

        String token = jwtService.generateToken(developer2);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);

        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }


    @Test
    void delete_apiKey_successful() throws Exception {
        String token = jwtService.generateToken(developer);

        apiKeyRepository.save(apiKey);
        RequestBuilder request = MockMvcRequestBuilders.delete(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);

        assertTrue(apiKeyRepository.findById(apiKey.getId()).isPresent());
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertFalse(apiKeyRepository.findById(apiKey.getId()).isPresent());
    }

    @Test
    void delete_apiKey_for_other_user_returns_401() throws Exception {
        User developer2 = User.builder()
                .nickName("developer2")
                .email("developer2@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();
        userRepository.save(developer2);

        String token = jwtService.generateToken(developer2);
        apiKeyRepository.save(apiKey);

        RequestBuilder request = MockMvcRequestBuilders.delete(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);

        assertTrue(apiKeyRepository.findById(apiKey.getId()).isPresent());
        mockMvc.perform(request).andExpect(status().isUnauthorized());
        assertTrue(apiKeyRepository.findById(apiKey.getId()).isPresent());
    }


}
