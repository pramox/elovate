package com.tuwien.elovate.services.rating;

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

import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.rating.RatingParameters;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.services.rating.glicko2.Glicko2RatingService;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class RatingService {

    private final Glicko2RatingService glicko2RatingService;
    private final RatingRepository ratingRepository;
    private final RatingParametersRepository ratingParametersRepository;
    private final GameRepository gameRepository;

    /**
     * Updates the rating deviations of inactive players.
     */
    @Scheduled(fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void updateInactiveGlicko2Ratings() {
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            RatingParameters<? extends Rating> parameters = game.getRatingParameters();
            if (parameters == null || parameters.getRatingClass() != Glicko2Rating.class) {
                continue;
            }

            Glicko2RatingParameters glicko2RatingParameters = (Glicko2RatingParameters) parameters;
            LocalDateTime lastAdjustment = glicko2RatingParameters.getLastRatingPeriodAdjustment();
            LocalDateTime currentTime = LocalDateTime.now();
            int updatePeriodInDays = glicko2RatingParameters.getRatingPeriod().getDays();
            long daysSinceLastAdjustment = ChronoUnit.DAYS.between(lastAdjustment, currentTime);
            if (daysSinceLastAdjustment <= updatePeriodInDays) {
                continue;
            }

            LocalDateTime dateTime = LocalDateTime.now().minusDays(updatePeriodInDays);
            List<Glicko2Rating> inactiveRatings = ratingRepository.findInactiveGlicko2RatingsForGame(game.getId(), dateTime);
            glicko2RatingService.updateGlicko2RatingsForInactivePlayers(glicko2RatingParameters, inactiveRatings);
            glicko2RatingParameters.setLastRatingPeriodAdjustment(LocalDateTime.now());
            ratingParametersRepository.save(glicko2RatingParameters);
            ratingRepository.saveAll(inactiveRatings);
        }
    }

    /**
     * This method updates the rating of the winner and the loser based on the parameters.
     *
     * @param parameters  The parameters.
     * @param playerOne   The winner.
     * @param playerTwo   The loser.
     * @param matchResult How the match ended from the point of view of playerOne.
     */
    public void updateRating(RatingParameters<? extends Rating> parameters, Rating playerOne, Rating playerTwo, GameEndResult matchResult, boolean saveToDatabase) {
        updateRating(parameters, List.of(playerOne), List.of(playerTwo), matchResult, saveToDatabase);
    }

    /**
     * This method updates the rating of the winnerTeam and the loserTeam based on the parameters.
     *
     * @param parameters  The parameters.
     * @param teamOne     The first team of the match.
     * @param teamTwo     The second team of the match.
     * @param matchResult How the match ended from the point of view of teamOne.
     */
    // the unchecked cast warning can be ignored as the classes are checked in validateTypeSafety and therefore the cast is safe.
    @SuppressWarnings("unchecked")
    public void updateRating(RatingParameters<? extends Rating> parameters, List<? extends Rating> teamOne, List<? extends Rating> teamTwo, GameEndResult matchResult, boolean saveToDatabase) {
        validateTypeSafety(parameters, teamOne, teamTwo);

        // here other rating system may be added manually - currently only Glicko-2 is supported.
        if (parameters.getRatingClass() == Glicko2Rating.class) {
            glicko2RatingService.updateGlicko2Ratings((Glicko2RatingParameters) parameters, (List<Glicko2Rating>) teamOne, (List<Glicko2Rating>) teamTwo, matchResult);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }

        if (saveToDatabase) {
            ratingRepository.saveAll(teamOne);
            ratingRepository.saveAll(teamTwo);
        }
    }

    /**
     * Returns the expected win percentage of playerOne over playerTwo. A value of 1 means playerOne is expected to
     * win all games, a value of 0 means playerOne is expected to lose all games. A value of e.g. 0.2 means that playerOne
     * is expected to score 20% of points over a period of games.
     *
     * @param playerOne The first player.
     * @param playerTwo The second player.
     * @return A double between 0 and 1.
     */
    // the unchecked cast warning can be ignored as the classes are checked in validateTypeSafety and therefore the cast is safe.
    @SuppressWarnings("unchecked")
    public double getExpectedScoreForPlayerOne(RatingParameters<?> parameters, Rating playerOne, Rating playerTwo) {
        validateTypeSafety(parameters, playerOne, playerTwo);

        if (parameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getExpectedScore((Glicko2RatingParameters) parameters, (Glicko2Rating) playerOne, (Glicko2Rating) playerTwo);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * Returns the expected win percentage of teamOne over teamTwo. The value is an average of expected scores
     * for each player in teamOne against each player in teamTwo.
     *
     * @param teamOne A list representing the first team.
     * @param teamTwo A list representing the second team.
     * @return A double between 0 and 1 representing the average expected score.
     */
    // the unchecked cast warning can be ignored as the classes are checked in validateTypeSafety and therefore the cast is safe.
    @SuppressWarnings("unchecked")
    public double getExpectedScoreForTeamOne(RatingParameters<?> parameters, List<? extends Rating> teamOne, List<? extends Rating> teamTwo) {
        validateTypeSafety(parameters, teamOne, teamTwo);

        if (parameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getExpectedScore((Glicko2RatingParameters) parameters, (List<Glicko2Rating>) teamOne, (List<Glicko2Rating>) teamTwo);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * Returns a confidence interval for the rating.
     *
     * @param parameters The parameters.
     * @param rating     The rating.
     * @param percentage The allowed win percentage (e.g. if the percentage is 0.8 (which is 80%) this means that interval from 0.1 to 0.9 for the given rating is used.
     *                   This means that the user with this rating has between a 10% and 90% chance to win this game (totaling 80%).
     * @return The confidence interval.
     */
    public Pair<Double, Double> getConfidenceInterval(RatingParameters<?> parameters, Rating rating, double percentage) {
        validateTypeSafety(parameters, rating);

        if (parameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getConfidenceInterval((Glicko2RatingParameters) parameters, (Glicko2Rating) rating, percentage);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * Fetches the max rating a player can reach in the game.
     *
     * @param game The game.
     * @return The max rating the player can reach.
     */
    public double getMaxRatingForGame(Game game) {
        RatingParameters<? extends Rating> ratingParameters = game.getRatingParameters();
        if (ratingParameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getMaxRating((Glicko2RatingParameters) ratingParameters);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * Fetches the min rating a player can reach in the game.
     *
     * @param game The game.
     * @return The min rating the player can reach.
     */
    public double getMinRatingForGame(Game game) {
        RatingParameters<? extends Rating> ratingParameters = game.getRatingParameters();
        if (ratingParameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getMinRating((Glicko2RatingParameters) ratingParameters);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * Fetches the max allowed rating discrepancy between two players for a "fair" game.
     *
     * @param game       The game.
     * @param percentage The allowed interval width. e.g. 0.6 means that all win percentages between 0.2 (20%) and 0.8 (80%) for either team
     *                   are generally allowed, due to the interval 0.8 - 0.2 being the given percentage value 0.6.
     * @return The maximum discrepancy
     */
    public double getMaxRatingDiscrepancyForGame(Game game, double percentage) {
        RatingParameters<?> ratingParameters = game.getRatingParameters();
        if (ratingParameters.getRatingClass() == Glicko2Rating.class) {
            return glicko2RatingService.getMaxRatingDiscrepancy((Glicko2RatingParameters) ratingParameters, percentage);
        } else {
            throw new IllegalArgumentException("Rating type not supported");
        }
    }

    /**
     * This method throws an exception if the generic types have a mismatch.
     *
     * @param parameters The rating parameters of the game.
     * @param teamOne    The first team of the match.
     * @param teamTwo    The second team of the match.
     */
    private void validateTypeSafety(RatingParameters<? extends Rating> parameters, List<? extends Rating> teamOne, List<? extends Rating> teamTwo) {
        Class<?> ratingClass = parameters.getRatingClass(); // Assuming you have a method to get the class of Rating

        for (Rating rating : teamOne) {
            if (!ratingClass.isInstance(rating)) {
                throw new IllegalArgumentException("Winner team rating type mismatch: " + ratingClass.getName() + " vs " + rating.getClass());
            }
        }

        for (Rating rating : teamTwo) {
            if (!ratingClass.isInstance(rating)) {
                throw new IllegalArgumentException("Loser team rating type mismatch: " + ratingClass.getName() + " vs " + rating.getClass());
            }
        }
    }

    private void validateTypeSafety(RatingParameters<? extends Rating> parameters, Rating playerOne, Rating playerTwo) {
        validateTypeSafety(parameters, List.of(playerOne), List.of(playerTwo));
    }

    private void validateTypeSafety(RatingParameters<? extends Rating> parameters, Rating playerOne) {
        validateTypeSafety(parameters, List.of(playerOne), List.of());
    }
}
