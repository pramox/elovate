package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.external.CustomStatDto;
import com.tuwien.elovate.dtos.external.CustomUserStatsDto;
import com.tuwien.elovate.dtos.external.ExternalGlicko2RatingParametersDto;
import com.tuwien.elovate.dtos.external.ExternalLobbyProvideDto;
import com.tuwien.elovate.dtos.external.ExternalMatchmakingDto;
import com.tuwien.elovate.dtos.external.ExternalMatchmakingDtoWithParams;
import com.tuwien.elovate.dtos.external.ExternalUser;
import com.tuwien.elovate.dtos.external.ExternalUserInfoDto;
import com.tuwien.elovate.dtos.external.GameEndInfoDto;
import com.tuwien.elovate.dtos.external.UserUnlockDto;
import com.tuwien.elovate.entities.apikey.ApiKey;
import com.tuwien.elovate.entities.apikey.ApiKeyId;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.repositories.apikey.APIKeyRepository;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.lobby.LobbyRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExternalGameApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataClearer dataClearer;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private GameAccessRepository gameAccessRepository;

    @Autowired
    private APIKeyRepository apiKeyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private FinishedGameRepository finishedGameRepository;

    @Autowired
    private RatingRepository ratingRepository;

    private Game game;
    private User user;
    private User user2;
    private String apiKeyUnhashed;
    private Lobby lobby;


    @BeforeEach
    void setup() {
        dataClearer.clear();
        finishedGameRepository.deleteAll();

        Game game1 = new Game();
        game = gameRepository.save(game1);
        user = userRepository.save(new User());
        user2 = userRepository.save(new User());

        apiKeyUnhashed = UUID.randomUUID().toString();
        var apiKey = ApiKey.builder()
                .id(new ApiKeyId(game.getId(), user.getId()))
                .key(DigestUtils.sha256Hex(apiKeyUnhashed))
                .timestamp(Instant.now())
                .build();
        apiKeyRepository.save(apiKey);

        lobby = new Lobby(null, game, List.of(user), List.of(user2), LocalDateTime.now());
        lobby = lobbyRepository.save(lobby);
    }

    @Test
    void unlockUser_WithValidApiKey_Expect200() throws Exception {
        GameAccess gameAccess = new GameAccess(UUID.randomUUID().toString(), user, game, null, null, true);
        gameAccessRepository.save(gameAccess);
        UserUnlockDto userUnlockDto = new UserUnlockDto(gameAccess.getUuid(), apiKeyUnhashed);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/external/unlock-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUnlockDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ExternalUserInfoDto obj = objectMapper.readValue(responseBody, ExternalUserInfoDto.class);

        assertEquals(obj.getGameId(), game.getId());
        assertEquals(true, obj.getSuccess());
        assertEquals(obj.getUserId(), user.getId());
    }

    @Test
    void unlockUser_withInvalidApiKey_Expect401() throws Exception {
        GameAccess gameAccess = new GameAccess(UUID.randomUUID().toString(), user, game, null, null, true);
        gameAccessRepository.save(gameAccess);

        String invalidApiKey = "invalidApiKey123";
        UserUnlockDto userUnlockDto = new UserUnlockDto(gameAccess.getUuid(), invalidApiKey);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/external/unlock-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUnlockDto)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    void unlockUser_withValidApiKey_AndUUIDNull_Expect404() throws Exception {
        String validApiKey = apiKeyUnhashed;

        UserUnlockDto userUnlockDto = new UserUnlockDto(null, validApiKey);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/external/unlock-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUnlockDto)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity());
    }

    @Test
    void getAllLobbiesFromGame_Expect200() throws Exception {
        ExternalLobbyProvideDto externalLobbyProvideDto = new ExternalLobbyProvideDto(apiKeyUnhashed, game.getId());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/external/lobbies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(externalLobbyProvideDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
    }

    @Test
    void validGameEnd_ShouldReturn200_AndUpdateRankings() throws Exception {
        Rating rating = new Glicko2Rating(0.06, 200.0, null, null, null);
        rating.setRating(1000.0);
        ratingRepository.save(rating);

        Rating rating2 = new Glicko2Rating(0.06, 200., null, null, null);
        rating2.setRating(1000.0);
        ratingRepository.save(rating2);

        GameAccess gameAccess = new GameAccess(UUID.randomUUID().toString(), user, game, rating, null, true);
        GameAccess gameAccess2 = new GameAccess(UUID.randomUUID().toString(), user2, game, rating2, null, true);
        gameAccessRepository.save(gameAccess);
        gameAccessRepository.save(gameAccess2);


        CustomUserStatsDto customUserStatsDto = new CustomUserStatsDto(user.getId(),
                List.of(
                        new CustomStatDto("Kills", "12"),
                        new CustomStatDto("Deaths", "3"))
        );

        CustomUserStatsDto customUserStatsDto2 = new CustomUserStatsDto(user2.getId(),
                List.of(
                        new CustomStatDto("Kills", "3"),
                        new CustomStatDto("Deaths", "33"))
        );


        GameEndInfoDto gameEndInfoDto = new GameEndInfoDto(GameEndResult.TEAM_A_WINNER, game.getId(), apiKeyUnhashed, List.of(customUserStatsDto, customUserStatsDto2));
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/external/game-end/" + lobby.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameEndInfoDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        var finishedGames = finishedGameRepository.findAll();
        FinishedGame finished = null;
        for (FinishedGame finishedGame : finishedGames) {
            if (finishedGame.getGame() == null) {
                continue;
            }
            if (game.getId().equals(finishedGame.getGame().getId())) {
                finished = finishedGame;
            }
        }
        var user1 = finished.getTeamA().get(0);
        var user2 = finished.getTeamB().get(0);

        //Draw
        assertEquals(-78.80171887243887, user1.getRatingBefore() - user1.getRatingAfter());
        assertEquals(78.80171887243876, user2.getRatingBefore() - user2.getRatingAfter());
        assertEquals("Kills", finished.getTeamA().get(0).getCustomStats().get(0).getStatName());
    }

    @Test
    void glickoMatchmaking_WithExternalEndpoint() throws Exception {
        ExternalUser externalUser = new ExternalUser("id1", 1000.0, 0.06, 200.);
        ExternalUser externalUser1 = new ExternalUser("id2", 1000.0, 0.06, 200.);

        ExternalMatchmakingDto externalMatchmakingDto =
                new ExternalMatchmakingDto(List.of(externalUser), List.of(externalUser1), GameEndResult.TEAM_A_WINNER);
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/external/matchmaking/glicko")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(externalMatchmakingDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        ExternalMatchmakingDto obj = objectMapper.readValue(responseBody, ExternalMatchmakingDto.class);

        assertEquals(78.80171887243887, obj.getTeamA().get(0).getRating() - externalUser.getRating());
        assertEquals(-78.80171887243876, obj.getTeamB().get(0).getRating() - externalUser1.getRating());
    }

    @Test
    void glickoMatchmaking_WithExternalEndpoint_WithParams() throws Exception {
        ExternalUser externalUser = new ExternalUser("id1", 1000.0, 0.06, 200.);
        ExternalUser externalUser1 = new ExternalUser("id2", 1000.0, 0.06, 200.);

        ExternalMatchmakingDto externalMatchmakingDto =
                new ExternalMatchmakingDto(List.of(externalUser), List.of(externalUser1), GameEndResult.TEAM_A_WINNER);

        ExternalMatchmakingDtoWithParams externalMatchmakingDtoWithParams = new ExternalMatchmakingDtoWithParams(
                externalMatchmakingDto,
                new ExternalGlicko2RatingParametersDto(
                        1500d, 200d, 0.1, 1.0, 0d, 4000d
                ));
        var result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/external/matchmaking/glicko-params")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(externalMatchmakingDtoWithParams)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        ExternalMatchmakingDto obj = objectMapper.readValue(responseBody, ExternalMatchmakingDto.class);

        assertEquals(78.80171887243887, obj.getTeamA().get(0).getRating() - externalUser.getRating());
        assertEquals(-78.80171887243876, obj.getTeamB().get(0).getRating() - externalUser1.getRating());
    }
}
