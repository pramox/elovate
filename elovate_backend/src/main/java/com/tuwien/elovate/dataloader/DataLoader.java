package com.tuwien.elovate.dataloader;

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

import com.tuwien.elovate.dtos.rating.Glicko2MatchResultDto;
import com.tuwien.elovate.dtos.rating.Glicko2RatingPeriodDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.gameapplication.comment.Comment;
import com.tuwien.elovate.entities.queue.QueuePlayer;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.enums.Genre;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.enums.UserStatus;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.gameapplication.comment.CommentRepository;
import com.tuwien.elovate.repositories.queue.QueuePlayerRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.game.GameService;
import com.tuwien.elovate.services.rating.glicko2.Glicko2Calculator;
import com.tuwien.elovate.services.users.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader {
    private static final String ENABLE_PROPERTY = "dataloader.load.enable";
    private static final String EXTENDED_PROPERTY = "dataloader.load.extended";
    private static final Random random = new Random();

    private final Environment environment;
    private final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private final ResourceLoader resourceLoader;

    // dataClearer has to be loaded so data is cleared before adding data
    private final DataClearer dataClearer;

    private final QueuePlayerRepository queuePlayerRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final FinishedGameRepository finishedGameRepository;
    private final PlayerStatsPostGameRepository playerStatsPostGameRepository;
    private final GameApplicationRepository gameApplicationRepository;

    private final GameService gameService;
    private final PasswordEncoder passwordEncoder;
    private final GameRepository gameRepository;

    private final GameAccessRepository gameAccessRepository;

    private final RatingRepository ratingRepository;
    private final RatingParametersRepository ratingParametersRepository;

    private static final int AMOUNT_OF_USERS = 50;
    private static final int AMOUNT_OF_USERS_EXTENDED = random.nextInt(300, 400);
    private static final int GAMES_PER_ADMIN = 50;
    private static final List<String> ADMIN_USER_NAMES = List.of("admin", "mischa", "philipp", "elias", "maximal", "maxernst", "samuel");
    private static final List<String> COUNTRY_CODES = List.of("AT", "DE", "EN", "RU", "NL");

    private String encodedPassword;

    /**
     * Loads data into the database if the environment-property dataloader.load.enable is set to "true".
     */
    @PostConstruct
    public void loadData() {
        encodedPassword = passwordEncoder.encode("1234");

        String propertyValue = environment.getProperty(ENABLE_PROPERTY);

        if (!Boolean.parseBoolean(propertyValue)) {
            return;
        }

        String extendedPropertyValue = environment.getProperty(EXTENDED_PROPERTY);
        boolean isExtended = Boolean.parseBoolean(extendedPropertyValue);

        int amountOfUsers = isExtended ? AMOUNT_OF_USERS_EXTENDED : AMOUNT_OF_USERS;

        loadAdmins();
        loadGameDevelopers();
        loadUsers(amountOfUsers);
        addFriendsAndBlocked(amountOfUsers);
        loadGameApplications();
        loadComments();
        loadGames();
        connectGames();
        loadQueuePlayers();
        loadRatingParametersAndRatings();
        loadFinishedGames(GAMES_PER_ADMIN);
    }

    private void loadAdmins() {
        for (String userName : ADMIN_USER_NAMES) {
            Resource resource = resourceLoader.getResource("classpath:images/profilepictures/" + userName + ".jpg");
            byte[] imageBytes = new byte[0];
            try {
                imageBytes = Files.readAllBytes(resource.getFile().toPath());
            } catch (IOException ignored) {
                // ignored
            }
            User admin = new User();
            admin.setRoles(Set.of(Role.ADMIN, Role.GAME_DEVELOPER, Role.USER));
            admin.setEmail(userName + "@email.com");
            admin.setPassword(encodedPassword);
            admin.setNickName(userName);
            admin.setStatus(UserStatus.NORMAL);
            admin.setCountryCode(Math.random() > 0.5 ? "AT" : "DE");
            admin.setGameApplications(new HashSet<>());
            admin.setFriends(new HashSet<>());
            admin.setBlocked(new HashSet<>());
            admin.setConnectedGames(new HashSet<>());
            admin.setComments(new HashSet<>());
            Image image = new Image();
            image.setFileContent(imageBytes);
            image.setEmpty(false);
            image.setContentType(MediaType.IMAGE_JPEG_VALUE);
            image.setName(userName);
            admin.setImage(image);
            userService.save(admin);
        }
    }

    private void loadGameDevelopers() {
        Resource resource = resourceLoader.getResource("classpath:images/valve.jpg");
        byte[] imageBytes = new byte[0];
        try {
            imageBytes = Files.readAllBytes(resource.getFile().toPath());
        } catch (IOException ignored) {
            // ignored
        }
        User gameDeveloper = new User();
        gameDeveloper.setRoles(Set.of(Role.GAME_DEVELOPER, Role.USER));
        gameDeveloper.setEmail("developer@email.com");
        gameDeveloper.setPassword(encodedPassword);
        gameDeveloper.setNickName("gamedeveloper");
        gameDeveloper.setStatus(UserStatus.NORMAL);
        gameDeveloper.setCountryCode("AT");
        gameDeveloper.setGameApplications(new HashSet<>());
        gameDeveloper.setFriends(new HashSet<>());
        gameDeveloper.setBlocked(new HashSet<>());
        gameDeveloper.setConnectedGames(new HashSet<>());
        gameDeveloper.setComments(new HashSet<>());
        Image image = new Image();
        image.setFileContent(imageBytes);
        image.setEmpty(false);
        image.setContentType(MediaType.IMAGE_JPEG_VALUE);
        image.setName("valve");
        gameDeveloper.setImage(image);
        userService.save(gameDeveloper);
    }

    private void loadUsers(int amountOfUsers) {
        Resource resource = resourceLoader.getResource("classpath:images/profilepictures/users.jpg");
        byte[] imageBytes = new byte[0];
        try {
            imageBytes = Files.readAllBytes(resource.getFile().toPath());
        } catch (IOException ignored) {
            // ignored
        }
        List<User> users = new ArrayList<>();
        for (int i = 0; i <= amountOfUsers; i++) {
            User player = new User();
            player.setRoles(Set.of(Role.USER));
            player.setEmail("user" + (i > 0 ? i : "") + "@email.com");
            player.setPassword(encodedPassword);
            player.setNickName("user" + (i > 0 ? i : ""));
            player.setStatus(UserStatus.NORMAL);
            player.setCountryCode(COUNTRY_CODES.get(random.nextInt(0, COUNTRY_CODES.size())));
            player.setFriends(new HashSet<>());
            player.setBlocked(new HashSet<>());
            player.setConnectedGames(new HashSet<>());
            Image image = new Image();
            image.setFileContent(imageBytes);
            image.setEmpty(false);
            image.setContentType(MediaType.IMAGE_JPEG_VALUE);
            image.setName("user_profile_pic_" + i);
            player.setImage(image);
            users.add(player);
        }
        userRepository.saveAll(users);
    }

    private void addFriendsAndBlocked(int amountOfUsers) {
        List<User> allUsers = userRepository.findAll();
        int maxCount = Math.min(25, amountOfUsers / 2);

        for (User currentUser : allUsers) {
            Collections.shuffle(allUsers);

            int count = random.nextInt(1, maxCount);

            Set<User> friends = allUsers.stream()
                    .filter(u -> !u.equals(currentUser))
                    .limit(count)
                    .collect(Collectors.toSet());
            currentUser.setFriends(friends);

            Set<User> blocked = allUsers.stream()
                    .filter(u -> !u.equals(currentUser) && !friends.contains(u) && !u.isAdmin())
                    .limit(count)
                    .collect(Collectors.toSet());

            currentUser.setBlocked(blocked);
        }
        userRepository.saveAll(allUsers);
    }


    private void loadGameApplications() {
        GameApplication gameApplication = new GameApplication();
        gameApplication.setStatus(GameApplicationStatus.PENDING);
        gameApplication.setGenre(Genre.FIRST_PERSON_SHOOTER);
        gameApplication.setName("First Person Shooter");
        gameApplication.setDrawPossible(false);
        gameApplication.setPlayersPerTeam(1);

        Optional<User> gameDeveloperOpt = userRepository.findByNickName("gamedeveloper");

        // game applications need at least one persisted Developer!
        assert (gameDeveloperOpt.isPresent());
        gameDeveloperOpt.ifPresent(gameApplication::setDeveloper);

        gameApplicationRepository.save(gameApplication);

        GameApplication leagueOfLegends = new GameApplication();
        leagueOfLegends.setStatus(GameApplicationStatus.ACCEPTED);
        leagueOfLegends.setGenre(Genre.MOBA);
        leagueOfLegends.setName("League of Legends");
        leagueOfLegends.setDrawPossible(false);
        leagueOfLegends.setDeveloper(gameDeveloperOpt.get());
        leagueOfLegends.setPlayersPerTeam(5);
        gameApplicationRepository.save(leagueOfLegends);

        GameApplication fifa = new GameApplication();
        fifa.setStatus(GameApplicationStatus.ACCEPTED);
        fifa.setGenre(Genre.SPORTS);
        fifa.setName("FIFA 23");
        fifa.setDrawPossible(false);
        fifa.setDeveloper(gameDeveloperOpt.get());
        fifa.setPlayersPerTeam(1);
        gameApplicationRepository.save(fifa);

        GameApplication hearthstone = new GameApplication();
        hearthstone.setStatus(GameApplicationStatus.ACCEPTED);
        hearthstone.setGenre(Genre.FIRST_PERSON_SHOOTER);
        hearthstone.setName("Hearthstone");
        hearthstone.setDrawPossible(false);
        hearthstone.setDeveloper(gameDeveloperOpt.get());
        hearthstone.setPlayersPerTeam(1);
        gameApplicationRepository.save(hearthstone);

        GameApplication f1 = new GameApplication();
        f1.setStatus(GameApplicationStatus.ACCEPTED);
        f1.setGenre(Genre.RACING);
        f1.setName("F1 2023");
        f1.setDrawPossible(false);
        f1.setDeveloper(gameDeveloperOpt.get());
        f1.setPlayersPerTeam(1);
        gameApplicationRepository.save(f1);

        GameApplication rocketLeague = new GameApplication();
        rocketLeague.setStatus(GameApplicationStatus.ACCEPTED);
        rocketLeague.setGenre(Genre.OTHER);
        rocketLeague.setName("Rocket League");
        rocketLeague.setDrawPossible(false);
        rocketLeague.setDeveloper(gameDeveloperOpt.get());
        rocketLeague.setPlayersPerTeam(3);
        gameApplicationRepository.save(rocketLeague);

        GameApplication csgo = new GameApplication();
        csgo.setStatus(GameApplicationStatus.ACCEPTED);
        csgo.setGenre(Genre.FIRST_PERSON_SHOOTER);
        csgo.setName("Counter Strike Global Offensive");
        csgo.setDrawPossible(false);
        csgo.setDeveloper(gameDeveloperOpt.get());
        csgo.setPlayersPerTeam(5);
        gameApplicationRepository.save(csgo);

        GameApplication warcraftClone = new GameApplication();
        warcraftClone.setStatus(GameApplicationStatus.REJECTED);
        warcraftClone.setGenre(Genre.REAL_TIME_STRATEGY);
        warcraftClone.setName("Warcraft 666: Totally my Game");
        warcraftClone.setDrawPossible(false);
        warcraftClone.setDeveloper(gameDeveloperOpt.get());
        warcraftClone.setPlayersPerTeam(2);
        gameApplicationRepository.save(warcraftClone);

        GameApplication pubg = new GameApplication();
        pubg.setStatus(GameApplicationStatus.PENDING);
        pubg.setGenre(Genre.THIRD_PERSON_SHOOTER);
        pubg.setName("PUBG: Battlegrounds");
        pubg.setDrawPossible(false);
        pubg.setDeveloper(gameDeveloperOpt.get());
        pubg.setPlayersPerTeam(1);
        gameApplicationRepository.save(pubg);

        GameApplication overcooked = new GameApplication();
        overcooked.setStatus(GameApplicationStatus.PENDING);
        overcooked.setGenre(Genre.OTHER);
        overcooked.setName("Overcooked");
        overcooked.setDrawPossible(false);
        overcooked.setDeveloper(gameDeveloperOpt.get());
        overcooked.setPlayersPerTeam(2);
        gameApplicationRepository.save(overcooked);

        GameApplication mk2 = new GameApplication();
        mk2.setStatus(GameApplicationStatus.ACCEPTED);
        mk2.setGenre(Genre.FIGHTING);
        mk2.setName("Mortal Kombat II 3v3");
        mk2.setDrawPossible(true);
        mk2.setDeveloper(gameDeveloperOpt.get());
        mk2.setPlayersPerTeam(3);
        gameApplicationRepository.save(mk2);
    }

    private void loadComments() {
        Optional<User> adminOpt = userRepository.findFirstByRolesContains(Role.ADMIN);

        Comment comment = new Comment();
        comment.setContent("Well I really don't know");
        comment.setTimestamp(Instant.now().minus(10, ChronoUnit.MINUTES));
        adminOpt.ifPresent(comment::setUser);

        Optional<GameApplication> optPending = gameApplicationRepository.findFirstPendingApplication();

        Comment comment2 = new Comment();
        comment2.setContent("I don't know either!");
        comment2.setTimestamp(Instant.now().minus(5, ChronoUnit.MINUTES));
        optPending.ifPresent(gameApplication -> comment2.setUser(gameApplication.getDeveloper()));

        optPending.ifPresent(comment::setGameApplication);
        optPending.ifPresent(comment2::setGameApplication);
        optPending.ifPresent(gameApplication -> gameApplication.setComments(List.of(
                commentRepository.save(comment), commentRepository.save(comment2)
        )));
        if (optPending.isPresent()) {
            GameApplication gameApplication = optPending.get();
            gameApplicationRepository.save(gameApplication);
        }

        Optional<GameApplication> optRejected = gameApplicationRepository.findFirstRejectedApplication();

        Comment comment3 = new Comment();
        comment3.setContent("Do you honestly want me to believe, that you made this game?!");
        comment3.setTimestamp(Instant.now().minus(60, ChronoUnit.MINUTES));
        adminOpt.ifPresent(comment3::setUser);

        Comment comment4 = new Comment();
        comment4.setContent("Yes.");
        comment4.setTimestamp(Instant.now().minus(50, ChronoUnit.MINUTES));
        optRejected.ifPresent(gameApplication -> comment4.setUser(gameApplication.getDeveloper()));

        Comment comment5 = new Comment();
        comment5.setContent("Are you not even going to elaborate on this?!");
        comment5.setTimestamp(Instant.now().minus(40, ChronoUnit.MINUTES));
        adminOpt.ifPresent(comment5::setUser);

        Comment comment6 = new Comment();
        comment6.setContent("No.");
        comment6.setTimestamp(Instant.now().minus(30, ChronoUnit.MINUTES));
        optRejected.ifPresent(gameApplication -> comment6.setUser(gameApplication.getDeveloper()));

        Comment comment7 = new Comment();
        comment7.setContent("Then I am rejecting this application");
        comment7.setTimestamp(Instant.now().minus(10, ChronoUnit.MINUTES));
        adminOpt.ifPresent(comment7::setUser);

        optRejected.ifPresent(comment3::setGameApplication);
        optRejected.ifPresent(comment4::setGameApplication);
        optRejected.ifPresent(comment5::setGameApplication);
        optRejected.ifPresent(comment6::setGameApplication);
        optRejected.ifPresent(comment7::setGameApplication);
        optRejected.ifPresent(gameApplication -> gameApplication.setComments(List.of(
                commentRepository.save(comment3),
                commentRepository.save(comment4),
                commentRepository.save(comment5),
                commentRepository.save(comment6),
                commentRepository.save(comment7)
        )));
        if (optRejected.isPresent()) {
            GameApplication gameApplication = optRejected.get();
            gameApplicationRepository.save(gameApplication);
        }
    }

    private void loadGames() {
        List<GameApplication> gameApplications = gameApplicationRepository.findAllByStatus(GameApplicationStatus.ACCEPTED);

        for (GameApplication application : gameApplications) {
            Game game = new Game();
            game.setGameApplication(application);
            game.setGenre(application.getGenre());
            game.setName(application.getName());
            game.setDrawPossible(application.isDrawPossible());
            game.setPlayersPerTeam(application.getPlayersPerTeam());

            try {
                Resource resource = resourceLoader.getResource("classpath:images/games/" + application.getName() + ".jpg");
                byte[] imageBytes = Files.readAllBytes(resource.getFile().toPath());
                Image image = new Image(null, imageBytes, "image", application.getName() + "_image", "image/jpeg", false, 0);
                game.setImage(image);
                application.setImage(image);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
            application.setGame(gameService.save(game));
            gameApplicationRepository.save(application);
        }
    }

    private void connectGames() {
        List<Game> games = gameRepository.findAll();
        List<User> users = userRepository.findAll();

        Game leagueOfLegends = games.stream().filter(g -> g.getName().equals("League of Legends")).findFirst().get();
        Game mortalKombatII = games.stream().filter(g -> g.getName().equals("Mortal Kombat II 3v3")).findFirst().get();
        games.remove(leagueOfLegends);
        games.remove(mortalKombatII);
        int totalGames = games.size();

        for (User user : users) {
            Collections.shuffle(games);

            Set<Game> gamesToConnect;

            int minGames = totalGames / 2;
            int amountOfGames = random.nextInt(totalGames > 5 ? totalGames - 5 : minGames, totalGames - 1);
            List<Game> gamesForUser = new ArrayList<>(games.subList(0, amountOfGames));
            gamesForUser.add(leagueOfLegends); // connect all users to league
            gamesForUser.add(mortalKombatII);
            gamesToConnect = new HashSet<>(gamesForUser);

            user.setConnectedGames(gamesToConnect);

            for (Game game : gamesToConnect) {
                GameAccess access = new GameAccess();
                access.setUser(user);
                access.setGame(game);
                access.setUnlocked(true);
                gameAccessRepository.save(access);
            }
        }

        userRepository.saveAll(users);
    }

    public void loadQueuePlayers() {
        Optional<Long> gameIdOpt = gameRepository.findByName("League of Legends");
        if (gameIdOpt.isEmpty()) {
            throw new IllegalStateException("Dataloader failed, couldn't find game League of Legends");
        }
        Game game = gameRepository.getReferenceById(gameIdOpt.get());
        Instant timestamp = Instant.now();
        List<User> users = userRepository.findAll().stream().filter(user -> !user.isAdmin() && !user.isDeveloper()).limit(100).collect(Collectors.toList());
        for (User player : users) {
            int randSeconds = random.nextInt(5 * 60);
            long randRating = random.nextLong(500) + 1500;
            QueuePlayer qp = new QueuePlayer(null, null, timestamp.minus(Duration.ofSeconds(randSeconds)), player, game, (double) randRating);
            queuePlayerRepository.save(qp);
        }
        userRepository.saveAll(users);
    }

    private void loadRatingParametersAndRatings() {
        List<Game> gameList = gameRepository.findAll();

        for (Game game : gameList) {
            if (game.getName().equals("Mortal Kombat II 3v3")) {
                loadRatingParametersAndRatingsForShowcase();
                continue;
            }
            Glicko2RatingParameters ratingParameters = Glicko2RatingParameters.createNewRatingParameters();
            game.setRatingParameters(ratingParameters);

            ratingParameters.setDefaultRating(getRandomDouble(1000.0, 2000.0, 100.0));
            ratingParameters.setTau(getRandomDouble(0.2, 1.2, 0.01));
            ratingParameters.setDefaultRatingDeviation(getRandomDouble(150.0, 250.0, 10.0));
            ratingParameters.setDefaultRatingVolatility(getRandomDouble(0.03, 0.1, 0.01));
            ratingParameters.setMinRating(0.0);
            ratingParameters.setMaxRating(ratingParameters.getDefaultRating() + 5 * ratingParameters.getDefaultRatingDeviation());

            ratingParametersRepository.save(ratingParameters);
            gameRepository.save(game);

            List<GameAccess> gameAccesses = gameAccessRepository.findAllByGame(game);

            for (GameAccess access : gameAccesses) {
                Glicko2Rating rating = ratingParameters.createNewRating();

                rating.setRating(getRandomGaussianDouble(ratingParameters.getDefaultRating() - 3 * ratingParameters.getDefaultRatingDeviation(),
                        ratingParameters.getDefaultRating() + 3 * ratingParameters.getDefaultRatingDeviation(), ratingParameters.getDefaultRatingDeviation()));
                rating.setRatingDeviation(getRandomDouble(ratingParameters.getDefaultRatingDeviation() - 50,
                        ratingParameters.getDefaultRatingDeviation() + 50, 1.0));

                ratingRepository.save(rating);
                access.setRating(rating);
            }

            gameAccessRepository.saveAll(gameAccesses);
        }
    }

    public void loadRatingParametersAndRatingsForShowcase() {
        Game game = gameRepository.findAll().stream().filter(g -> g.getName().equals("Mortal Kombat II 3v3")).findFirst().get();

        Glicko2RatingParameters ratingParameters = Glicko2RatingParameters.createNewRatingParameters();
        game.setRatingParameters(ratingParameters);

        ratingParameters.setDefaultRating(1500.0);
        ratingParameters.setTau(0.75);
        ratingParameters.setDefaultRatingDeviation(200.0);
        ratingParameters.setDefaultRatingVolatility(0.06);
        ratingParameters.setMinRating(0.0);
        ratingParameters.setMaxRating(ratingParameters.getDefaultRating() + 5 * ratingParameters.getDefaultRatingDeviation());

        ratingParametersRepository.save(ratingParameters);
        gameRepository.save(game);

        List<GameAccess> gameAccesses = gameAccessRepository.findAllByGame(game);

        final List<Double> ratings = List.of(1957.0, 1936.0, 1949.0, 1946.0, 1904.0, 2021.0);
        final List<Double> deviations = List.of(125.0, 60.0, 55.0, 25.0, 250.0, 75.0);
        final List<Double> volatilities = List.of(0.06, 0.03, 0.025, 0.06, 0.09, 0.06);

        int i = 0;
        for (GameAccess access : gameAccesses) {
            if (access.getUser().isAdmin() && !access.getUser().getNickName().equals("admin")) {
                Glicko2Rating rating = ratingParameters.createNewRating();

                rating.setRating(ratings.get(i % ratings.size()));
                rating.setRatingDeviation(deviations.get(i % deviations.size()));
                rating.setRatingVolatility(volatilities.get(i % volatilities.size()));

                ratingRepository.save(rating);
                access.setRating(rating);

                ++i;
            } else {
                Glicko2Rating rating = ratingParameters.createNewRating();

                rating.setRating(getRandomGaussianDouble(ratingParameters.getDefaultRating() - 3 * ratingParameters.getDefaultRatingDeviation(),
                        ratingParameters.getDefaultRating() + 3 * ratingParameters.getDefaultRatingDeviation(), ratingParameters.getDefaultRatingDeviation()));
                rating.setRatingDeviation(getRandomDouble(ratingParameters.getDefaultRatingDeviation() - 50,
                        ratingParameters.getDefaultRatingDeviation() + 50, 1.0));

                ratingRepository.save(rating);
                access.setRating(rating);
            }
        }

        gameAccessRepository.saveAll(gameAccesses);
    }

    private void loadFinishedGames(int gamesPerAdmin) {
        List<User> users = userRepository.findAllByRolesContains(Role.ADMIN);
        users.removeIf(us -> us.getNickName().equals("admin"));

        Game game = gameRepository.findById(gameRepository.findByName("Mortal Kombat II 3v3").get()).get();
        List<GameAccess> accesses = gameAccessRepository.findAllByGame(game).stream().filter(ac -> users.contains(ac.getUser())).collect(Collectors.toList());

        Glicko2RatingParameters parameters = (Glicko2RatingParameters) game.getRatingParameters();
        Glicko2Calculator calculator = new Glicko2Calculator(parameters);

        List<FinishedGame> finishedGames = new ArrayList<>();

        for (int i = 1; i <= gamesPerAdmin; i++) {
            Collections.shuffle(accesses);

            List<Glicko2Rating> teamOne = accesses.subList(0, 3).stream().map(ac -> (Glicko2Rating) ac.getRating()).toList();
            List<Glicko2Rating> teamTwo = accesses.subList(3, 6).stream().map(ac -> (Glicko2Rating) ac.getRating()).toList();

            List<Double> ratingBeforeTeamOne = teamOne.stream().map(Rating::getRating).toList();
            List<Double> ratingBeforeTeamTwo = teamTwo.stream().map(Rating::getRating).toList();

            GameEndResult result = Math.random() > 0.5 ? GameEndResult.TEAM_A_WINNER : GameEndResult.TEAM_B_WINNER;

            Glicko2RatingPeriodDto ratingPeriodDto = new Glicko2RatingPeriodDto();
            ratingPeriodDto.setResults(List.of(
                    new Glicko2MatchResultDto(
                            teamOne,
                            teamTwo,
                            result
                    )
            ));
            calculator.calculateAndApplyRatingChange(ratingPeriodDto);

            List<PlayerStatsPostGame> playerStatsPostGameList = new ArrayList<>();

            for (int j = 0; j < 6; j++) {
                GameAccess access = accesses.get(j);
                double ratingBefore = j <= 2 ? ratingBeforeTeamOne.get(j) : ratingBeforeTeamTwo.get(j - 3);
                double ratingAfter = j <= 2 ? teamOne.get(j).getRating() : teamTwo.get(j - 3).getRating();
                if (i == gamesPerAdmin) {
                    ratingAfter = access.getRating().getRating();
                }
                PlayerStatsPostGame playerStatsPostGame = new PlayerStatsPostGame(null, access.getUser(), access.getUser().getId(), ratingBefore, ratingAfter, List.of());
                playerStatsPostGameList.add(playerStatsPostGame);
            }

            playerStatsPostGameRepository.saveAll(playerStatsPostGameList);

            LocalDateTime randomDate = LocalDateTime.of(2023, random.nextInt(1, 13), random.nextInt(1, 28), random.nextInt(0, 24), random.nextInt(0, 60));

            if (i == gamesPerAdmin) {
                randomDate = LocalDateTime.now().minusDays(10);
            }

            FinishedGame finishedGame = new FinishedGame(null, game, result, playerStatsPostGameList.subList(0, 3), playerStatsPostGameList.subList(3, 6), randomDate, null);

            finishedGames.add(finishedGame);
        }

        finishedGameRepository.saveAll(finishedGames);
        gameAccessRepository.saveAll(accesses);
    }

    private double getRandomDouble(double lowerBound, double upperBound, double stepSize) {
        int steps = (int) ((upperBound - lowerBound) / stepSize);
        int randomStep = random.nextInt(steps + 1);
        return lowerBound + (randomStep * stepSize);
    }

    private double getRandomGaussianDouble(double lowerBound, double upperBound, double deviation) {
        double value = random.nextGaussian() * deviation + (lowerBound + 0.5 * (upperBound - lowerBound));
        if (value < lowerBound) return lowerBound - Math.random() * deviation * 0.2;
        if (value > upperBound) return upperBound + Math.random() * deviation * 0.2;
        return value;
    }
}
