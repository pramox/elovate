package com.tuwien.elovate.repositories.users;

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
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findFirstByRolesContains(Role role);

    Optional<User> findByNickName(String nickName);

    @Query("select game from User u join u.connectedGames game where u.id = :id order by game.id")
    Page<Game> findAllConnectedGames(@Param("id") Long id, Pageable pageable);

    @Query("select count(u) > 0 from User u where :role member of u.roles and u.id = :userId")
    boolean hasRoleById(@Param("userId") Long userId, @Param("role") Role role);

    @Query("select u from User u where u.id != :userId and LOWER(u.nickName) like LOWER(concat('%', :nickName, '%')) and u not in (select f from User u2 join u2.friends f where u2.id = :userId) and u not in (select b from User u3 join u3.blocked b where u3.id = :userId) and :userId not in (select b2.id from User u4 join u4.blocked b2 where u4.id = u.id) order by u.nickName asc")
    Page<User> findAllByNicknameExcludingFriendsAndBlocked(@Param("nickName") String nickName, @Param("userId") Long userId, Pageable pageable);


    @Query("select b from User u join u.blocked b where u.id = :userId and LOWER(b.nickName) like LOWER(concat('%', :nickName, '%')) order by b.nickName asc")
    Page<User> findBlockedUsersByNickname(@Param("userId") Long userId, @Param("nickName") String nickName, Pageable pageable);

    @Query("select b from User u join u.friends b where u.id = :userId and LOWER(b.nickName) like LOWER(concat('%', :nickName, '%')) order by b.nickName asc")
    Page<User> findFriendsByNickname(@Param("userId") Long userId, @Param("nickName") String nickName, Pageable pageable);

    @Query("select b from User u join u.blocked b where u.id = :userId order by b.nickName asc")
    Page<User> findBlocked(@Param("userId") Long userId, Pageable pageable);

    @Query("select b from User u join u.friendRequests b where u.id = :userId order by b.nickName asc")
    Page<User> findFriendRequests(@Param("userId") Long userId, Pageable pageable);

    @Query("select b from User u join u.friends b where u.id = :userId order by b.nickName asc")
    Page<User> findFriends(@Param("userId") Long userId, Pageable pageable);

    List<User> findBy(String nickName, Pageable pageable);

    List<User> findAllByRolesContains(Role role);
}
