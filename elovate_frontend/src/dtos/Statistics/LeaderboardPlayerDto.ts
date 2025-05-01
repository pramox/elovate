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
export interface LeaderboardPlayerDto {
  userId: number;
  username: string;
  gameId: number;
  rating: number;
  countryCode?: string;
  leaderboardPosition?: number;
}
