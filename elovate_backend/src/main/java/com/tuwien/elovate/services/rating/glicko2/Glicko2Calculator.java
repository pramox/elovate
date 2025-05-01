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
import com.tuwien.elovate.entities.rating.Rating;

import java.util.List;

public class Glicko2Calculator {

    private final Glicko2RatingParameters ratingParameters;
    private final Glicko2Converter converter;

    /**
     * Creates a calculator for the specified RatingParameters.
     *
     * @param ratingParameters The parameters.
     */
    public Glicko2Calculator(Glicko2RatingParameters ratingParameters) {
        this.ratingParameters = ratingParameters;
        this.converter = new Glicko2Converter(ratingParameters);
    }

    /**
     * Returns the expected score of teamOne compared to teamTwo.
     *
     * @param teamOne The first team.
     * @param teamTwo The second team.
     * @return The expected score.
     */
    public double getExpectedScore(List<Glicko2Rating> teamOne, List<Glicko2Rating> teamTwo) {
        double averageRatingTeamOne = teamOne.stream().map(Rating::getRating).mapToDouble(v -> v).sum() / teamOne.size();
        double my = converter.scaleRatingToGlicko2(averageRatingTeamOne);

        double averageRatingTeamTwo = teamTwo.stream().map(Rating::getRating).mapToDouble(v -> v).sum() / teamTwo.size();
        double myJ = converter.scaleRatingToGlicko2(averageRatingTeamTwo);

        double averageRatingDeviationTeamTwo = teamTwo.stream().map(Glicko2Rating::getRatingDeviation).mapToDouble(v -> v).sum() / teamTwo.size();
        double phiJ = converter.scaleRatingDeviationToGlicko2(averageRatingDeviationTeamTwo);

        return calculateExpectedScore(my, myJ, phiJ);
    }

    /**
     * Returns the expected score of playerOne compared to playerTwo.
     *
     * @param playerOne The first player.
     * @param playerTwo The second player.
     * @return The expected score.
     */
    public double getExpectedScore(Glicko2Rating playerOne, Glicko2Rating playerTwo) {
        return getExpectedScore(List.of(playerOne), List.of(playerTwo));
    }

    /**
     * Updates the ratings for all the player in the {@link Glicko2RatingPeriodDto}. Players that are part of
     * {@link Glicko2RatingPeriodDto#getPlayersWithoutMatch()} only get their rating deviation updated.
     *
     * @param glicko2RatingPeriodDto The period.
     */
    public void calculateAndApplyRatingChange(Glicko2RatingPeriodDto glicko2RatingPeriodDto) {

        // for players with a match the ratings must be updated
        List<Glicko2Rating> playersWithMatch = glicko2RatingPeriodDto.getPlayersWithMatch();
        for (Glicko2Rating player : playersWithMatch) {
            List<Glicko2MatchResultDto> resultsForPlayer = glicko2RatingPeriodDto.getResultsForPlayer(player);
            calculateAndApplyRatingChange(player, resultsForPlayer);
        }

        // for players without a match in the period only update the rating deviation
        List<Glicko2Rating> playerWithoutMatch = glicko2RatingPeriodDto.getPlayersWithoutMatch();
        for (Glicko2Rating player : playerWithoutMatch) {
            increaseRatingDeviationForInactivePlayer(player);
        }

        // finalise the rating for all players after the match
        List<Glicko2Rating> allPlayers = glicko2RatingPeriodDto.getAllPlayers();
        for (Glicko2Rating player : allPlayers) {
            endRatingPeriod(player);
        }
    }

    /**
     * Calculates and applies the rating change for the given matchResults.
     *
     * @param player       The player for whom to update the rating.
     * @param matchResults The results.
     */
    private void calculateAndApplyRatingChange(Glicko2Rating player, List<Glicko2MatchResultDto> matchResults) {
        double my = converter.scaleRatingToGlicko2(player.getRating()); // this is the players original rating converted to the Glicko-2 scale

        double phi = converter.scaleRatingDeviationToGlicko2(player.getRatingDeviation());
        double sigma = player.getRatingVolatility(); // the volatility does not get scaled
        double quantity = calculateQuantity(player, matchResults);
        double delta = calculateDelta(player, matchResults, quantity);

        double sigmaDash = calculateSigmaDash(sigma, delta, phi, quantity);
        player.setWorkingRatingVolatility(sigmaDash);

        double phiStar = calculatePhiStar(phi, sigma);
        double phiDash = calculatePhiDash(phiStar, quantity);
        double phiDashScaled = converter.scaleRatingDeviationFromGlicko2(phiDash);
        player.setWorkingRatingDeviation(phiDashScaled);

        double myDash = my + (Math.pow(phiDash, 2) * calculatePerformanceRating(player, matchResults));
        double myDashScaled = converter.scaleRatingFromGlicko2(myDash);
        player.setWorkingRating(myDashScaled);
    }

