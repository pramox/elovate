package com.tuwien.elovate.rating;

import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.rating.RatingParameters;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.rating.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest
class RatingServiceTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RatingParametersRepository ratingParametersRepository;

    @Autowired
    private GameAccessRepository gameAccessRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinishedGameRepository finishedGameRepository;

    @Autowired
    private PlayerStatsPostGameRepository playerStatsPostGameRepository;

    @Test
    void shouldNotCrashWhenUpdateRatingCalledWithCorrectClasses() {
        RatingParameters<Glicko2Rating> parameters = Glicko2RatingParameters.createNewRatingParameters();
        List<Glicko2Rating> winnerRatings = List.of(parameters.createNewRating());
        List<Glicko2Rating> loserRatings = List.of(parameters.createNewRating());
        ratingService.updateRating(parameters, winnerRatings, loserRatings, GameEndResult.TEAM_A_WINNER, false);
        assertTrue(true, "Method executed without exception");
    }

    @Test
    void shouldNotCrashWhenUpdateRatingCalledWithCorrectClass() {
        RatingParameters<?> parameters = Glicko2RatingParameters.createNewRatingParameters();
        Rating winnerRating = parameters.createNewRating();
        Rating loserRating = parameters.createNewRating();
        ratingService.updateRating(parameters, winnerRating, loserRating, GameEndResult.TEAM_A_WINNER, false);
        assertTrue(true, "Method executed without exception");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateRatingCalledWithIncorrectLoserClasses() {
        RatingParameters<Glicko2Rating> parameters = Glicko2RatingParameters.createNewRatingParameters();
        List<Glicko2Rating> winnerRatings = List.of(parameters.createNewRating());
        List<Rating> loserRatings = List.of(new Rating() {
        });
        assertThrows(IllegalArgumentException.class, () -> ratingService.updateRating(parameters, winnerRatings, loserRatings, GameEndResult.TEAM_A_WINNER, false));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateRatingCalledWithIncorrectWinnerClasses() {
        RatingParameters<Glicko2Rating> parameters = Glicko2RatingParameters.createNewRatingParameters();
        List<Rating> winnerRatings = List.of(new Rating() {
        });
        List<Glicko2Rating> loserRatings = List.of(parameters.createNewRating());
        assertThrows(IllegalArgumentException.class, () -> ratingService.updateRating(parameters, winnerRatings, loserRatings, GameEndResult.TEAM_A_WINNER, false));

    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateRatingCalledWithIncorrectLoserClass() {
        RatingParameters<Glicko2Rating> parameters = Glicko2RatingParameters.createNewRatingParameters();
        Glicko2Rating winnerRating = parameters.createNewRating();
        Rating loserRating = new Rating() {
        };
        assertThrows(IllegalArgumentException.class, () -> ratingService.updateRating(parameters, winnerRating, loserRating, GameEndResult.TEAM_A_WINNER, false));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdateRatingCalledWithIncorrectWinnerClass() {
        RatingParameters<Glicko2Rating> parameters = Glicko2RatingParameters.createNewRatingParameters();
        Rating winnerRating = new Rating() {
        };
        Glicko2Rating loserRating = parameters.createNewRating();
        assertThrows(IllegalArgumentException.class, () -> ratingService.updateRating(parameters, winnerRating, loserRating, GameEndResult.TEAM_A_WINNER, false));
    }

    @Test
    void shouldCalculateRatingCorrectlyForSingleGameFromGlickmanPaper() {
        Glicko2RatingParameters parameters = Glicko2RatingParameters.createNewRatingParameters();

        Glicko2Rating playerOne = parameters.createNewRating();
        playerOne.setId(1L);
        playerOne.setRatingDeviation(200.0);

        Glicko2Rating playerTwo = parameters.createNewRating();
        playerTwo.setId(2L);
        playerTwo.setRatingDeviation(30.0);
        playerTwo.setRating(1400.0);

        ratingService.updateRating(parameters, List.of(playerOne), List.of(playerTwo), GameEndResult.TEAM_A_WINNER, false);

        double expectedRating = 1563.564;
        double expectedRatingDeviation = 175.402;
        double expectedVolatility = 0.05999;

        double expectedRatingPlayerTwo = 1398.143;
        double expectedRatingDeviationPlayerTwo = 31.670;
        double expectedVolatilityPlayerTwo = 0.059999;

        assertEquals(expectedRating, playerOne.getRating(), 0.01, "Rating should match the expected value");
        assertEquals(expectedRatingDeviation, playerOne.getRatingDeviation(), 0.01, "Rating deviation should match the expected value");
        assertEquals(expectedVolatility, playerOne.getRatingVolatility(), 0.0001, "Volatility should match the expected value");

        assertEquals(expectedRatingPlayerTwo, playerTwo.getRating(), 0.01, "Rating for playerTwo should match the expected value");
        assertEquals(expectedRatingDeviationPlayerTwo, playerTwo.getRatingDeviation(), 0.01, "Rating deviation for playerTwo should match the expected value");
        assertEquals(expectedVolatilityPlayerTwo, playerTwo.getRatingVolatility(), 0.0001, "Volatility for playerTwo should match the expected value");
    }

    @Test
    void calculateConfidenceIntervalWorks() {
        Glicko2RatingParameters parameters = Glicko2RatingParameters.createNewRatingParameters();
        Glicko2Rating playerOne = parameters.createNewRating();
        playerOne.setId(1L);
        playerOne.setRatingDeviation(200.0);

        List<Double> testValues = List.of(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9);

        for (Double testValue : testValues) {
            Pair<Double, Double> interval = ratingService.getConfidenceInterval(parameters, playerOne, testValue);
            assertTrue(interval.getFirst() < playerOne.getRating());
            assertTrue(interval.getSecond() > playerOne.getRating());
        }
    }

    @Test
    @Transactional
    void inactiveRatingGetsIncreasedDeviation() {
        Glicko2RatingParameters parameters = Glicko2RatingParameters.createNewRatingParameters();
        parameters.setLastRatingPeriodAdjustment(LocalDateTime.now().minusDays(50));
        parameters.setRatingPeriod(Period.ofDays(5));

        Game game = new Game();
        game.setRatingParameters(parameters);

        ratingParametersRepository.save(parameters);
        gameRepository.save(game);

        User activeUser = new User();
        User inactiveUser = new User();

        userRepository.save(activeUser);
        userRepository.save(inactiveUser);

        Glicko2Rating activeRating = parameters.createNewRating();
        Glicko2Rating inactiveRating = parameters.createNewRating();

        ratingRepository.save(inactiveRating);
        ratingRepository.save(activeRating);

        GameAccess activeAccess = new GameAccess();
        GameAccess inactiveAccess = new GameAccess();

        activeAccess.setRating(activeRating);
        inactiveAccess.setRating(inactiveRating);
        activeAccess.setGame(game);
        inactiveAccess.setGame(game);
        activeAccess.setUser(activeUser);
        inactiveAccess.setUser(inactiveUser);

        gameAccessRepository.save(activeAccess);
        gameAccessRepository.save(inactiveAccess);

        FinishedGame newFinishedGame = new FinishedGame();
        newFinishedGame.setGame(game);
        newFinishedGame.setFinishTime(LocalDateTime.now().minusDays(1));

        FinishedGame oldFinishedGame = new FinishedGame();
        oldFinishedGame.setGame(game);
        oldFinishedGame.setFinishTime(LocalDateTime.now().minusDays(100));

        PlayerStatsPostGame activePlayerPostGameStats = new PlayerStatsPostGame();
        activePlayerPostGameStats.setUser(activeUser);
        activePlayerPostGameStats.setUserId(activeUser.getId());

        PlayerStatsPostGame inactivePlayerPostGameStats = new PlayerStatsPostGame();
        inactivePlayerPostGameStats.setUser(inactiveUser);
        inactivePlayerPostGameStats.setUserId(inactiveUser.getId());

        newFinishedGame.setTeamA(List.of(activePlayerPostGameStats));
        newFinishedGame.setTeamB(List.of(activePlayerPostGameStats));

        oldFinishedGame.setTeamA(List.of(inactivePlayerPostGameStats));
        oldFinishedGame.setTeamB(List.of(inactivePlayerPostGameStats));

        playerStatsPostGameRepository.save(activePlayerPostGameStats);
        playerStatsPostGameRepository.save(inactivePlayerPostGameStats);
        finishedGameRepository.save(newFinishedGame);
        finishedGameRepository.save(oldFinishedGame);

        ratingService.updateInactiveGlicko2Ratings();

        activeRating = (Glicko2Rating) ratingRepository.getReferenceById(activeRating.getId());
        inactiveRating = (Glicko2Rating) ratingRepository.getReferenceById(inactiveRating.getId());

        assertEquals(350.0, activeRating.getRatingDeviation(), 0.0001);
        assertTrue(inactiveRating.getRatingDeviation() > 350.0);
    }
}
