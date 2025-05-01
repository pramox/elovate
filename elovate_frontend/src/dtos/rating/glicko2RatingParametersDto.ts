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
export interface Glicko2RatingParametersDto {
  defaultRating: number;
  defaultRatingDeviation: number;
  defaultRatingVolatility: number;
  tau: number;
  ratingPeriod: number; // in days
  minRating: number;
  maxRating: number;
}
