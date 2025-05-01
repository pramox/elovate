package com.tuwien.elovate.repositories.lobby;

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
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LobbyRepository extends JpaRepository<Lobby, String> {
    @Query("SELECT l " +
            "FROM Lobby l " +
            "WHERE (:user MEMBER OF l.teamA OR :user MEMBER OF l.teamB) " +
            "AND l.timestamp > :currentTime")
    List<Lobby> findValidLobbyForUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);

    List<Lobby> findLobbiesByGame(Game game);
}
