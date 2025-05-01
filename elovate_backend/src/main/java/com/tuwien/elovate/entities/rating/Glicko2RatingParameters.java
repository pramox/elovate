package com.tuwien.elovate.entities.rating;

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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.Period;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Glicko2RatingParameters extends RatingParameters<Glicko2Rating> {

    /**
     * This is a Glicko-2 system constant, that is used in calculation.
     * It must not be changed by the game developer.
     */
    public static final double GLICKO_2_SCALE = 173.7178;

    /**
     * The default convergence tolerance used in the Glicko-2 rating system.
     * This constant defines the precision level for the iterative rating calculation process.
     * A lower value means higher precision but may increase computation time.
     * It is used to determine when the iterative calculation has converged to a stable value.
     * It must not be changed by the game developer, as this choice of 0.000001 is sufficiently
     * precise and is a good baseline for our desired precision and computational resources.
     */
    public static final double GLICKO_2_CONVERGENCE_TOLERANCE = 0.000001;

    /**
     * The default rating period for the Glicko-2 rating system.
     * This Period value represents the standard duration of a rating period, in terms of time passed.
     * The rating period is used to update inactive players rating deviation. When a player is inactive
     * their deviation will increase as they very likely to either have gotten worse at the game (by not playing)
     * or have gotten better (by playing on another service, similar games, etc.)
     */
    public static final Period DEFAULT_RATING_PERIOD = Period.ofWeeks(1);

    /**
     * The default rating for a new player in the Glicko-2 rating system.
     * This is the initial rating assigned to a player who has not yet participated in any rated game.
     * The value of 1500 is considered a baseline or average skill level in this context and is the
     * base value defined by Glicko-2.
     */
    public static final double DEFAULT_RATING = 1500.0;

    /**
     * The default minimum rating a player can reach.
     * If a player dropped below this rating he is instead placed exactly on this rating.
     */
    public static final double DEFAULT_MIN_RATING = 100.0;

    /**
     * The default maximum rating a player can reach.
     * If a player exceeded this rating he is instead placed exactly on this rating.
     */
    public static final double DEFAULT_MAX_RATING = 3500.0;

    /**
     * The default rating deviation for a new player in the Glicko-2 system.
     * Rating deviation represents the uncertainty in a player's rating.
     * A higher deviation indicates more uncertainty, typically assigned to new players or those with few games.
     * The default value of 350.0 is the Glicko-2 default, indicating a high degree of initial uncertainty.
     */
    public static final double DEFAULT_RATING_DEVIATION = 350.0;

    /**
     * The default rating volatility in the Glicko-2 rating system.
     * Rating volatility measures the degree of expected fluctuation in a player's rating.
     * A higher volatility indicates that a player's performance is more erratic or less consistent.
     * The default value of 0.06 represents a moderate level of volatility for new or unestablished players.
     */
    public static final double DEFAULT_RATING_VOLATILITY = 0.06;

    /**
     * The default value of Tau (τ) in the Glicko-2 rating system.
     * Tau is a system constant that controls the rate at which a player's rating volatility changes.
     * It has a significant impact on the responsiveness of a player's rating to recent results.
     * A value of 0.5 is a balanced choice, providing a reasonable level of responsiveness under most conditions.
     */
    public static final double DEFAULT_TAU = 0.5;

    /**
     * The duration of a rating period in the Glicko-2 system, measured as a timestamp. This is used to update
     * the rating deviation of inactive players in accordance to the Glicko-2 system.
     */
    @Column(name = "rating_period")
    private String ratingPeriod;

    /**
     * The default initial rating for a new player in the Glicko-2 system.
     * Represents an assumed average skill level for players without established ratings.
     */
    @Column(name = "default_rating")
    private Double defaultRating;

    /**
     * The default initial rating deviation for a new player in the Glicko-2 system.
     * Represents the initial uncertainty in a player's rating, typically higher for new players.
     */
    @Column(name = "default_rating_deviation")
    private Double defaultRatingDeviation;

    /**
     * The default initial rating volatility for a new player in the Glicko-2 system.
     * Indicates the expected fluctuation in a player's performance, generally higher for new or inconsistent players.
     */
    @Column(name = "default_rating_volatility")
    private Double defaultRatingVolatility;

    /**
     * The system constant Tau (τ) in the Glicko-2 rating system.
     * Controls the rate of change in a player's rating volatility, affecting how quickly ratings respond to recent results.
     */
    @Column(name = "tau")
    private Double tau;

    /**
     * The lower cap for the rating of a player.
     * Represents the lowest possible numerical value the rating can reach.
     */
    @Column(name = "min_rating")
    private Double minRating;

    /**
     * The upper cap for the rating of a player.
     * Represents the highest possible numerical value the rating can reach.
     */
    @Column(name = "max_rating")
    private Double maxRating;

    /**
     * Stores the timestamp when the last period was ended and all inactive player's ratings were recalculated.
     */
    @Column(name = "last_rating_period_adjustment")
    private LocalDateTime lastRatingPeriodAdjustment;

    /**
     * Creates a new Glicko-2 rating instance with default parameters.
     * This method initializes a new rating for a player with default values for rating, rating deviation, and volatility.
     *
     * @return A new instance of Glicko2Rating with default values.
     */
    @Override
    public Glicko2Rating createNewRating() {
        return Glicko2Rating.builder()
                .rating(getDefaultRating())
                .workingRating(0.0)
                .ratingVolatility(getDefaultRatingVolatility())
                .workingRatingVolatility(0.0)
                .ratingDeviation(getDefaultRatingDeviation())
                .workingRatingVolatility(0.0)
                .build();
    }

    @Override
    public Class<?> getRatingClass() {
        return Glicko2Rating.class;
    }

    /**
     * Creates a new instance of Glicko2RatingParameters with default values.
     *
     * @return A new instance of Glicko2RatingParameters with default settings.
     */
    public static Glicko2RatingParameters createNewRatingParameters() {
        return Glicko2RatingParameters.builder()
                .ratingPeriod(DEFAULT_RATING_PERIOD.toString())
                .defaultRating(DEFAULT_RATING)
                .defaultRatingDeviation(DEFAULT_RATING_DEVIATION)
                .defaultRatingVolatility(DEFAULT_RATING_VOLATILITY)
                .minRating(DEFAULT_MIN_RATING)
                .maxRating(DEFAULT_MAX_RATING)
                .lastRatingPeriodAdjustment(LocalDateTime.now())
                .tau(DEFAULT_TAU)
                .build();
    }

    public void setRatingPeriod(Period period) {
        this.ratingPeriod = period.toString();
    }

    public Period getRatingPeriod() {
        return Period.parse(this.ratingPeriod);
    }

    public LocalDateTime getLastRatingPeriodAdjustment() {
        if (this.lastRatingPeriodAdjustment == null) {
            this.lastRatingPeriodAdjustment = LocalDateTime.now();
        }
        return this.lastRatingPeriodAdjustment;
    }
}
