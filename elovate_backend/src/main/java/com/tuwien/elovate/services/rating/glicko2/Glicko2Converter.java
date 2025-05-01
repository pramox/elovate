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

import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Glicko2Converter {

    private final Glicko2RatingParameters ratingParameters;

    /**
     * Returns the rating on the Glicko-2 scale. This is also referred to as µ (my).
     *
     * @param rating The rating on the Glicko scale.
     * @return The rating on the Glicko-2 scale.
     */
    public double scaleRatingToGlicko2(double rating) {
        return (rating - ratingParameters.getDefaultRating()) / Glicko2RatingParameters.GLICKO_2_SCALE;
    }

    /**
     * Returns the rating on the Glicko scale.
     *
     * @param rating The rating on the Glicko-2 scale.
     * @return The rating on the Glicko scale.
     */
    public double scaleRatingFromGlicko2(Double rating) {
        return rating * Glicko2RatingParameters.GLICKO_2_SCALE + ratingParameters.getDefaultRating();
    }

    /**
     * Returns the rating deviation on the Glicko-2 scale.
     *
     * @param ratingDeviation The rating deviation on the Glicko scale.
     * @return The rating deviation on the Glicko-2 scale.
     */
    public double scaleRatingDeviationToGlicko2(double ratingDeviation) {
        return ratingDeviation / Glicko2RatingParameters.GLICKO_2_SCALE;
    }

    /**
     * Returns the rating deviation on the Glicko scale.
     *
     * @param ratingDeviation The rating deviation on the Glicko-2 scale.
     * @return The rating deviation on the Glicko scale.
     */
    public double scaleRatingDeviationFromGlicko2(Double ratingDeviation) {
        return ratingDeviation * Glicko2RatingParameters.GLICKO_2_SCALE;
    }
}
