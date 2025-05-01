package com.tuwien.elovate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.statistics.EloByDayDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.enums.UserStatus;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class GameStatisticsTest {
    private static final String BASE_URI = "/api/v1/statistics/";
    private final int timePeriod = 10;
    private List<User> users;
    private Game game;
    private String userToken;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private DataClearer dataClearer;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    GameApplicationRepository gameApplicationRepository;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    RatingRepository ratingRepository;
    @Autowired
    private GameAccessRepository gameAccessRepository;
    @Autowired
    private PlayerStatsPostGameRepository playerStatsPostGameRepository;
    @Autowired
    FinishedGameRepository finishedGameRepository;


    @BeforeEach
    @Transactional
    void setup() {
        dataClearer.clear();
        userRepository.deleteAll();
        mockGameApplications();
        mockUsers();
        mockMatches();
        mockFinishedGames();
    }

    private void mockUsers() {
        Set<Game> games = new HashSet<>(gameRepository.findAll());
        Set<Role> user1Roles = new HashSet<>();
        user1Roles.add(Role.USER);
        User mockUser1 = User.builder()
                .roles(user1Roles)
                .email("user1@email.com")
                .nickName("user1")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .password(getEncodedPassword())
                .blocked(new HashSet<>())
                .friends(new HashSet<>())
                .friendRequests(new HashSet<>())
                .connectedGames(games)
                .build();
        mockUser1 = userRepository.save(mockUser1);

        Set<Role> user2Roles = new HashSet<>();
        user2Roles.add(Role.USER);
        User mockUser2 = User.builder()
                .roles(user2Roles)
                .email("user2@email.com")
                .nickName("user2")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .password(getEncodedPassword())
                .blocked(new HashSet<>())
                .friends(new HashSet<>())
                .friendRequests(new HashSet<>())
                .connectedGames(games)
                .build();
        mockUser2 = userRepository.save(mockUser2);

        Set<Role> user3Roles = new HashSet<>();
        user3Roles.add(Role.USER);
        User mockUser3 = User.builder()
                .roles(user3Roles)
                .email("user3@email.com")
                .nickName("user3")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .password(getEncodedPassword())
                .blocked(new HashSet<>())
                .friends(new HashSet<>())
                .friendRequests(new HashSet<>())
                .connectedGames(games)
                .build();
        mockUser3 = userRepository.save(mockUser3);
        this.users = List.of(mockUser1, mockUser2, mockUser3);
        this.userToken = jwtService.generateToken(this.users.get(0));
    }

    private void mockGameApplications() {
        GameApplication gameApplication = new GameApplication();
        gameApplicationRepository.save(gameApplication);
        Game game = new Game();
        game.setGameApplication(gameApplication);
        this.game = gameRepository.save(game);
    }

    private void mockMatches() {
        List<User> userList = userRepository.findAll();
        System.out.println("USER SIZE " + userList.size());
        for (User user : userList) {
            Rating rating = new Glicko2Rating(10.0, 10.0, 10.0, 10.0, 10.0);
            rating.setRating(Math.random() * 2 + 100);
            ratingRepository.save(rating);
            GameAccess gameAccess = new GameAccess(UUID.randomUUID().toString(), user, this.game, rating, null, true);
            gameAccessRepository.save(gameAccess);
        }
        System.out.println("Game access size: " + gameAccessRepository.findAll().size());
        System.out.println("Rating size: " + ratingRepository.findAll().size());
    }

    private void mockFinishedGames() {
        List<User> userList = userRepository.findAll();
        for (int day = 0; day < this.timePeriod; day++) {
            List<PlayerStatsPostGame> playerStatsPostGames1 = new ArrayList<>();
            PlayerStatsPostGame playerStatsPostGame1 = new PlayerStatsPostGame(null, userList.get(0), null, Math.random() * 100.0, Math.random() * 100.0, List.of());
            playerStatsPostGameRepository.save(playerStatsPostGame1);
            playerStatsPostGames1.add(playerStatsPostGame1);
            List<PlayerStatsPostGame> playerStatsPostGames2 = new ArrayList<>();
            PlayerStatsPostGame playerStatsPostGame2 = new PlayerStatsPostGame(null, userList.get(1), null, Math.random() * 100.0, Math.random() * 100.0, List.of());
            playerStatsPostGameRepository.save(playerStatsPostGame2);
            playerStatsPostGames2.add(playerStatsPostGame2);
            FinishedGame finishedGame = new FinishedGame(null, this.game, GameEndResult.TEAM_A_WINNER, playerStatsPostGames1, playerStatsPostGames2, LocalDateTime.now().minusDays(day), null);
            finishedGameRepository.save(finishedGame);
        }
    }

    private String getEncodedPassword() {
        return passwordEncoder.encode("1234");
    }

    @Test
    void getLeaderboard_ShouldReturnCorrectOrder() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URI + this.game.getId() + "/leaderboard")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andReturn();

        List<LinkedHashMap<String, Object>> response = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        double currentRating = (double) response.get(0).get("rating");
        for (LinkedHashMap<String, Object> obj : response) {
            double rating = (double) obj.get("rating");
            assertTrue(rating <= currentRating);
            currentRating = rating;
        }
    }

    @Test
    void getLeaderboardWithCountry_ShouldReturnCorrectOrder() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URI + this.game.getId() + "/leaderboard?countryCode=AT")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andReturn();

        List<LinkedHashMap<String, Object>> response = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        double currentRating = (double) response.get(0).get("rating");
        for (LinkedHashMap<String, Object> obj : response) {
            double rating = (double) obj.get("rating");
            assertTrue(rating <= currentRating);
            currentRating = rating;
            assertEquals("AT", obj.get("countryCode"));
        }
    }

    @Test
    void getRankDistribution_ShouldReturnCorrectValues() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URI + this.game.getId() + "/distribution")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()).andReturn();

        LinkedHashMap<String, Object> response = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
        });

        ArrayList<LinkedHashMap> distribution = (ArrayList<LinkedHashMap>) response.get("rankDistribution");
        double sum = 0;
        for (LinkedHashMap value : distribution) {
            sum += (double) value.get("second");
        }

        assertTrue(Math.abs(sum - 100) < 1); // there might be some rounding errors - error of 1% is acceptable
    }

    @Test
    void getLeaderboardWithIncorrectUser_ShouldReturn403() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URI + this.game.getId() + "/leaderboard/45")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest()).andReturn();
    }

    @Test
    void getEloOverTimeForUser_ShouldReturnCorrectValues() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders
                        .get(BASE_URI + "/game/" + this.game.getId().toString() + "/eloOverTime")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isCreated()).andReturn();
        String responseBody = result.getResponse().getContentAsString();
        List<EloByDayDto> eloList = mapper.readValue(responseBody, new TypeReference<List<EloByDayDto>>() {
        });
        HashMap<LocalDateTime, EloByDayDto> eloMap = new HashMap<>();
        for (EloByDayDto eloByDayDto : eloList) {
            eloMap.put(eloByDayDto.getFinishTime(), eloByDayDto);
        }
        assertEquals(timePeriod, eloMap.size());
    }
}
