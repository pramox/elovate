package com.tuwien.elovate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dtos.lobby.LobbyCreationDto;
import com.tuwien.elovate.dtos.lobby.LobbyDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.lobby.LobbyRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class LobbyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameApplicationRepository gameApplicationRepository;


    @BeforeEach
    void beforeEach() {
        lobbyRepository.deleteAll();
    }

    @Test
    void createLobbyWithUser_fetchLobbyByUserId_Expect200AndReceivedLobbyDTO() throws Exception {
        User user = createUser();
        String token = jwtService.generateToken(user);
        Lobby lobby = new Lobby(null, null, List.of(user), List.of(), LocalDateTime.now());
        lobbyRepository.save(lobby);


        MvcResult mvcResult = mockMvc.perform(get("/api/v1/lobby/user/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        List<LobbyDto> actualLobbyList = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        assertEquals(1, actualLobbyList.size());
        assertEquals(actualLobbyList.get(0).getTeamA().get(0).getId(), user.getId());
        assertNotNull(actualLobbyList.get(0).getId());
    }

    @Test
    void fetchLobbyByUserIdWithoutCreatingLobby_Expect200AndEmptyList() throws Exception {
        User user = createUser();
        String token = jwtService.generateToken(user);


        MvcResult mvcResult = mockMvc.perform(get("/api/v1/lobby/user/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        List<LobbyDto> actualLobbyList = objectMapper.readValue(responseBody, new TypeReference<>() {
        });

        assertEquals(0, actualLobbyList.size());
    }

    @Test
    void saveLobby_thenFindById_expect200AndCorrectLobbyDto() throws Exception {
        User user = createUser();
        String token = jwtService.generateToken(user);
        Lobby lobby = new Lobby(null, null, List.of(user), List.of(), LocalDateTime.now());
        lobby = lobbyRepository.save(lobby);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/lobby/{id}", lobby.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();
        LobbyDto actualLobby = objectMapper.readValue(responseBody, LobbyDto.class);

        assertEquals(actualLobby.getId(), lobby.getId());
    }

    @Test
    void createLobby_expect200() throws Exception {
        User user = createUser();
        String token = jwtService.generateToken(user);

        GameApplication gameApplication = new GameApplication();
        gameApplicationRepository.save(gameApplication);
        Game game = new Game();
        game.setGameApplication(gameApplication);
        game = gameRepository.save(game);
        LobbyCreationDto lobbyCreationDto = new LobbyCreationDto(Collections.singletonList(user.getId()), List.of(), game.getId());

        MvcResult result = mockMvc.perform(post("/api/v1/lobby/create")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lobbyCreationDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        LobbyDto actualLobby = objectMapper.readValue(responseBody, LobbyDto.class);

        assertEquals(actualLobby.getTeamA().get(0).getId(), user.getId());
        assertEquals(actualLobby.getGame().getId(), game.getId());
    }

    private User createUser() {
        User user = new User();
        user.setPassword("password");
        user.setEmail("email");
        user.setNickName("nickname_test");
        user.setRoles(Set.of(Role.ADMIN));
        userRepository.save(user);
        return user;
    }
}
