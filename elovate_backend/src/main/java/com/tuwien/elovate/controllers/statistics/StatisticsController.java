package com.tuwien.elovate.controllers.statistics;

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
import com.tuwien.elovate.dtos.statistics.LeaderboardPlayerDto;
import com.tuwien.elovate.dtos.statistics.RankDistributionDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.statistics.StatisticsService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics/")
@AllArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final SessionUtils sessionUtils;
    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/game/{gameId}/eloOverTime")
    public ResponseEntity<List<EloByDayDto>> getEloOverTimeForUser(@PathVariable("gameId") Long gameId) {
        User user = sessionUtils.getActiveUser();
        log.info("Request to: /api/v1/statistics/game/{}/eloOverTime", gameId);
        return new ResponseEntity<>(statisticsService.getEloOverTimeForUser(gameId, user), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{gameId}/leaderboard")
    public ResponseEntity<List<LeaderboardPlayerDto>> getLeaderboard(@PathVariable("gameId") Long gameId,
                                                                     @RequestParam(required = false) Long count,
                                                                     @RequestParam(required = false) String countryCode) {
        log.info("Requested leaderboard for game {}. Count: {}, Country: {}", gameId, count, countryCode);
        return new ResponseEntity<>(statisticsService.getLeaderboard(gameId, countryCode, count), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{gameId}/leaderboard/{userId}")
    public ResponseEntity<LeaderboardPlayerDto> getUserInLeaderboard(@PathVariable("gameId") Long gameId,
                                                                     @PathVariable("userId") Long userId,
                                                                     @RequestParam("withCountry") boolean withCountry) {
        log.info("Requested leaderboard for game {} and user {}", gameId, userId);
        return new ResponseEntity<>(statisticsService.getUserInLeaderboard(gameId, userId, withCountry), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/lobby/{lobbyId}")
    public ResponseEntity<List<LeaderboardPlayerDto>> getUserRatingInLobby(@PathVariable("lobbyId") String lobbyId) {
        log.info("Requested leaderboard for Lobby {}", lobbyId);
        return new ResponseEntity<>(statisticsService.getUserInLobby(lobbyId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{gameId}/distribution")
    public ResponseEntity<RankDistributionDto> getRankDistribution(@PathVariable("gameId") Long gameId) {
        log.info("Requested rank distribution for game {}", gameId);
        return new ResponseEntity<>(statisticsService.getRankDistribution(gameId), HttpStatus.OK);
    }
}
