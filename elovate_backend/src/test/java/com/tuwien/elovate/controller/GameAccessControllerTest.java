package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dtos.game.GameAccessDto;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.apikey.APIKeyService;
import com.tuwien.elovate.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RequiredArgsConstructor
@WebAppConfiguration
@SpringBootTest
@ActiveProfiles("test")
class GameAccessControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GameAccessRepository gameAccessRepository;

    @Autowired
    GameRepository gameRepository;

    private User user;

    private User developer;

    private User admin;

    private String userToken;

    private String developerToken;

    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @MockBean
    private APIKeyService apiKeyService;

    @Autowired
    private JwtService jwtService;

    private static final String BASE_URI = "/api/v1/";

    @BeforeEach
    void setup() {
        this.developer = userRepository.findFirstByRolesContains(Role.GAME_DEVELOPER).get();
        this.user = userRepository.findFirstByRolesContains(Role.USER).get();
        this.admin = userRepository.findFirstByRolesContains(Role.ADMIN).get();
        this.mapper = new ObjectMapper();

        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        this.developerToken = jwtService.generateToken(developer);
        this.userToken = jwtService.generateToken(user);
    }

    @Transactional
    @Test
    void getOldUuidCode_withCorrectUserAndGame_ShouldReturnOk() throws Exception {
        Long gameId = gameRepository.findAll().get(0).getId();
        when(apiKeyService.validateAPIKey(gameId, developer.getId(), "1234")).thenReturn(true);

        gameAccessRepository.deleteByUserAndGame(developer, gameRepository.findById(gameId).get());
        GameAccess gameAccess = this.gameAccessRepository.save(new GameAccess(
                null, developer, gameRepository.findById(gameId).get(),
                null, Instant.now(), true));
        GameAccessDto gameAccessDTO = new GameAccessDto(null, developer.getId(), 1L, null, null);
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URI + "games/access")
                        .content(JSONValue.toJSONString(gameAccessDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk()).andReturn();

        GameAccessDto response = mapper.readValue(result.getResponse().getContentAsString(), GameAccessDto.class);

        assertNotNull(response.getUuid());
        assertTrue(this.gameAccessRepository.findByUser_IdAndGame_Id(developer.getId(), gameId).isPresent());
        assertEquals(response.getUuid(), gameAccessRepository.findByUser_IdAndGame_Id(developer.getId(), gameId).get().getUuid());
        assertEquals(gameAccess.getUuid(), response.getUuid());
    }

    @Transactional
    @Test
    void generate_newCodeWithCorrectUserAndGame_ShouldReturnOk() throws Exception {
        Long gameId = gameRepository.findAll().get(0).getId();
        when(apiKeyService.validateAPIKey(gameId, developer.getId(), "1234")).thenReturn(true);

        gameAccessRepository.deleteByUserAndGame(developer, gameRepository.findById(gameId).get());
        GameAccessDto gameAccessDTO = new GameAccessDto(null, developer.getId(), 1L, null, null);

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URI + "games/access")
                        .content(JSONValue.toJSONString(gameAccessDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk()).andReturn();

        GameAccessDto response = mapper.readValue(result.getResponse().getContentAsString(), GameAccessDto.class);

        assertNotNull(response.getUuid());
        assertTrue(this.gameAccessRepository.findByUser_IdAndGame_Id(developer.getId(), gameId).isPresent());
        assertEquals(response.getUuid(), gameAccessRepository.findByUser_IdAndGame_Id(developer.getId(),
                gameId).get().getUuid());
    }

    @Test
    void generate_withIncorrectUserOrGame_ShouldThrowException() throws Exception {
        when(apiKeyService.validateAPIKey(1L, admin.getId(), "1234")).thenReturn(true);

        GameAccessDto gameAccessDTO = new GameAccessDto(null, 10L, 10L, null, "1234");
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(BASE_URI + "games/access")
                        .content(JSONValue.toJSONString(gameAccessDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isUnprocessableEntity());
    }
}
