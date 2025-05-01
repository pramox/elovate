package com.tuwien.elovate.dtos.rating;

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

import lombok.Data;

@Data
public class Glicko2RatingParametersDto {

    Long id;
    Double defaultRating;
    Double defaultRatingDeviation;
    Double defaultRatingVolatility;
    Double tau;
    Integer ratingPeriod; // this is in days - the mapper maps this to the valid Period Object
    Double minRating;
    Double maxRating;

}
