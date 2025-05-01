package com.tuwien.elovate.repositories.stats;

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

import com.tuwien.elovate.dtos.statistics.EloByDayDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlayerStatsPostGameRepository extends JpaRepository<PlayerStatsPostGame, Long> {

    @Query(value =
            "SELECT new com.tuwien.elovate.dtos.statistics.EloByDayDto(" +
                    "pspgA.userId, " +
                    "fg.finishTime, " +
                    "pspgA.ratingAfter) " +
            "FROM FinishedGame fg " +
            "JOIN fg.teamA pspgA " +
            "WHERE :user = pspgA.user AND :game = fg.game    " +
            "UNION " +
            "SELECT new com.tuwien.elovate.dtos.statistics.EloByDayDto(" +
                    "pspgB.userId, " +
                    "fg.finishTime, " +
                    "pspgB.ratingAfter) " +
            "FROM FinishedGame fg " +
            "JOIN fg.teamB pspgB " +
            "WHERE :user = pspgB.user AND :game = fg.game " +
            "ORDER BY fg.finishTime ASC"
    )
    List<EloByDayDto> getEloOverTimeForUser(@Param("user") User user, @Param("game") Game game);
}
