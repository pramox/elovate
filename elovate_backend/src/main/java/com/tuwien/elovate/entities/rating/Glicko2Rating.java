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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Represents a Glicko-2 rating.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, onlyExplicitlyIncluded = true)
public class Glicko2Rating extends Rating {

    /**
     * The volatility of the player's Glicko-2 rating.
     * Indicates the degree of expected fluctuation in the player's performance.
     */
    @Column(name = "rating_volatility")
    @ToString.Include
    private Double ratingVolatility;

    /**
     * The rating deviation for the Glicko-2 rating.
     * Represents the uncertainty in the player's rating.
     */
    @Column(name = "rating_deviation")
    @ToString.Include
    private Double ratingDeviation;

    /**
     * The Glicko-2 working rating of the player. This is needed in the
     * calculation and must not be persisted.
     */
    @Transient
    @Builder.Default
    private Double workingRating = 0.0;

    /**
     * The working rating deviation. This is needed in the
     * calculation and must not be persisted.
     */
    @Transient
    @Builder.Default
    private Double workingRatingDeviation = 0.0;

    /**
     * The working rating volatility. This is needed in the
     * calculation and must not be persisted.
     */
    @Transient
    @Builder.Default
    private Double workingRatingVolatility = 0.0;

}