    /**
     * Updates the rating deviation for a player who has not played in the rating period, but is part of the
     * {@link Glicko2RatingPeriodDto}. If a player has played this step is not needed and done as part of
     * {@link Glicko2Calculator#calculateAndApplyRatingChange(Glicko2Rating, List)}.
     *
     * @param player The updated rating.
     */
    private void increaseRatingDeviationForInactivePlayer(Glicko2Rating player) {
        player.setWorkingRating(player.getRating());
        player.setWorkingRatingVolatility(player.getRatingVolatility());
        double phi = converter.scaleRatingDeviationToGlicko2(player.getRatingDeviation());
        double sigma = player.getRatingVolatility();
        double phiStar = calculatePhiStar(phi, sigma);
        double phiStarConverted = converter.scaleRatingDeviationFromGlicko2(phiStar);
        player.setWorkingRatingDeviation(phiStarConverted);
    }

    /**
     * Ends the rating period for the player. Here the working ratings are persisted to the {@link Glicko2Rating}
     * objects.
     *
     * @param player The player to end the period for.
     */
    private void endRatingPeriod(Glicko2Rating player) {

        double newRating = player.getWorkingRating();
        if (this.ratingParameters.getMinRating() > newRating) {
            newRating = this.ratingParameters.getMinRating();
        } else if (this.ratingParameters.getMaxRating() < newRating) {
            newRating = this.ratingParameters.getMaxRating();
        }

        player.setRating(newRating);
        player.setRatingDeviation(player.getWorkingRatingDeviation());
        player.setRatingVolatility(player.getWorkingRatingVolatility());

        player.setWorkingRating(0.0);
        player.setWorkingRatingVolatility(0.0);
        player.setWorkingRatingDeviation(0.0);
    }

    /**
     * Calculates the quantity v. This is the estimated variance of the team’s/player’s
     * rating based only on game outcomes.
     *
     * @param player       The player.
     * @param matchResults The results.
     * @return The quantity v.
     */
    private double calculateQuantity(Glicko2Rating player, List<Glicko2MatchResultDto> matchResults) {
        double sum = 0.0;

        for (Glicko2MatchResultDto result : matchResults) {
            double opponentRatingDeviation = result.getOpponentRatingDeviation(player);
            double phiJ = converter.scaleRatingDeviationToGlicko2(opponentRatingDeviation);

            double eValue = getExpectedScore(result.getTeamOfPlayer(player), result.getOpponentTeamOfPlayer(player));

            double value = Math.pow(calculateG(phiJ), 2);
            value *= eValue;
            double endValue = 1 - eValue;
            value *= endValue;

            sum += value;
        }

        sum = Math.pow(sum, -1);

        return sum;
    }

    /**
     * Delta is the estimated improvement in rating by comparing the
     * pre-period rating to the performance rating based only on game outcomes.
     *
     * @param player       The player for which to calculate the delta.
     * @param matchResults The results for the player.
     * @param quantity     The quantity.
     * @return The delta.
     */
    private double calculateDelta(Glicko2Rating player, List<Glicko2MatchResultDto> matchResults, double quantity) {
        double performanceRating = calculatePerformanceRating(player, matchResults);
        return quantity * performanceRating;
    }

    /**
     * Calculates the new value of sigma (σ), the volatility.
     *
     * @return The new σ.
     */
    private double calculateSigmaDash(double sigma, double delta, double phi, double quantity) {
        double tau = ratingParameters.getTau();
        double epsilon = Glicko2RatingParameters.GLICKO_2_CONVERGENCE_TOLERANCE;

        double a = Math.log(Math.pow(sigma, 2));
        double lowerBound = a; // The "A" in Glickman's paper

        double upperBound = determineInitialUpperBound(a, delta, phi, quantity, tau); // The "B" in Glickman's paper

        double fA = calculateF(lowerBound, delta, phi, quantity, a);
        double fB = calculateF(upperBound, delta, phi, quantity, a);

        // This is the Illinois algorithm, a variant of the regula falsi ("false position") procedure
        // It is a way to solve an equation with one unknown.
        while (Math.abs(upperBound - lowerBound) > epsilon) {
            double estimate = lowerBound + (((lowerBound - upperBound) * fA) / (fB - fA)); // The "C" in Glickman's paper
            double fC = calculateF(estimate, delta, phi, quantity, a);

            if (fC * fB <= 0) {
                lowerBound = upperBound;
                fA = fB;
            } else {
                fA /= 2.0;
            }

            upperBound = estimate;
            fB = fC;
        }

        return Math.exp(lowerBound / 2.0);
    }

