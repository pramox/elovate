package com.tuwien.elovate.rating;

import com.tuwien.elovate.dtos.rating.Glicko2MatchResultDto;
import com.tuwien.elovate.dtos.rating.Glicko2RatingPeriodDto;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.services.rating.glicko2.Glicko2Calculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingCalculatorTest {

    private Glicko2RatingParameters parameters;
    private Glicko2Calculator calculator;
    private Glicko2RatingPeriodDto glicko2RatingPeriodDto;
    private Glicko2Rating playerOne, playerTwo, playerThree, playerFour;

    @BeforeEach
    void setUp() {
        parameters = Glicko2RatingParameters.createNewRatingParameters();
        calculator = new Glicko2Calculator(parameters);
        glicko2RatingPeriodDto = new Glicko2RatingPeriodDto();

        playerOne = parameters.createNewRating();
        playerOne.setId(1L);
        playerOne.setRating(1500.0);
        playerOne.setRatingDeviation(200.0);

        playerTwo = parameters.createNewRating();
        playerTwo.setId(2L);
        playerTwo.setRating(1400.0);
        playerTwo.setRatingDeviation(30.0);

        playerThree = parameters.createNewRating();
        playerThree.setId(3L);
        playerThree.setRating(1550.0);
        playerThree.setRatingDeviation(100.0);

        playerFour = parameters.createNewRating();
        playerFour.setId(4L);
        playerFour.setRating(1700.0);
        playerFour.setRatingDeviation(300.0);
    }

    @Test
    void shouldCalculateRatingForPeriodFromGlickmanPaper() {
        Glicko2MatchResultDto result1 = new Glicko2MatchResultDto();
        result1.setResult(GameEndResult.TEAM_A_WINNER);
        result1.setTeamOne(List.of(playerOne));
        result1.setTeamTwo(List.of(playerTwo));

        Glicko2MatchResultDto result2 = new Glicko2MatchResultDto();
        result2.setResult(GameEndResult.TEAM_B_WINNER);
        result2.setTeamOne(List.of(playerOne));
        result2.setTeamTwo(List.of(playerThree));

        Glicko2MatchResultDto result3 = new Glicko2MatchResultDto();
        result3.setResult(GameEndResult.TEAM_B_WINNER);
        result3.setTeamOne(List.of(playerOne));
        result3.setTeamTwo(List.of(playerFour));

        glicko2RatingPeriodDto.setResults(List.of(
                result1,
                result2,
                result3
        ));

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        // these are the values that are calculated in the paper by Professor Mark E. Glickman
        double expectedRating = 1464.06;
        double expectedRatingDeviation = 151.52;
        double expectedVolatility = 0.05999;

        // compare our calculated values to the ones calculated in the paper
        assertEquals(expectedRating, playerOne.getRating(), 0.01, "Rating should match the expected value");
        assertEquals(expectedRatingDeviation, playerOne.getRatingDeviation(), 0.01, "Rating deviation should match the expected value");
        assertEquals(expectedVolatility, playerOne.getRatingVolatility(), 0.0001, "Volatility should match the expected value");
    }

    @Test
    void shouldCalculateRatingForSingleGameFromGlickmanPaper() {
        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(playerOne));
        glicko2MatchResultDto.setTeamTwo(List.of(playerTwo));
        glicko2RatingPeriodDto.setResults(
                List.of(
                        glicko2MatchResultDto
                )
        );

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

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
    void shouldUpdateRatingSameAmountForSingleGameWithTwoPlayers() {

        // Create players for team one
        Glicko2Rating playerOneTeamOne = parameters.createNewRating();
        playerOneTeamOne.setId(1L);

        Glicko2Rating playerTwoTeamOne = parameters.createNewRating();
        playerTwoTeamOne.setId(2L);

        // Create players for team two
        Glicko2Rating playerOneTeamTwo = parameters.createNewRating();
        playerOneTeamTwo.setId(3L);

        Glicko2Rating playerTwoTeamTwo = parameters.createNewRating();
        playerTwoTeamTwo.setId(4L);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(playerOneTeamOne, playerTwoTeamOne));
        glicko2MatchResultDto.setTeamTwo(List.of(playerOneTeamTwo, playerTwoTeamTwo));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        // Perform the rating calculation
        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        // Assertions to check that both players in each team have the same rating change
        assertEquals(playerOneTeamOne.getRating(), playerTwoTeamOne.getRating(), 0.01, "Both players in Team One should have the same rating");
        assertEquals(playerOneTeamOne.getRatingDeviation(), playerTwoTeamOne.getRatingDeviation(), 0.01, "Both players in Team One should have the same rating deviation");
        assertEquals(playerOneTeamOne.getRatingVolatility(), playerTwoTeamOne.getRatingVolatility(), 0.0001, "Both players in Team One should have the same volatility");

        assertEquals(playerOneTeamTwo.getRating(), playerTwoTeamTwo.getRating(), 0.01, "Both players in Team Two should have the same rating");
        assertEquals(playerOneTeamTwo.getRatingDeviation(), playerTwoTeamTwo.getRatingDeviation(), 0.01, "Both players in Team Two should have the same rating deviation");
        assertEquals(playerOneTeamTwo.getRatingVolatility(), playerTwoTeamTwo.getRatingVolatility(), 0.0001, "Both players in Team Two should have the same volatility");
    }

    @Test
    void shouldResultInConsistentChangeWithDifferentStartRatingsForTwoPlayers() {

        // Create players for team one with different initial ratings
        Glicko2Rating playerOneTeamOne = parameters.createNewRating();
        playerOneTeamOne.setId(1L);
        playerOneTeamOne.setRating(1500.0);

        Glicko2Rating playerTwoTeamOne = parameters.createNewRating();
        playerTwoTeamOne.setId(2L);
        playerTwoTeamOne.setRating(2000.0);

        // Create players for team two with different initial ratings
        Glicko2Rating playerOneTeamTwo = parameters.createNewRating();
        playerOneTeamTwo.setId(3L);
        playerOneTeamTwo.setRating(1600.0);

        Glicko2Rating playerTwoTeamTwo = parameters.createNewRating();
        playerTwoTeamTwo.setId(4L);
        playerTwoTeamTwo.setRating(2100.0);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(playerOneTeamOne, playerTwoTeamOne));
        glicko2MatchResultDto.setTeamTwo(List.of(playerOneTeamTwo, playerTwoTeamTwo));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        // Perform the rating calculation
        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        // Calculate expected new ratings
        double expectedRatingChange = 195.397; // calculated via https://glicko2-calculator.streamlit.app/

        // Assertions to check that the rating change is consistent across players
        assertEquals(1500.0 + expectedRatingChange, playerOneTeamOne.getRating(), 0.01, "Player One Team One rating change should be consistent");
        assertEquals(2000.0 + expectedRatingChange, playerTwoTeamOne.getRating(), 0.01, "Player Two Team One rating change should be consistent");
        assertEquals(1600.0 - expectedRatingChange, playerOneTeamTwo.getRating(), 0.01, "Player One Team Two rating change should be consistent");
        assertEquals(2100.0 - expectedRatingChange, playerTwoTeamTwo.getRating(), 0.01, "Player Two Team Two rating change should be consistent");
    }

    @Test
    void shouldResultInConsistentChangeWithDifferentStartRatingsForThreePlayersAgainstHigherRatedTeam() {

        // Create players for team one with different initial ratings
        Glicko2Rating playerOneTeamOne = parameters.createNewRating();
        playerOneTeamOne.setId(1L);
        playerOneTeamOne.setRating(1500.0);

        Glicko2Rating playerTwoTeamOne = parameters.createNewRating();
        playerTwoTeamOne.setId(2L);
        playerTwoTeamOne.setRating(2000.0);

        Glicko2Rating playerThreeTeamOne = parameters.createNewRating();
        playerThreeTeamOne.setId(5L);
        playerThreeTeamOne.setRating(1800.0);

        // Create players for team two with 100 points more on average
        Glicko2Rating playerOneTeamTwo = parameters.createNewRating();
        playerOneTeamTwo.setId(3L);
        playerOneTeamTwo.setRating(1600.0);

        Glicko2Rating playerTwoTeamTwo = parameters.createNewRating();
        playerTwoTeamTwo.setId(4L);
        playerTwoTeamTwo.setRating(2100.0);

        Glicko2Rating playerThreeTeamTwo = parameters.createNewRating();
        playerThreeTeamTwo.setId(6L);
        playerThreeTeamTwo.setRating(1900.0);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(playerOneTeamOne, playerTwoTeamOne, playerThreeTeamOne));
        glicko2MatchResultDto.setTeamTwo(List.of(playerOneTeamTwo, playerTwoTeamTwo, playerThreeTeamTwo));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        // Perform the rating calculation
        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        double expectedRatingChange = 195.397; // calculated via https://glicko2-calculator.streamlit.app/

        // Assertions to check that the rating change is consistent across players
        assertEquals(1500.0 + expectedRatingChange, playerOneTeamOne.getRating(), 0.01, "Player One Team One rating change should be consistent");
        assertEquals(2000.0 + expectedRatingChange, playerTwoTeamOne.getRating(), 0.01, "Player Two Team One rating change should be consistent");
        assertEquals(1800.0 + expectedRatingChange, playerThreeTeamOne.getRating(), 0.01, "Player Three Team One rating change should be consistent");

        assertEquals(1600.0 - expectedRatingChange, playerOneTeamTwo.getRating(), 0.01, "Player One Team Two rating change should be consistent");
        assertEquals(2100.0 - expectedRatingChange, playerTwoTeamTwo.getRating(), 0.01, "Player Two Team Two rating change should be consistent");
        assertEquals(1900.0 - expectedRatingChange, playerThreeTeamTwo.getRating(), 0.01, "Player Three Team Two rating change should be consistent");
    }

    @Test
    void shouldResultInConsistentChangeWithDifferentStartRatingsForThreePlayersAgainstLowerRatedTeam() {

        // Create players for team one with different initial ratings (swapped)
        Glicko2Rating playerOneTeamOne = parameters.createNewRating();
        playerOneTeamOne.setId(1L);
        playerOneTeamOne.setRating(1600.0);

        Glicko2Rating playerTwoTeamOne = parameters.createNewRating();
        playerTwoTeamOne.setId(2L);
        playerTwoTeamOne.setRating(2100.0);

        Glicko2Rating playerThreeTeamOne = parameters.createNewRating();
        playerThreeTeamOne.setId(5L);
        playerThreeTeamOne.setRating(1900.0);

        // Create players for team two with different initial ratings (swapped)
        Glicko2Rating playerOneTeamTwo = parameters.createNewRating();
        playerOneTeamTwo.setId(3L);
        playerOneTeamTwo.setRating(1500.0);

        Glicko2Rating playerTwoTeamTwo = parameters.createNewRating();
        playerTwoTeamTwo.setId(4L);
        playerTwoTeamTwo.setRating(2000.0);

        Glicko2Rating playerThreeTeamTwo = parameters.createNewRating();
        playerThreeTeamTwo.setId(6L);
        playerThreeTeamTwo.setRating(1800.0);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(playerOneTeamOne, playerTwoTeamOne, playerThreeTeamOne));
        glicko2MatchResultDto.setTeamTwo(List.of(playerOneTeamTwo, playerTwoTeamTwo, playerThreeTeamTwo));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        // Calculate expected new ratings
        double expectedRatingChange = 132.938; // calculated via https://glicko2-calculator.streamlit.app/

        // Assertions to check that the rating change is consistent across players
        assertEquals(1600.0 + expectedRatingChange, playerOneTeamOne.getRating(), 0.01, "Player One Team One rating change should be consistent");
        assertEquals(2100.0 + expectedRatingChange, playerTwoTeamOne.getRating(), 0.01, "Player Two Team One rating change should be consistent");
        assertEquals(1900.0 + expectedRatingChange, playerThreeTeamOne.getRating(), 0.01, "Player Three Team One rating change should be consistent");

        assertEquals(1500.0 - expectedRatingChange, playerOneTeamTwo.getRating(), 0.01, "Player One Team Two rating change should be consistent");
        assertEquals(2000.0 - expectedRatingChange, playerTwoTeamTwo.getRating(), 0.01, "Player Two Team Two rating change should be consistent");
        assertEquals(1800.0 - expectedRatingChange, playerThreeTeamTwo.getRating(), 0.01, "Player Three Team Two rating change should be consistent");
    }

    @Test
    void shouldResultInNoRatingChangeWithLargeRatingDifference() {

        // Create two players with a large rating difference and set their rating deviations
        Glicko2Rating highRatedPlayer = parameters.createNewRating();
        highRatedPlayer.setId(1L);
        highRatedPlayer.setRating(2700.0);
        highRatedPlayer.setRatingDeviation(30.0);

        Glicko2Rating lowRatedPlayer = parameters.createNewRating();
        lowRatedPlayer.setId(2L);
        lowRatedPlayer.setRating(1000.0);
        lowRatedPlayer.setRatingDeviation(200.0);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_A_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(highRatedPlayer));
        glicko2MatchResultDto.setTeamTwo(List.of(lowRatedPlayer));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        assertEquals(2700.0, highRatedPlayer.getRating(), 0.1, "High rated player's rating should remain unchanged");
        assertEquals(1000.0, lowRatedPlayer.getRating(), 0.1, "Low rated player's rating should remain unchanged");
    }


    @Test
    void shouldResultInBigChangeIfLowerRatedPlayerWinsAgainstHighRatedPlayer() {

        // Create two players with a large rating difference
        Glicko2Rating highRatedPlayer = parameters.createNewRating();
        highRatedPlayer.setId(1L);
        highRatedPlayer.setRating(2700.0);
        highRatedPlayer.setRatingDeviation(60.0);

        Glicko2Rating lowRatedPlayer = parameters.createNewRating();
        lowRatedPlayer.setId(2L);
        lowRatedPlayer.setRating(1000.0);
        lowRatedPlayer.setRatingDeviation(90.0);

        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();

        // Set the match result and the teams
        glicko2MatchResultDto.setResult(GameEndResult.TEAM_B_WINNER);
        glicko2MatchResultDto.setTeamOne(List.of(highRatedPlayer));
        glicko2MatchResultDto.setTeamTwo(List.of(lowRatedPlayer));
        glicko2RatingPeriodDto.setResults(List.of(glicko2MatchResultDto));

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        // expected ratings calculated via https://glicko2-calculator.streamlit.app/
        assertEquals(2679.474, highRatedPlayer.getRating(), 0.1, "High rated player's rating should change a lot");
        assertEquals(1046.415, lowRatedPlayer.getRating(), 0.1, "Low rated player's rating should change a lot");
    }

    @Test
    void shouldCreateDefaultRatingCorrectly() {
        Glicko2Rating newPlayer = parameters.createNewRating();
        assertEquals(parameters.getDefaultRating(), newPlayer.getRating(), "Default rating should should match");
        assertEquals(parameters.getDefaultRatingDeviation(), newPlayer.getRatingDeviation(), "Default rating deviation should match");
        assertEquals(parameters.getDefaultRatingVolatility(), newPlayer.getRatingVolatility(), "Default rating volatility should match");
    }

    @Test
    void shouldNotChangeRatingWithNoMatches() {
        Glicko2RatingPeriodDto ratingPeriod = new Glicko2RatingPeriodDto();
        ratingPeriod.setResults(List.of());

        double originalRating = playerOne.getRating();
        double originalRatingDeviation = playerOne.getRatingDeviation();
        double originalVolatility = playerOne.getRatingVolatility();

        calculator.calculateAndApplyRatingChange(ratingPeriod);

        assertEquals(originalRating, playerOne.getRating(), "Rating should not change when there are no matches");
        assertEquals(originalRatingDeviation, playerOne.getRatingDeviation(), "Rating deviation should not change when there are no matches");
        assertEquals(originalVolatility, playerOne.getRatingVolatility(), "Rating volatility should not change when there are no matches");
    }

    @Test
    void shouldIncreaseRatingDeviationForInactivePlayers() {
        glicko2RatingPeriodDto.setOtherPlayers(List.of(playerOne));

        double originalRatingDeviation = playerOne.getRatingDeviation();

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        assertTrue(playerOne.getRatingDeviation() > originalRatingDeviation, "Rating deviation should increase for an inactive player");
    }

    @Test
    void shouldCorrectlyCalculateRatingAfterWinAndLoseAgainstEqualRatedPlayerInSamePeriod() {
        Glicko2Rating equalRatedOpponent = parameters.createNewRating();
        equalRatedOpponent.setId(5L);
        equalRatedOpponent.setRating(playerOne.getRating()); // Same rating as playerOne
        equalRatedOpponent.setRatingDeviation(playerOne.getRatingDeviation());
        equalRatedOpponent.setRatingVolatility(playerOne.getRatingVolatility());

        glicko2RatingPeriodDto.setResults(List.of(
                new Glicko2MatchResultDto(List.of(playerOne), List.of(equalRatedOpponent), GameEndResult.TEAM_A_WINNER),
                new Glicko2MatchResultDto(List.of(playerOne), List.of(equalRatedOpponent), GameEndResult.TEAM_B_WINNER)
        ));
        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

        assertEquals(playerOne.getRating(), equalRatedOpponent.getRating(), 0.0001, "Player should have higher rating after winning against equally rated player");
    }

    @Test
    void shouldCorrectlyUpdateVolatilityAfterMultipleImprobablySetsOfMatches() {
        playerOne.setRatingVolatility(0.05);
        playerOne.setRatingDeviation(50.0);

        // if the player is extremely erratic in their performance their volatility and deviation will increase
        for (int i = 0; i < 10; i++) {
            playerOne.setRating(1500.0);
            playerTwo.setRating(2000.0);
            playerThree.setRating(800.0);

            Glicko2MatchResultDto match1 = new Glicko2MatchResultDto(List.of(playerOne), List.of(playerTwo), GameEndResult.TEAM_A_WINNER);
            Glicko2MatchResultDto match2 = new Glicko2MatchResultDto(List.of(playerOne), List.of(playerThree), GameEndResult.TEAM_B_WINNER);

            glicko2RatingPeriodDto.setResults(List.of(match1, match1, match1, match1, match1));
            calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);

            glicko2RatingPeriodDto.setResults(List.of(match2, match2, match2));
            calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);
        }

        assertTrue(playerOne.getRatingVolatility() > 0.05, "Volatility should increase after a set of extremely unstable performance");
        assertTrue(playerOne.getRatingDeviation() > 50.0, "Deviation should increase after a set of extremely unstable performance");
    }

    @Test
    void shouldReturnCorrectExpectedScore() {
        double playerOneExpectedScore = calculator.getExpectedScore(playerOne, playerTwo);
        double playerTwoExpectedScore = calculator.getExpectedScore(playerTwo, playerOne);
        assertEquals(0.639, playerOneExpectedScore, 0.1, "Player one expected score should be correct");
        assertEquals(1 - 0.639, playerTwoExpectedScore, 0.1, "Player two expected score should be correct");
        assertEquals(1.0, playerOneExpectedScore + playerTwoExpectedScore, 0.1, "Expected score should add up to 1.0");
    }

    @Test
    void shouldReturnSameExpectedScoreForPlayersOfEqualRating() {
        playerOne.setRating(1500.0);
        playerTwo.setRating(1500.0);
        double playerOneExpectedScore = calculator.getExpectedScore(playerOne, playerTwo);
        double playerTwoExpectedScore = calculator.getExpectedScore(playerTwo, playerOne);
        assertEquals(0.5, playerOneExpectedScore, 0.1, "Player one expected score should be correct");
        assertEquals(0.5, playerTwoExpectedScore, 0.1, "Player two expected score should be correct");
        assertEquals(1.0, playerOneExpectedScore + playerTwoExpectedScore, 0.1, "Expected score should add up to 1.0");
    }

    @Test
    void shouldReturnZeroAsExpectedScoreForPlayersOfExtremeDisparityInRating() {
        playerOne.setRating(500.0);
        playerTwo.setRating(2500.0);
        double playerOneExpectedScore = calculator.getExpectedScore(playerOne, playerTwo);
        double playerTwoExpectedScore = calculator.getExpectedScore(playerTwo, playerOne);
        assertEquals(0.0, playerOneExpectedScore, 0.01, "Player one expected score should be correct");
        assertEquals(1.0, playerTwoExpectedScore, 0.01, "Player two expected score should be correct");
        assertEquals(1.0, playerOneExpectedScore + playerTwoExpectedScore, 0.01, "Expected score should add up to 1.0");
    }

    @Test
    void shouldReturnSymmetricalExpectedScoreForAnyRating() {
        playerOne.setRatingDeviation(200.0);
        playerTwo.setRatingDeviation(200.0);

        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double randomRatingOne = random.nextDouble(500, 2500);
            double randomRatingTwo = random.nextDouble(500, 2500);

            playerOne.setRating(randomRatingOne);
            playerTwo.setRating(randomRatingTwo);

            double playerOneExpectedScore1 = calculator.getExpectedScore(playerOne, playerTwo);
            double playerTwoExpectedScore1 = calculator.getExpectedScore(playerTwo, playerOne);

            playerOne.setRating(randomRatingTwo);
            playerTwo.setRating(randomRatingOne);

            double playerOneExpectedScore2 = calculator.getExpectedScore(playerOne, playerTwo);
            double playerTwoExpectedScore2 = calculator.getExpectedScore(playerTwo, playerOne);

            assertEquals(playerOneExpectedScore1, playerTwoExpectedScore2, "Expected score return is not exactly symmetrical.");
            assertEquals(playerOneExpectedScore2, playerTwoExpectedScore1, "Expected score return is not exactly symmetrical.");
        }
    }
}
