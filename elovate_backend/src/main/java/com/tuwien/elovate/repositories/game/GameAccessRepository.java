package com.tuwien.elovate.repositories.game;

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

import com.tuwien.elovate.dtos.statistics.LeaderboardPlayerDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.users.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GameAccessRepository extends JpaRepository<GameAccess, String> {

    @Transactional
    Optional<GameAccess> findByUser_IdAndGame_Id(Long id, Long id1);

    List<GameAccess> findByUser_IdInAndGame_Id(List<Long> ids, Long id1);

    Optional<GameAccess> findByUuid(String uuid);

    @Query("SELECT ga FROM GameAccess ga WHERE ga.game = :game")
    List<GameAccess> findAllByGame(@Param("game") Game game);

    @Query(value =
            "SELECT USERS.ID as userId, " +
                    "USERS.NICK_NAME as username, " +
                    "GAME.ID as gameId, " +
                    "USERS.COUNTRY_CODE as countryCode, " +
                    "rating.rating as rating, " +
                    "row_number() over (ORDER BY rating DESC) as leaderboardPosition " +
                    "FROM GAME_ACCESS" +
                    "    JOIN GAME ON GAME_ACCESS.GAME_ID = GAME.id " +
                    "    JOIN USERS ON GAME_ACCESS.user_id = users.id " +
                    "    JOIN RATING ON GAME_ACCESS.rating_id = rating.id " +
                    "WHERE game_id = :gameId " +
                    "AND (:countryCode IS NULL OR USERS.COUNTRY_CODE = :countryCode)" +
                    "ORDER BY rating DESC " +
                    "LIMIT :playerCount", nativeQuery = true)
    List<LeaderboardPlayerDto> getLeaderboard(@Param("gameId") Long gameId, @Param("playerCount") Long playerCount, @Param("countryCode") String countryCode);

    @Query(value =
            "SELECT * FROM" +
                    "(SELECT USERS.ID           as userId, " +
                    "        USERS.NICK_NAME    as username, " +
                    "        GAME.ID            as gameId, " +
                    "        USERS.COUNTRY_CODE as countryCode, " +
                    "        rating.rating      as rating, " +
                    "        row_number() over (ORDER BY rating DESC) as leaderboardPosition " +
                    "               FROM GAME_ACCESS " +
                    "                        JOIN GAME ON GAME_ACCESS.GAME_ID = GAME.id " +
                    "                        JOIN USERS ON GAME_ACCESS.user_id = users.id " +
                    "                        JOIN RATING ON GAME_ACCESS.rating_id = rating.id " +
                    "               WHERE game_id = :gameId " +
                    "                 AND (:countryCode IS null OR USERS.COUNTRY_CODE = :countryCode) " +
                    "               ORDER BY rating DESC) AS TEMP " +
                    "WHERE userId = :userId", nativeQuery = true)
    LeaderboardPlayerDto getUserInLeaderboard(@Param("gameId") Long gameId, @Param("userId") Long userId, @Param("countryCode") String countryCode);

    @Modifying
    @Query("DELETE from GameAccess ac where ac.user = :user and ac.game = :game")
    void deleteByUserAndGame(@Param("user") User user, @Param("game") Game game);

}
