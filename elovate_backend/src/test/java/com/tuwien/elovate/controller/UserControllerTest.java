package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.TestData;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.authentication.LoginRequestDto;
import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.dtos.user.RegisterRequestDto;
import com.tuwien.elovate.dtos.user.UserGameRankingPagedDto;
import com.tuwien.elovate.dtos.user.UserProfileDto;
import com.tuwien.elovate.dtos.user.UserResponsePagedDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.enums.UserStatus;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataClearer dataClearer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameApplicationRepository gameApplicationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameAccessRepository gameAccessRepository;

    @Autowired
    private JwtService jwtService;

    private static final String URL = TestData.BASE_URL + TestData.USERS_URL;
    private static final int MAX_BLOCKED_USERS = 10;

    @BeforeEach
    @Transactional
    void beforeEach() {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        dataClearer.clear();
        userRepository.deleteAll();

        System.out.println(userRepository.findAll());

        User mockDeveloper = User.builder()
                .nickName("developer")
                .email("developer@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(new HashSet<>(Set.of(Role.USER, Role.GAME_DEVELOPER)))
                .password(getEncodedPassword())
                .build();

        User mockPlayer = User.builder()
                .nickName("player")
                .email("player@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(new HashSet<>(Set.of(Role.USER)))
                .password(getEncodedPassword())
                .build();

        User mockUser = User.builder()
                .nickName("user")
                .email("user@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(new HashSet<>(Set.of(Role.USER)))
                .password(getEncodedPassword())
                .build();

        User mockAdmin = User.builder()
                .nickName("admin")
                .email("admin@email.com")
                .countryCode("AT")
                .status(UserStatus.NORMAL)
                .roles(new HashSet<>(Set.of(Role.USER, Role.ADMIN)))
                .password(getEncodedPassword())
                .build();

        userRepository.saveAll(new ArrayList<>(
                List.of(mockDeveloper, mockPlayer, mockUser, mockAdmin)
        ));

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
                .connectedGames(new HashSet<>())
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
                .connectedGames(new HashSet<>())
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
                .connectedGames(new HashSet<>())
                .build();
        mockUser3 = userRepository.save(mockUser3);

        Set<User> user1Friends = new HashSet<>();
        user1Friends.add(mockUser2);
        mockUser1.setFriends(user1Friends);
        Set<User> user1Blocked = new HashSet<>();
        user1Blocked.add(mockUser3);
        mockUser1.setBlocked(user1Blocked);
        mockUser1 = userRepository.save(mockUser1);

        Set<User> user2Friends = new HashSet<>();
        user2Friends.add(mockUser1);
        mockUser2.setFriends(user2Friends);
        userRepository.save(mockUser2);
    }

    String getEncodedPassword() {
        return passwordEncoder.encode("1234");
    }

    @Test
    void testRegisterEndpoint_Expect200() throws Exception {
        RegisterRequestDto requestDTO = new RegisterRequestDto();
        requestDTO.setEmail("test1@example.com");
        requestDTO.setNickname("testUser123");
        requestDTO.setPassword("Password123#");

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void registerWithInvalidEmail_Expect422() throws Exception {
        RegisterRequestDto requestDTO = new RegisterRequestDto();
        requestDTO.setEmail("test.example.com");
        requestDTO.setNickname("testUser1234");
        requestDTO.setPassword("Password123#");

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    void registerWithInvalidPassword_Expect422() throws Exception {
        RegisterRequestDto requestDTO = new RegisterRequestDto();
        requestDTO.setEmail("test3@example.com");
        requestDTO.setNickname("testUser123556");
        requestDTO.setPassword("nonumbers");

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    void registerWithAlreadyRegisteredEmail_Expect422() throws Exception {
        RegisterRequestDto requestDTO = new RegisterRequestDto();
        requestDTO.setEmail("user@email.com");
        requestDTO.setNickname("testUser123");
        requestDTO.setPassword("Password123#");

        ResultActions result = mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)));

        result.andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("[{\"errorKey\":\"EMAIL_ALREADY_TAKEN\",\"message\":\"Email already taken\"}]"));
    }

    @Test
    void loginWithValidData_Expect200() throws Exception {
        LoginRequestDto loginRequestDTO = new LoginRequestDto();
        loginRequestDTO.setNickname("user@email.com");
        loginRequestDTO.setPassword("1234");

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDTO)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginWithInvalidData_Expect302() throws Exception {
        LoginRequestDto loginRequestDTO = new LoginRequestDto();
        loginRequestDTO.setNickname("user@email.com");
        loginRequestDTO.setPassword("12345");

        ResultActions result = mockMvc.perform(post("/api/v1/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestDTO)));

        result.andExpect(status().isFound());
    }

    @Test
    void user_getAllBlockedUsers_expect_200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User blockedUser = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(URL + "/blocked?page=0&size=5")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        UserResponsePagedDto responseDto = mapper.readValue(response, UserResponsePagedDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(1, responseDto.getTotalElements());
        Assertions.assertEquals(blockedUser.getId(), responseDto.getValues().get(0).getId());
    }

    @Test
    void user_blocksUser_expect_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/block/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("\"blocked\":true"));
        Assertions.assertTrue(response.contains("\"id\":" + user3.getId()));

        user2 = userRepository.findByNickName("user2").orElseThrow();
        Assertions.assertTrue(user2.getBlocked().contains(user3));
    }

    @Test
    void user_blocksFriend_expect_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/block/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("\"blocked\":true"));
        Assertions.assertTrue(response.contains("\"id\":" + user2.getId()));

        user1 = userRepository.findByNickName("user1").orElseThrow();
        user2 = userRepository.findByNickName("user2").orElseThrow();
        Assertions.assertTrue(user1.getBlocked().contains(user2));
        Assertions.assertFalse(user1.getFriends().contains(user2));
        Assertions.assertFalse(user2.getFriends().contains(user1));
    }

    @Test
    void user_blocks_themself_expect_422() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/block/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.USER_CANT_BLOCK_THEMSELVES.getMessage()));
    }

    @Test
    void user_blocks_moreThanTheMaximum_expect_422() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user2);
        User user3 = userRepository.findByNickName("user3").orElseThrow();

        for (int i = 0; i < MAX_BLOCKED_USERS; i++) {
            Set<Role> userRoles = new HashSet<>();
            userRoles.add(Role.USER);
            User mockUser = User.builder()
                    .roles(userRoles)
                    .email("user+" + i + "@email.com")
                    .nickName("user+" + i)
                    .countryCode("AT")
                    .status(UserStatus.NORMAL)
                    .password(getEncodedPassword())
                    .blocked(new HashSet<>())
                    .friends(new HashSet<>())
                    .connectedGames(new HashSet<>())
                    .build();
            mockUser = userRepository.save(mockUser);
            RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/block/" + mockUser.getId())
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(null));
            mockMvc.perform(requestBuilder).andExpect(status().isOk());
        }

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/block/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.USER_CANT_BLOCK_MORE_USERS.getMessage()));
    }

    @Test
    void user_unblockUser_expect_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unblock/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("\"blocked\":false"));
        Assertions.assertTrue(response.contains("\"id\":" + user3.getId()));

        user1 = userRepository.findByNickName("user1").orElseThrow();
        Assertions.assertFalse(user1.getBlocked().contains(user3));
    }

    @Test
    void user_unblocks_notBlockedUser_expect_422() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unblock/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.USER_IS_NOT_BLOCKED.getMessage()));
    }

    @Test
    void user_unblocks_themself_expect_422() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unblock/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.USER_CANT_UNBLOCK_THEMSELVES.getMessage()));
    }

    @Test
    void user_unfriendUser_expect_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unfriend/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains("\"friend\":false"));
        Assertions.assertTrue(response.contains("\"id\":" + user2.getId()));

        user1 = userRepository.findByNickName("user1").orElseThrow();
        user2 = userRepository.findByNickName("user2").orElseThrow();
        Assertions.assertFalse(user1.getFriends().contains(user2));
        Assertions.assertFalse(user2.getFriends().contains(user1));
    }

    @Test
    void user_unfriendStranger_expect_422() throws Exception {
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user3);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unfriend/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.CANT_UNFRIEND_STRANGER.getMessage()));
    }

    @Test
    void user_unfriends_themself_expect_422() throws Exception {
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user3);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/unfriend/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.CANT_UNFRIEND_YOURSELF.getMessage()));
    }

    @Test
    void user_getAllRankedGames_expect_200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();

        GameApplication gameApplication = GameApplication.builder()
                .developer(userRepository.findFirstByRolesContains(Role.GAME_DEVELOPER).orElseThrow())
                .drawPossible(false)
                .playersPerTeam(1)
                .build();
        gameApplication = gameApplicationRepository.save(gameApplication);
        Game game = Game.builder()
                .name("game")
                .gameApplication(gameApplication)
                .drawPossible(false)
                .playersPerTeam(1)
                .build();
        game = gameRepository.save(game);
        Set<Game> games = user3.getConnectedGames();
        games.add(game);
        userRepository.save(user3);

        Rating rating = new Glicko2Rating(0.06, 200.0, null, null, null);
        rating.setRating(1000.0);
        ratingRepository.save(rating);

        GameAccess gameAccess = new GameAccess(UUID.randomUUID().toString(), user3, game, rating, null, true);
        gameAccessRepository.save(gameAccess);
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(URL + "/profile/" + user3.getId() + "/games?page=0&size=5")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        UserGameRankingPagedDto responseDto = mapper.readValue(response, UserGameRankingPagedDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(1, responseDto.getTotalElements());
        Assertions.assertEquals(user3.getNickName(),
                responseDto.getValues().get(0).getUser().getNickName());
        Assertions.assertEquals(game.getName(),
                responseDto.getValues().get(0).getGame().getName());
    }

    @Test
    void user_getAllRankedGames_ofUserWithNoGames_expect_200() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(URL + "/profile/" + user3.getId() + "/games?page=0&size=5")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        UserGameRankingPagedDto responseDto = mapper.readValue(response, UserGameRankingPagedDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(0, responseDto.getTotalElements());
    }

    @Test
    void user_getAllRankedGames_fromUnknownUser_expect_404() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(URL + "/profile/" + 100L + "/games?page=0&size=5")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.USER_NOT_FOUND.getMessage()));
    }


    @Test
    void user_getProfileById_whenBlocked_returnsCorrectDto_and_200() throws Exception {
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/profile/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertTrue(userProfileDto.isBlocked());
        Assertions.assertFalse(userProfileDto.isRequesting());
        Assertions.assertFalse(userProfileDto.isRequested());
        Assertions.assertFalse(userProfileDto.isFriend());
    }

    @Test
    void user_getProfileById_whenFriends_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/profile/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertFalse(userProfileDto.isBlocked());
        Assertions.assertFalse(userProfileDto.isRequesting());
        Assertions.assertFalse(userProfileDto.isRequested());
        Assertions.assertTrue(userProfileDto.isFriend());
    }

    @Test
    void user_getProfileById_whenSelf_returnsCorrectDto_and_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/profile/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertFalse(userProfileDto.isBlocked());
        Assertions.assertFalse(userProfileDto.isRequesting());
        Assertions.assertFalse(userProfileDto.isRequested());
        Assertions.assertFalse(userProfileDto.isFriend());
        Assertions.assertTrue(userProfileDto.isSelf());
    }


    @Test
    void user_blocked_returnsCorrectDto_and_200() throws Exception {
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/blocked")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(1, userProfileDto.getTotalElements());
        Assertions.assertEquals(user3.getId(), userProfileDto.getValues().get(0).getId());
    }

    @Test
    void user_friends_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/friends")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(1, userProfileDto.getTotalElements());
        Assertions.assertEquals(user2.getId(), userProfileDto.getValues().get(0).getId());
    }

    @Test
    void user_getFriendRequests_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        user2.setFriendRequests(new HashSet<>(Set.of(user3)));
        userRepository.save(user2);
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(1, userProfileDto.getTotalElements());
        Assertions.assertEquals(user3.getId(), userProfileDto.getValues().get(0).getId());
    }

    @Test
    void user_getFriendRequests_returnsEmptyDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "/requests")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(0, userProfileDto.getTotalElements());
    }


    @Test
    void user_getUsersByNickname_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();

        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "?nickname=us&friends=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(1, userProfileDto.getTotalElements());
        Assertions.assertEquals(user2.getId(), userProfileDto.getValues().get(0).getId());
    }


    @Test
    void user_getUsersByNickname_returnsEmptyDto_and_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();

        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "?nickname=user9999&friends=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(0, userProfileDto.getTotalElements());
    }


    @Test
    void user_getUsersByNickname_AndBlocked_returnsCorrectto_and_200() throws Exception {
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(URL + "?nickname=us&blocked=true")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON);

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserResponsePagedDto userProfileDto = mapper.readValue(response, UserResponsePagedDto.class);

        Assertions.assertEquals(1, userProfileDto.getTotalElements());
        Assertions.assertEquals(user3.getId(), userProfileDto.getValues().get(0).getId());

    }


    @Test
    void user_confirmFriend_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        user2.setFriendRequests(new HashSet<>(Set.of(user3)));
        userRepository.save(user2);
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/confirm/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertTrue(userProfileDto.isFriend());
        Assertions.assertTrue(userRepository.findByNickName("user3").orElseThrow().getFriends().contains(user2));
        Assertions.assertTrue(userRepository.findByNickName("user2").orElseThrow().getFriends().contains(user3));
    }


    @Test
    void user_confirmFriendWhenBlockedByUser_returnsCorrectDto_and_404() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        user2.setFriendRequests(new HashSet<>(Set.of(user3)));
        userRepository.save(user2);
        user3.setBlocked(new HashSet<>(Set.of(user2)));
        userRepository.save(user3);
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/confirm/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains(Message.USER_NOT_FOUND.getMessage()));
        Assertions.assertFalse(userRepository.findByNickName("user3").orElseThrow().getFriends().contains(user2));
        Assertions.assertFalse(userRepository.findByNickName("user2").orElseThrow().getFriends().contains(user3));
        Assertions.assertFalse(userRepository.findByNickName("user2").orElseThrow().getFriendRequests().contains(user3));
    }


    @Test
    void user_confirm_no_friend_request_returns_404() throws Exception {
        User user1 = userRepository.findByNickName("user2").orElseThrow();
        User user2 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/confirm/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains(Message.FRIEND_REQUEST_NOT_FOUND.getMessage()));

    }


    @Test
    void user_befriend_returnsCorrectDto_and_200() throws Exception {
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user2);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/befriend/" + user3.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertTrue(userProfileDto.isRequested());
        Assertions.assertTrue(userRepository.findByNickName("user3").orElseThrow().getFriendRequests().contains(user2));

    }

    @Test
    void user_befriend_when_Blocked_404() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user3 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user3);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/befriend/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

    }

    @Test
    void user_befriend_when_self_returns_422() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/befriend/" + user1.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains(Message.USER_IS_THEMSELVES.getMessage()));

    }

    @Test
    void user_befriend_alredy_friends_returns_422() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user2 = userRepository.findByNickName("user2").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/befriend/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains(Message.USER_IS_ALREADY_FRIEND.getMessage()));

    }


    @Test
    void user_befriend_when_blocked_makes_to_friend_and_returns_200() throws Exception {
        User user1 = userRepository.findByNickName("user1").orElseThrow();
        User user2 = userRepository.findByNickName("user3").orElseThrow();
        String token = jwtService.generateToken(user1);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(URL + "/befriend/" + user2.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserProfileDto userProfileDto = mapper.readValue(response, UserProfileDto.class);

        Assertions.assertTrue(userProfileDto.isRequested());
        Assertions.assertFalse(userProfileDto.isBlocked());
        Assertions.assertTrue(userRepository.findByNickName("user3").orElseThrow().getFriendRequests().contains(user1));
        Assertions.assertFalse(userRepository.findByNickName("user1").orElseThrow().getBlocked().contains(user2));

    }

    @Test
    void promoteToAdmin_Expect200AndUpdatedAdmin() throws Exception {
        User user = userRepository.findFirstByRolesContains(Role.USER).orElseThrow();
        var roles = user.getRoles();
        User admin = userRepository.findFirstByRolesContains(Role.ADMIN).orElseThrow();

        String token = jwtService.generateToken(admin);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/admins/promote/" + user.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        ExtendedUserResponseDto userProfileDto = mapper.readValue(response, ExtendedUserResponseDto.class);

        Assertions.assertTrue(userProfileDto.getRoles().contains(Role.ADMIN));
        Assertions.assertFalse(roles.contains(Role.ADMIN));
    }

    @Test
    void promoteToAdminThenDemoteAgain_Expect200AndUpdatedUser() throws Exception {
        User user = userRepository.findFirstByRolesContains(Role.USER).orElseThrow();
        var roles = user.getRoles();
        User admin = userRepository.findFirstByRolesContains(Role.ADMIN).orElseThrow();

        String token = jwtService.generateToken(admin);
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch("/api/v1/admins/promote/" + user.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        ExtendedUserResponseDto userProfileDto = mapper.readValue(response, ExtendedUserResponseDto.class);

        Assertions.assertTrue(userProfileDto.getRoles().contains(Role.ADMIN));
        Assertions.assertFalse(roles.contains(Role.ADMIN));

        RequestBuilder requestBuilder1 = MockMvcRequestBuilders.patch("/api/v1/admins/demote/" + user.getId())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(null));

        String response1 = mockMvc.perform(requestBuilder1)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        ExtendedUserResponseDto userProfileDtoDemoted = mapper.readValue(response1, ExtendedUserResponseDto.class);

        Assertions.assertFalse(userProfileDtoDemoted.getRoles().contains(Role.ADMIN));
    }
}
