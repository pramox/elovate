package com.tuwien.elovate.services.validator;

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

import com.tuwien.elovate.dtos.external.ExternalGlicko2RatingParametersDto;
import com.tuwien.elovate.dtos.rating.Glicko2RatingParametersDto;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import org.springframework.stereotype.Component;

@Component
public class RatingParametersValidator {

    public void validateGlicko2RatingParameters(Glicko2RatingParametersDto ratingParameters) {
        Double defaultRating = ratingParameters.getDefaultRating();
        Double defaultRatingDeviation = ratingParameters.getDefaultRatingDeviation();
        Double defaultRatingVolatility = ratingParameters.getDefaultRatingVolatility();
        Double tau = ratingParameters.getTau();
        Integer period = ratingParameters.getRatingPeriod();
        Double minRating = ratingParameters.getMinRating();
        Double maxRating = ratingParameters.getMaxRating();

        internalValidate(defaultRating, defaultRatingDeviation, defaultRatingVolatility, tau, minRating, maxRating);

        if (period == null || period < 7 || period > 30) {
            throw new ValidationException(Message.INVALID_RATING_PERIOD);
        }
    }

    public void validateExternalGlicko2RatingParameters(ExternalGlicko2RatingParametersDto ratingParameters) {
        Double defaultRating = ratingParameters.getDefaultRating();
        Double defaultRatingDeviation = ratingParameters.getDefaultRatingDeviation();
        Double defaultRatingVolatility = ratingParameters.getDefaultRatingVolatility();
        Double tau = ratingParameters.getTau();
        Double minRating = ratingParameters.getMin();
        Double maxRating = ratingParameters.getMax();

        internalValidate(defaultRating, defaultRatingDeviation, defaultRatingVolatility, tau, minRating, maxRating);
    }

    private void internalValidate(Double defaultRating, Double defaultRatingDeviation, Double defaultRatingVolatility, Double tau, Double minRating, Double maxRating) {
        if (defaultRating == null || Double.isNaN(defaultRating) || defaultRating < 1000 || defaultRating > 2000) {
            throw new ValidationException(Message.INVALID_DEFAULT_RATING);
        }

        if (defaultRatingDeviation == null || Double.isNaN(defaultRatingDeviation) || defaultRatingDeviation < 50.0 || defaultRatingDeviation > 500.0) {
            throw new ValidationException(Message.INVALID_DEFAULT_RATING_DEVIATION);
        }

        if (defaultRatingVolatility == null || Double.isNaN(defaultRatingVolatility) || defaultRatingVolatility < 0.02 || defaultRatingVolatility > 0.1) {
            throw new ValidationException(Message.INVALID_DEFAULT_RATING_VOLATILITY);
        }

        if (tau == null || Double.isNaN(tau) || tau < 0.2 || tau > 1.2) {
            throw new ValidationException(Message.INVALID_TAU);
        }

        if (minRating == null || Double.isNaN(minRating) || minRating < 0 || minRating > 1000) {
            throw new ValidationException(Message.INVALID_MIN_RATING);
        }

        if (maxRating == null || Double.isNaN(maxRating) || maxRating < 2000 || maxRating > 6000) {
            throw new ValidationException(Message.INVALID_MAX_RATING);
        }
    }
}
