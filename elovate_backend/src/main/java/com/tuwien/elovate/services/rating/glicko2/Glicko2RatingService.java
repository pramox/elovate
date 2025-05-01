package com.tuwien.elovate.services.rating.glicko2;

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
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.enums.GameEndResult;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class Glicko2RatingService {

    /**
     * This method updates the glicko-2 rating of every player in both teams based on the parameters.
     *
     * @param parameters  The parameters.
     * @param teamOne     The first team of the match.
     * @param teamTwo     The second team of the match.
     * @param matchResult How the match ended from the point of view of teamOne.
     */
    public void updateGlicko2Ratings(Glicko2RatingParameters parameters, List<Glicko2Rating> teamOne, List<Glicko2Rating> teamTwo, GameEndResult matchResult) {
        Glicko2Calculator calculator = new Glicko2Calculator(parameters);

        Glicko2RatingPeriodDto glicko2RatingPeriodDto = new Glicko2RatingPeriodDto();
        Glicko2MatchResultDto glicko2MatchResultDto = new Glicko2MatchResultDto();
        glicko2MatchResultDto.setTeamOne(teamOne);
        glicko2MatchResultDto.setTeamTwo(teamTwo);
        glicko2MatchResultDto.setResult(matchResult);

        glicko2RatingPeriodDto.setResults(
                List.of(
                        glicko2MatchResultDto
                )
        );

        calculator.calculateAndApplyRatingChange(glicko2RatingPeriodDto);
    }

    public void updateGlicko2RatingsForInactivePlayers(Glicko2RatingParameters ratingParameters, List<Glicko2Rating> inactiveRatings) {
        Glicko2Calculator calculator = new Glicko2Calculator(ratingParameters);
        Glicko2RatingPeriodDto periodDto = new Glicko2RatingPeriodDto();
        periodDto.setOtherPlayers(inactiveRatings);
        calculator.calculateAndApplyRatingChange(periodDto);
    }

    public double getExpectedScore(Glicko2RatingParameters ratingParameters, Glicko2Rating playerOne, Glicko2Rating playerTwo) {
        Glicko2Calculator calculator = new Glicko2Calculator(ratingParameters);
        return calculator.getExpectedScore(playerOne, playerTwo);
    }

    public double getExpectedScore(Glicko2RatingParameters ratingParameters, List<Glicko2Rating> teamOne, List<Glicko2Rating> teamTwo) {
        Glicko2Calculator calculator = new Glicko2Calculator(ratingParameters);
        return calculator.getExpectedScore(teamOne, teamTwo);
    }

    public double getMaxRating(Glicko2RatingParameters parameters) {
        return parameters.getMaxRating();
    }

    public double getMinRating(Glicko2RatingParameters parameters) {
        return parameters.getMinRating();
    }

    public double getMaxRatingDiscrepancy(Glicko2RatingParameters parameters, double percentage) {
        Pair<Double, Double> confidenceInterval = getConfidenceInterval(parameters, parameters.createNewRating(), percentage);
        return confidenceInterval.getSecond() - parameters.getDefaultRating();
    }

    public Pair<Double, Double> getConfidenceInterval(Glicko2RatingParameters ratingParameters, Glicko2Rating player, double percentage) {
        if (percentage < 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException();
        }

        double halfPercentage = percentage / 2.0;
        double bound = 0.5 - halfPercentage;
        double precision = 0.005;

        double stepSize = ratingParameters.getDefaultRatingDeviation() / 50.0;
        double lowerRating = player.getRating();
        boolean found = false;

        // Adjust the ratings until the expected scores match the bounds
        while (!found) {
            Glicko2Rating testRating = new Glicko2Rating(ratingParameters.getDefaultRatingVolatility(), ratingParameters.getDefaultRatingDeviation(), 0.0, 0.0, 0.0);
            testRating.setRating(lowerRating);
            double expectedScore = getExpectedScore(ratingParameters, testRating, player);

            if (Math.abs(expectedScore - bound) <= precision) {
                found = true;
            } else {
                lowerRating -= stepSize;
            }
        }

        double disparity = player.getRating() - lowerRating;
        double upperRating = player.getRating() + disparity;

        return Pair.of(lowerRating, upperRating);
    }
}