    /**
     * Calculates the initial upper bound for the Illinois algorithm used in step 5 of Glickman's paper.
     *
     * @param a        The value of a
     * @param delta    The delta (∆)
     * @param phi      The phi (φ)
     * @param quantity The quantity v
     * @param tau      The tau (τ)
     * @return The initial upper bound (B).
     */
    private double determineInitialUpperBound(double a, double delta, double phi, double quantity, double tau) {
        double deltaSquared = Math.pow(delta, 2);
        double phiSquared = Math.pow(phi, 2);

        if (deltaSquared > (phiSquared + quantity)) {
            return Math.log(deltaSquared - phiSquared - quantity);
        } else {
            double incrementFactor = 1;
            double upperBound = a - (incrementFactor * Math.abs(tau));

            while (calculateF(upperBound, delta, phi, quantity, a) < 0) {
                incrementFactor++;
                upperBound = a - (incrementFactor * Math.abs(tau));
            }

            return upperBound;
        }
    }

    /*
     * HELPER FUNCTIONS
     */

    /**
     * Calculates g(φ)
     *
     * @param phi The phi value.
     * @return g(φ).
     */
    private double calculateG(double phi) {
        double numerator = 3.0 * Math.pow(phi, 2);
        double denominator = Math.pow(Math.PI, 2);
        double sqrtTerm = Math.sqrt(1.0 + numerator / denominator);

        return 1.0 / sqrtTerm;
    }

    /**
     * Calculates f(x)
     *
     * @param x        The unknown value.
     * @param delta    The delta (∆)
     * @param phi      The phi (φ)
     * @param quantity The quantity v
     * @param a        The value of a
     * @return f(x)
     */
    private double calculateF(double x, double delta, double phi, double quantity, double a) {
        double expX = Math.exp(x);
        double phiSquared = Math.pow(phi, 2);
        double deltaSquared = Math.pow(delta, 2);
        double denominatorTerm = phiSquared + quantity + expX;
        double tau = ratingParameters.getTau();

        double firstTermNumerator = expX * (deltaSquared - phiSquared - quantity - expX);
        double firstTermDenominator = 2.0 * Math.pow(denominatorTerm, 2);
        double firstTerm = firstTermNumerator / firstTermDenominator;

        double secondTerm = (x - a) / Math.pow(tau, 2);

        return firstTerm - secondTerm;
    }

    /**
     * Calculates the performance rating of a player for the given results.
     *
     * @param player       The player.
     * @param matchResults The results.
     * @return The performance rating.
     */
    private double calculatePerformanceRating(Glicko2Rating player, List<Glicko2MatchResultDto> matchResults) {
        double outcomeBasedRating = 0;

        for (Glicko2MatchResultDto result : matchResults) {
            double opponentRatingDeviation = result.getOpponentRatingDeviation(player);
            double phiJ = converter.scaleRatingDeviationToGlicko2(opponentRatingDeviation);

            double expectedScore = getExpectedScore(result.getTeamOfPlayer(player), result.getOpponentTeamOfPlayer(player));
            double scoreDifference = result.getScore(player) - expectedScore;
            double ratingUpdate = calculateG(phiJ) * scoreDifference;

            outcomeBasedRating += ratingUpdate;
        }

        return outcomeBasedRating;
    }

    /**
     * Calculates the expected score (0.0 = 0%, 1.0 = 100%) for the player.
     *
     * @param my   The Glicko-2 rating of the player (µ)
     * @param myJ  The Glicko-2 rating of the opponent (µj)
     * @param phiJ The Glicko-2 rating deviation of the opponent (φj)
     * @return The expected score between 0.0 and 1.0.
     */
    private double calculateExpectedScore(double my, double myJ, double phiJ) {
        double exponent = -calculateG(phiJ) * (my - myJ);
        return 1.0 / (1.0 + Math.exp(exponent));
    }

    /**
     * Calculates φ*
     *
     * @param phi   The phi (φ)
     * @param sigma The sigma (σ)
     * @return φ*
     */
    private double calculatePhiStar(double phi, double sigma) {
        double phiSquared = Math.pow(phi, 2);
        double sigmaSquared = Math.pow(sigma, 2);
        return Math.sqrt(phiSquared + sigmaSquared);
    }

    /**
     * Calculates φ'
     *
     * @param phiStar  The phi-star (φ*)
     * @param quantity The quantity v
     * @return φ'
     */
    private double calculatePhiDash(double phiStar, double quantity) {
        double phiStarSquaredInverse = 1.0 / Math.pow(phiStar, 2);
        double quantityInverse = 1.0 / quantity;
        double denominator = phiStarSquaredInverse + quantityInverse;

        return 1.0 / Math.sqrt(denominator);
    }
}
