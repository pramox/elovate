package com.tuwien.elovate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.TestData;
import com.tuwien.elovate.dataloader.DataClearer;
import com.tuwien.elovate.dtos.game.GameDetailDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Genre;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RequiredArgsConstructor
@WebAppConfiguration
@SpringBootTest
@ActiveProfiles("test")
class GameControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GameAccessRepository gameAccessRepository;

    @Autowired
    GameRepository gameRepository;


    @Autowired
    private GameApplicationRepository gameApplicationRepository;

    private User user, developer, admin;
    private Game game;

    private MockMvc mockMvc;

    private ObjectMapper mapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private DataClearer dataClearer;

    private static final String URL = TestData.BASE_URL + TestData.GAME_URL;

    @BeforeEach
    void setup() throws IOException {
        this.mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        dataClearer.clear();

        user = User.builder()
                .nickName("user")
                .email("user@test.com")
                .roles(Set.of(Role.USER))
                .password("password")
                .build();

        developer = userRepository.save(user);

        developer = User.builder()
                .nickName("developer")
                .email("developer@test.com")
                .roles(Set.of(Role.GAME_DEVELOPER))
                .password("password")
                .build();

        developer = userRepository.save(developer);

        Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/correct_ratio.png");
        byte[] content = Files.readAllBytes(path);
        Image image = new Image(null, content, "image", "image",
                "image/jpeg", false, 0);

        GameApplication gameApplication = GameApplication.builder()
                .name("Really Cool Game")
                .genre(Genre.RACING)
                .developer(developer)
                .status(GameApplicationStatus.ACCEPTED)
                .drawPossible(false)
                .playersPerTeam(1)
                .image(image)
                .build();

        gameApplication = gameApplicationRepository.save(gameApplication);
        developer.setGameApplications(new HashSet<>(Set.of(gameApplication)));
        developer = userRepository.save(developer);
        game = Game.builder()
                .name("Really Cool Game")
                .genre(Genre.RACING)
                .drawPossible(false)
                .playersPerTeam(1)
                .gameApplication(gameApplication)
                .build();
        game = gameRepository.save(game);
        game.setImage(gameApplication.getImage());
        gameApplication.setGame(game);
        gameApplicationRepository.save(gameApplication);

        admin = User.builder()
                .nickName("admin")
                .email("admin@test.com")
                .roles(Set.of(Role.ADMIN))
                .password("password")
                .build();
        admin = userRepository.save(admin);

        mapper = new ObjectMapper();
    }

    @Test
    void get_gameById_returns_200() throws Exception {
        String token = jwtService.generateToken(developer);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "/" + game.getId())
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        GameDetailDto responseDto = mapper.readValue(response, GameDetailDto.class);
        Assertions.assertNotNull(responseDto);
        Assertions.assertEquals(game.getId(), responseDto.getId());
        Assertions.assertEquals(game.getName(), responseDto.getName());
        Assertions.assertEquals(game.getGenre(), responseDto.getGenre());
        Assertions.assertEquals(game.getPlayersPerTeam(), responseDto.getPlayersPerTeam());
    }

    @Test
    void get_allGames_returns_200() throws Exception {
        List<Game> expectedGames = new LinkedList<>();
        expectedGames.add(game);
        expectedGames.addAll(createGameList(9));
        String token = jwtService.generateToken(developer);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "?activated=false")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        List<GameDetailDto> responseDtos = mapper.readValue(response,
                mapper.getTypeFactory().constructCollectionType(List.class, GameDetailDto.class));
        Assertions.assertNotNull(responseDtos);
        Assertions.assertEquals(expectedGames.size(), responseDtos.size());
        for (GameDetailDto responseDto : responseDtos) {
            Assertions.assertNotNull(responseDto);
        }
    }

    @Test
    void get_allActivatedGames_returns_200() throws Exception {
        List<Game> expectedGames = new LinkedList<>();
        expectedGames.add(game);
        expectedGames.addAll(createGameList(9));
        Game expected = expectedGames.get(4);
        user.setConnectedGames(new HashSet<>(Set.of(expected)));
        userRepository.save(user);
        String token = jwtService.generateToken(user);

        RequestBuilder request = MockMvcRequestBuilders.get(URL + "?activated=true")
                .header("Authorization", "Bearer " + token);
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<GameDetailDto> responseDtos = mapper.readValue(response,
                mapper.getTypeFactory().constructCollectionType(List.class, GameDetailDto.class));
        Assertions.assertNotNull(responseDtos);
        Assertions.assertNotEquals(expectedGames.size(), responseDtos.size());
        Assertions.assertEquals(1, responseDtos.size());
        Assertions.assertEquals(expected.getId(), responseDtos.get(0).getId());
        Assertions.assertEquals(expected.getName(), responseDtos.get(0).getName());
        Assertions.assertEquals(expected.getGenre(), responseDtos.get(0).getGenre());
        Assertions.assertEquals(expected.getPlayersPerTeam(), responseDtos.get(0).getPlayersPerTeam());
    }

    private List<Game> createGameList(int i) throws IOException {
        LinkedList<Game> list = new LinkedList<>();
        for (int j = 0; j < i; j++) {
            Path path = Paths.get("./src/test/java/com/tuwien/elovate/resources/correct_ratio.png");
            byte[] content = Files.readAllBytes(path);
            Image image = new Image(null, content, "image", "image",
                    "image/jpeg", false, 0);

            GameApplication gameApplication = GameApplication.builder()
                    .name("Really Cool Game " + i)
                    .genre(Genre.MOBA)
                    .developer(developer)
                    .status(GameApplicationStatus.ACCEPTED)
                    .drawPossible(false)
                    .playersPerTeam(1)
                    .image(image)
                    .build();

            gameApplication = gameApplicationRepository.save(gameApplication);
            Set<GameApplication> gameApplications = developer.getGameApplications();
            gameApplications.add(gameApplication);
            developer.setGameApplications(gameApplications);
            developer = userRepository.save(developer);
            Game newGame = Game.builder()
                    .name(gameApplication.getName())
                    .genre(gameApplication.getGenre())
                    .drawPossible(gameApplication.isDrawPossible())
                    .playersPerTeam(gameApplication.getPlayersPerTeam())
                    .gameApplication(gameApplication)
                    .build();
            newGame = gameRepository.save(newGame);
            newGame.setImage(gameApplication.getImage());
            gameApplication.setGame(newGame);
            gameApplicationRepository.save(gameApplication);

            list.add(newGame);
        }
        return list;
    }
}