package com.tuwien.elovate.dtos.statistics;

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

public interface LeaderboardPlayerDto {
    Long getUserId();

    String getUsername();

    Long getGameId();

    double getRating();

    String getCountryCode();

    Long getLeaderboardPosition();
}
