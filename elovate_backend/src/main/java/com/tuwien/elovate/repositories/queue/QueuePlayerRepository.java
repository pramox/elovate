package com.tuwien.elovate.repositories.queue;

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

import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.queue.QueuePlayer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface QueuePlayerRepository extends JpaRepository<QueuePlayer, Long> {

    @Query("select qp from QueuePlayer qp where qp.user.email = :userName")
    Optional<QueuePlayer> findQueuePlayerByUserName(@Param("userName") String userName);

    Optional<QueuePlayer> findQueuePlayerBySessionId(String sessionId);

    @Query("select qp from QueuePlayer qp where qp.game = :game order by qp.id asc")
    List<QueuePlayer> findQueuePlayerByGameIdAndMatchSize(@Param("game") Game game, Pageable pageable);

    @Query("select count(qp) > 0 from QueuePlayer qp where qp.user.email = :userName")
    boolean hasQueuePlayerByUserName(@Param("userName") String username);

    @Query("select count(qp) from QueuePlayer qp where qp.game = :game")
    int getUserCountForGame(@Param("game") Game game);

    List<QueuePlayer> findByGame_IdAndRatingNotNull(Long id);

    List<QueuePlayer> findByGame_Id(Long id);
}
