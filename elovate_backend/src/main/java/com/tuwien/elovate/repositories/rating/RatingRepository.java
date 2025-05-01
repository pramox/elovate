package com.tuwien.elovate.repositories.rating;

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

import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Query("SELECT distinct(gam.rating) FROM GameAccess gam WHERE NOT EXISTS( " +
            "select ga from GameAccess ga, Game g, FinishedGame fg, PlayerStatsPostGame pspg " +
            "WHERE g.id = :gameId and gam.uuid = ga.uuid and ga.game = g and fg.game = g and pspg.userId = ga.user.id and " +
            "(pspg.id in (select pspg2.id from fg.teamA pspg2) OR pspg.id in (select pspg2.id from fg.teamB pspg2)) AND " +
            "fg.finishTime > :date" +
            ") AND gam.game.id = :gameId"
    )
    List<Glicko2Rating> findInactiveGlicko2RatingsForGame(@Param("gameId") Long gameId, @Param("date") LocalDateTime date);
}
