package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tuwien.elovate.TestData;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationContractDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationRequestDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDetailDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponsePagedDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationUpdateRequestDto;
import com.tuwien.elovate.dtos.gameapplication.comment.CommentRequestDto;
import com.tuwien.elovate.dtos.gameapplication.comment.CommentResponseDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Genre;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@SpringBootTest
@AutoConfigureMockMvc
class GameApplicationControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private GameApplicationRepository gameApplicationRepository;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataClearer dataClearer;

    private User developer;

    private User admin;

    private GameApplication gameApplication;

    private static final String URL = TestData.BASE_URL + TestData.GAME_APPLICATION_URL;

    @BeforeEach
    void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        dataClearer.clear();


        developer = User.builder()
                .nickName("developer")
                .email("developer@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER))
                .password("password")
                .build();

        developer = userRepository.save(developer);

        gameApplication = GameApplication.builder()
                .name("Really Cool Game")
                .genre(Genre.RACING)
                .developer(developer)
                .status(GameApplicationStatus.PENDING)
                .drawPossible(false)
                .playersPerTeam(1)
                .build();

        gameApplication = gameApplicationRepository.save(gameApplication);
        developer.setGameApplications(Set.of(gameApplication));
        developer = userRepository.save(developer);

        admin = User.builder()
                .nickName("admin")
                .email("admin@test.com")
                .roles(Set.of(Role.ADMIN))
                .password("password")
                .build();
        admin = userRepository.save(admin);
    }

    @Test
    void create_gameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(developer);

        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Cool Game")
                .genre(Genre.OTHER)
                .playersPerTeam(3)
                .drawPossible(false)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequest(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertNotNull(responseDto.getId());
        Assertions.assertNull(responseDto.getGameId());
    }


    @Test
    void create_gameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Really Cool Game")
                .genre(Genre.OTHER)
                .playersPerTeam(3)
                .drawPossible(false)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequest(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_NAME_ALREADY_TAKEN), response);
    }

    @Test
    void create_gameApplication_withImage_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(developer);

        Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/correct_ratio.png");
        byte[] content = Files.readAllBytes(path);
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg",
                MediaType.IMAGE_PNG_VALUE, content);

        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Cool Game")
                .genre(Genre.OTHER)
                .playersPerTeam(4)
                .drawPossible(false)
                .image(image)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequestWithImage(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertNotNull(responseDto.getId());
        Assertions.assertNotNull(responseDto.getImage());
        Assertions.assertEquals("data:image/png;base64," + Base64.getEncoder().encodeToString(image.getBytes()),
                responseDto.getImage());
    }

    @Test
    void create_gameApplication_withTooBigImage_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/correct_ratio_3MB.jpg");
        byte[] content = Files.readAllBytes(path);
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg",
                MediaType.IMAGE_JPEG_VALUE, content);
        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Cool Game")
                .genre(Genre.OTHER)
                .drawPossible(false)
                .playersPerTeam(4)
                .image(image)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequestWithImage(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.FILE_SIZE_LIMIT_EXCEEDED.getMessage()));
    }

    @Test
    void create_gameApplication_withWrongFileType_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/correct_ratio.pdf");
        byte[] content = Files.readAllBytes(path);
        MockMultipartFile image = new MockMultipartFile("image", "image.pdf",
                MediaType.APPLICATION_PDF_VALUE, content);
        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Cool Game")
                .genre(Genre.OTHER)
                .drawPossible(false)
                .playersPerTeam(4)
                .image(image)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequestWithImage(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.FILE_EXTENSION_WRONG.getMessage()));
    }

    @Test
    void create_gameApplication_withWrongRatio_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/wrong_ratio.png");
        byte[] content = Files.readAllBytes(path);
        MockMultipartFile image = new MockMultipartFile("image", "image.png",
                MediaType.IMAGE_PNG_VALUE, content);
        GameApplicationRequestDto newGameApplicationDto = GameApplicationRequestDto.builder()
                .name("Cool Game")
                .genre(Genre.OTHER)
                .drawPossible(false)
                .playersPerTeam(4)
                .image(image)
                .build();
        RequestBuilder request = prepareGameApplicationCreateRequestWithImage(token, newGameApplicationDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(Message.FILE_ASPECT_RATIO_WRONG.getMessage()));
    }


    @Test
    void get_allPendingGameApplications_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(developer);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "?page=0&size=1&status=PENDING")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponsePagedDto responseDto = mapper.readValue(response, GameApplicationResponsePagedDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(0, responseDto.getPageNumber());
        Assertions.assertEquals(1, responseDto.getPageSize());
        Assertions.assertEquals(1, responseDto.getTotalElements());
    }

    @Test
    void admin_getsAllPendingGameApplications_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "?page=0&size=1&status=PENDING")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponsePagedDto responseDto = mapper.readValue(response, GameApplicationResponsePagedDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(0, responseDto.getPageNumber());
        Assertions.assertEquals(1, responseDto.getPageSize());
        Assertions.assertEquals(1, responseDto.getTotalElements());
    }

    @Test
    void admin_getsAllPendingGameApplicationsByUserId_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        RequestBuilder request = MockMvcRequestBuilders.get(URL
                        + "?page=0&size=1&status=PENDING&developerId=" + developer.getId())
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponsePagedDto responseDto = mapper.readValue(response, GameApplicationResponsePagedDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(0, responseDto.getPageNumber());
        Assertions.assertEquals(1, responseDto.getPageSize());
        Assertions.assertEquals(1, responseDto.getTotalElements());
    }

    @Test
    void get_gameApplicationById_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(developer);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + gameApplication.getId())
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDetailDto responseDto = mapper.readValue(response, GameApplicationResponseDetailDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals("Really Cool Game", responseDto.getName());
        Assertions.assertEquals(Genre.RACING, responseDto.getGenre());
        Assertions.assertEquals(GameApplicationStatus.PENDING, responseDto.getStatus());
    }

    @Test
    void get_gameApplicationById_userNotAllowed_returns_404() throws Exception {
        User developer2 = User.builder()
                .nickName("developer2")
                .email("developer2@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER))
                .password("password")
                .build();

        developer2 = userRepository.save(developer2);
        String token = jwtService.generateToken(developer2);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + gameApplication.getId())
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();


        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_NOT_FOUND), response);
    }

    @Test
    void admin_accepts_gameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals("Really Cool Game", responseDto.getName());
        Assertions.assertEquals(Genre.RACING, responseDto.getGenre());
        Assertions.assertEquals(GameApplicationStatus.ACCEPTED, responseDto.getStatus());

        Optional<GameApplication> opt = gameApplicationRepository.findById(responseDto.getId());
        Game game = opt.orElseThrow(Exception::new).getGame();
        Assertions.assertNotNull(game);
        Assertions.assertEquals("Really Cool Game", game.getName());
        Assertions.assertEquals(Genre.RACING, game.getGenre());
    }

    @Test
    void admin_accepts_gameApplication_userBecomesGameDeveloper() throws Exception {
        User user = User.builder()
                .nickName("user")
                .email("user@test.com")
                .roles(Set.of(Role.USER))
                .password("password")
                .build();
        user = userRepository.save(user);
        GameApplication gameApplication2 = GameApplication.builder()
                .name("Another Cool Game")
                .genre(Genre.FIRST_PERSON_SHOOTER)
                .developer(user)
                .status(GameApplicationStatus.PENDING)
                .drawPossible(false)
                .playersPerTeam(5)
                .build();
        gameApplication2 = gameApplicationRepository.save(gameApplication2);
        user.setGameApplications(Set.of(gameApplication2));
        userRepository.save(user);

        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication2.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals("Another Cool Game", responseDto.getName());
        Assertions.assertEquals(Genre.FIRST_PERSON_SHOOTER, responseDto.getGenre());
        Assertions.assertEquals(GameApplicationStatus.ACCEPTED, responseDto.getStatus());
        Assertions.assertNotNull(responseDto.getGameId());

        Optional<GameApplication> opt = gameApplicationRepository.findById(responseDto.getId());
        Game game = opt.orElseThrow(Exception::new).getGame();
        Assertions.assertNotNull(game);
        Assertions.assertEquals("Another Cool Game", game.getName());
        Assertions.assertEquals(Genre.FIRST_PERSON_SHOOTER, game.getGenre());

        Optional<User> optUser = userRepository.findById(user.getId());
        Set<Role> roles = optUser.orElseThrow(Exception::new).getRoles();
        Assertions.assertTrue(roles.contains(Role.GAME_DEVELOPER));
    }

    @Test
    void developer_accepts_gameApplication_returns_403() throws Exception {
        String token = jwtService.generateToken(developer);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertNotNull(response);
    }

    @Test
    void admin_accepts_alreadyAcceptedGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.ACCEPTED);
        gameApplicationRepository.save(gameApplication);
        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_ALREADY_ACCEPTED), response);
    }

    @Test
    void admin_accepts_rejectedGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.REJECTED);
        gameApplicationRepository.save(gameApplication);
        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_CANT_TRANSITION_REJECTED_ACCEPTED),
                response);
    }

    @Test
    void admin_rejects_gameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.REJECTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals("Really Cool Game", responseDto.getName());
        Assertions.assertEquals(Genre.RACING, responseDto.getGenre());
        Assertions.assertEquals(GameApplicationStatus.REJECTED, responseDto.getStatus());

        Optional<GameApplication> opt = gameApplicationRepository.findById(responseDto.getId());
        Game game = opt.orElseThrow(Exception::new).getGame();
        Assertions.assertNull(game);
    }

    @Test
    void developer_rejects_gameApplication_returns_403() throws Exception {
        String token = jwtService.generateToken(developer);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.REJECTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertNotNull(response);
    }

    @Test
    void admin_rejects_alreadyRejectedGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.REJECTED);
        gameApplicationRepository.save(gameApplication);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.REJECTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_ALREADY_REJECTED),
                response);
    }

    @Test
    void admin_rejects_acceptedGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.ACCEPTED);
        gameApplicationRepository.save(gameApplication);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.REJECTED)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_REJECTED),
                response);
    }

    @Test
    void admin_resets_rejectedGameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.REJECTED);
        gameApplicationRepository.save(gameApplication);
        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.PENDING)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameApplicationResponseDto responseDto = mapper.readValue(response, GameApplicationResponseDto.class);

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals("Really Cool Game", responseDto.getName());
        Assertions.assertEquals(Genre.RACING, responseDto.getGenre());
        Assertions.assertEquals(GameApplicationStatus.PENDING, responseDto.getStatus());
    }

    @Test
    void developer_resets_gameApplication_returns_403() throws Exception {
        String token = jwtService.generateToken(developer);

        gameApplication.setStatus(GameApplicationStatus.REJECTED);
        gameApplicationRepository.save(gameApplication);

        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.PENDING)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isForbidden())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertNotNull(response);
    }

    @Test
    void admin_resets_pendingGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.PENDING);
        gameApplicationRepository.save(gameApplication);
        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.PENDING)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_ALREADY_PENDING),
                response);
    }

    @Test
    void admin_resets_acceptedGameApplication_returns_422() throws Exception {
        String token = jwtService.generateToken(admin);

        gameApplication.setStatus(GameApplicationStatus.ACCEPTED);
        gameApplicationRepository.save(gameApplication);
        GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.PENDING)
                .build();
        RequestBuilder request = prepareGameApplicationUpdateRequest(token, gameApplication.getId(),
                gameApplicationUpdateRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_PENDING),
                response);
    }

    @Test
    void admin_reviews_gameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String token = jwtService.generateToken(admin);
        String comment = "I reviewed this game application";

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .commentContent(comment)
                .build();
        RequestBuilder request = prepareCommentOnGameApplicationRequest(token, gameApplication.getId(),
                commentRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentResponseDto> responseDto = mapper.readValue(response, GameApplicationResponseDetailDto.class)
                .getComments();

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(1, responseDto.size());

        CommentResponseDto commentResponseDto = responseDto.get(0);
        Assertions.assertEquals(comment, commentResponseDto.getContent());
        Assertions.assertEquals(admin.getNickName(), commentResponseDto.getNickName());
        Assertions.assertTrue(Instant.now().isAfter(commentResponseDto.getTimestamp()));
    }

    @Test
    void admin_reviews_gameApplicationWithTooLongContent_returns_422() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String token = jwtService.generateToken(admin);
        String comment = "A".repeat(1000);

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .commentContent(comment)
                .build();
        RequestBuilder request = prepareCommentOnGameApplicationRequest(token, gameApplication.getId(),
                commentRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        String expectedErrMessage = "Comment maximum length is 255 characters.";
        Assertions.assertTrue(response.contains(expectedErrMessage));
    }

    @Test
    void developer_comments_onGameApplication_successful() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        String token = jwtService.generateToken(developer);
        String comment = "I want a review, pls!";

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .commentContent(comment)
                .build();
        RequestBuilder request = prepareCommentOnGameApplicationRequest(token, gameApplication.getId(),
                commentRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentResponseDto> responseDto = mapper.readValue(response, GameApplicationResponseDetailDto.class)
                .getComments();

        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(1, responseDto.size());

        CommentResponseDto commentResponseDto = responseDto.get(0);
        Assertions.assertEquals(comment, commentResponseDto.getContent());
        Assertions.assertEquals(developer.getNickName(), commentResponseDto.getNickName());
        Assertions.assertTrue(Instant.now().isAfter(commentResponseDto.getTimestamp()));
    }

    @Test
    void developer_comments_onOtherDevelopersGameApplication_returns_404() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        User developer2 = User.builder()
                .nickName("developer2")
                .email("developer2@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER))
                .password("password")
                .build();

        developer2 = userRepository.save(developer2);
        String token = jwtService.generateToken(developer2);
        String comment = "I want a review, pls!";

        CommentRequestDto commentRequestDto = CommentRequestDto.builder()
                .commentContent(comment)
                .build();
        RequestBuilder request = prepareCommentOnGameApplicationRequest(token, gameApplication.getId(),
                commentRequestDto);
        String response = mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(getExpectedErrMessage(Message.GAME_APPLICATION_NOT_FOUND),
                response);
    }


    @Test
    void developer_creates_gameApplicationWithInvalidPlayersPerTeam_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        GameApplicationRequestDto requestDto = GameApplicationRequestDto.builder()
                .name("Test Game")
                .genre(Genre.OTHER)
                .playersPerTeam(0)
                .drawPossible(false)
                .build();

        RequestBuilder request = prepareGameApplicationCreateRequest(token, requestDto);
        String response = mockMvc.perform(request).andExpect(status().isUnprocessableEntity()).andReturn().getResponse().getContentAsString();
        Assertions.assertEquals(getExpectedErrMessage(Message.PLAYER_PER_TEAM_INVALID), response);
    }

    @Test
    void developer_creates_gameApplicationWithEmptyPlayersPerTeam_returns_422() throws Exception {
        String token = jwtService.generateToken(developer);

        GameApplicationRequestDto requestDto = GameApplicationRequestDto.builder()
                .name("Test Game")
                .genre(Genre.OTHER)
                .playersPerTeam(null)
                .drawPossible(false)
                .build();

        RequestBuilder request = prepareGameApplicationCreateRequest(token, requestDto);
        String response = mockMvc.perform(request).andExpect(status().isUnprocessableEntity()).andReturn().getResponse().getContentAsString();
        Assertions.assertEquals(getExpectedErrMessage(Message.PLAYERS_PER_TEAM_IS_NULL), response);
    }

    @Test
    void gameApplicationContract_can_be_found() throws Exception {
        String token = jwtService.generateToken(developer);
        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/getGameApplicationContract")
                .header("Authorization", "Bearer " + token)
                .with(csrf());
        String response = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        GameApplicationContractDto gameApplicationContractDto = new ObjectMapper().readValue(response, GameApplicationContractDto.class);
        Assertions.assertFalse(gameApplicationContractDto.getData().isEmpty());
    }

    /*
    PRIVATE HELPERS
     */

    private RequestBuilder prepareGameApplicationCreateRequest(String token,
                                                               GameApplicationRequestDto gameApplicationRequestDto) {
        return MockMvcRequestBuilders.post(URL)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", gameApplicationRequestDto.getName())
                .param("genre", gameApplicationRequestDto.getGenre().toString())
                .param("drawPossible", String.valueOf(gameApplicationRequestDto.isDrawPossible()))
                .param("playersPerTeam", gameApplicationRequestDto.getPlayersPerTeam() != null ? String.valueOf(gameApplicationRequestDto.getPlayersPerTeam()) : null)
                .with(csrf());
    }

    private RequestBuilder prepareGameApplicationCreateRequestWithImage(String token,
                                                                        GameApplicationRequestDto gameApplicationRequestDto) {
        return MockMvcRequestBuilders.multipart(URL).file((MockMultipartFile) gameApplicationRequestDto.getImage())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("name", gameApplicationRequestDto.getName())
                .param("genre", gameApplicationRequestDto.getGenre().toString())
                .param("playersPerTeam", gameApplicationRequestDto.getPlayersPerTeam().toString())
                .param("drawPossible", String.valueOf(gameApplicationRequestDto.isDrawPossible()))
                .with(csrf());
    }

    private RequestBuilder prepareGameApplicationUpdateRequest(String token, Long id,
                                                               GameApplicationUpdateRequestDto gameApplicationUpdateRequestDto) {
        return MockMvcRequestBuilders.put(URL + "/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("status", gameApplicationUpdateRequestDto.getStatus().toString())
                .with(csrf());
    }

    private RequestBuilder prepareCommentOnGameApplicationRequest(String token, Long id,
                                                                  CommentRequestDto commentRequestDto) {
        return MockMvcRequestBuilders.put(URL + "/" + id + "/comment")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .param("commentContent", commentRequestDto.getCommentContent())
                .with(csrf());
    }

    private String getExpectedErrMessage(Message message) {
        return "[{\"errorKey\":\""
                + message.name() + "\""
                + ",\"message\":\"" + message.getMessage()
                + "\"}]";
    }
}
