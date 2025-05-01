package com.tuwien.elovate.repositories.gameapplication;

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
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface GameApplicationRepository extends JpaRepository<GameApplication, Long>, PagingAndSortingRepository<GameApplication, Long> {

    @Query("select ga from GameApplication ga where ga.status = com.tuwien.elovate.enums.GameApplicationStatus.PENDING order by ga.name limit 1")
    Optional<GameApplication> findFirstPendingApplication();

    @Query("select ga from GameApplication ga where ga.status = com.tuwien.elovate.enums.GameApplicationStatus.REJECTED order by ga.name limit 1")
    Optional<GameApplication> findFirstRejectedApplication();

    GameApplication getGameApplicationByName(String name);

    Page<GameApplication> findAllByDeveloperAndStatus(User developer, GameApplicationStatus status, Pageable pageable);

    Page<GameApplication> findAllByStatus(GameApplicationStatus status, Pageable pageable);

    List<GameApplication> findAllByStatus(GameApplicationStatus status);

    @Query("select ga from Game ga, GameApplication app where app.game = ga and app.id = :id")
    Optional<Game> findGameByGameApplicationId(@Param("id") Long id);
}
