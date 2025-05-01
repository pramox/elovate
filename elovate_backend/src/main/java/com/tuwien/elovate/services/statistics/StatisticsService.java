package com.tuwien.elovate.services.statistics;

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
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.lobby.Lobby;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.lobby.LobbyRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.Cache;
import com.tuwien.elovate.services.session.SessionUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class StatisticsService {
    private final UserRepository userRepository;
    private final PlayerStatsPostGameRepository playerStatsPostGameRepository;
    private final GameAccessRepository gameAccessRepository;
    private final GameRepository gameRepository;

    private final LobbyRepository lobbyRepository;
    private final Cache cache;
    private final SessionUtils sessionUtils;

    private static final Long LEADERBOARD_LIMIT = 100L;
    private static final Long GRANULARITY = 30L;

    public List<EloByDayDto> getEloOverTimeForUser(Long gameId, User user) {
        Set<Game> userGames = userRepository.findAllConnectedGames(user.getId(), PageRequest.of(0, 100)).stream().collect(Collectors.toSet());
        Optional<Game> game = gameRepository.findById(gameId);
        if (!userGames.isEmpty() && game.isPresent() && userGames.contains(game.get())) {
            List<EloByDayDto> existingData = playerStatsPostGameRepository.getEloOverTimeForUser(user, game.get());
            return interpolateData(existingData);
        }
        throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
    }

    public List<LeaderboardPlayerDto> getLeaderboard(Long gameId, String countryCode, Long count) {

        List<LeaderboardPlayerDto> topPlayers;

        if (count == null || count > LEADERBOARD_LIMIT) {
            count = LEADERBOARD_LIMIT;
        }
        if (count < 0) {
            throw new ValidationException(Message.INVALID_LEADERBOARD_COUNT);
        }
        if (countryCode != null) {
            return gameAccessRepository.getLeaderboard(gameId, count, countryCode);
        } else {

            topPlayers = cache.getTopPlayers(count, gameId);

            if (topPlayers.isEmpty()) {
                topPlayers = gameAccessRepository.getLeaderboard(gameId, count, countryCode);
                cache.setTopPlayers(gameId, topPlayers);
            }

            assert (topPlayers != null);
            return topPlayers;
        }
    }

    public LeaderboardPlayerDto getUserInLeaderboard(Long gameId, Long userId, boolean withCountry) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }
        if (!Objects.equals(sessionUtils.getActiveUser().getId(), userId)) {
            throw new BadRequestException(Message.STATISTICS_INVALID_USER_LOGGED_IN);
        }
        return gameAccessRepository.getUserInLeaderboard(gameId, userOpt.get().getId(), withCountry ? userOpt.get().getCountryCode() : null);
    }

    public List<LeaderboardPlayerDto> getUserInLobby(String lobbyId) {

        Optional<Lobby> lobby = lobbyRepository.findById(lobbyId);
        if (lobby.isEmpty()) {
            throw new EntityNotFoundException(Message.LOBBY_NOT_FOUND);
        }
        Lobby lobbyObj = lobby.get();
        User active = sessionUtils.getActiveUser();
        if (!lobbyObj.getTeamA().contains(active) && !lobbyObj.getTeamB().contains(active)) {
            throw new BadRequestException(Message.STATISTICS_INVALID_USER_LOGGED_IN);
        }
        Long gameId = lobbyObj.getGame().getId();
        List<LeaderboardPlayerDto> leaderboardPlayerDtos = new ArrayList<>();
        for (User user : lobbyObj.getTeamA()) {
            leaderboardPlayerDtos.add(gameAccessRepository.getUserInLeaderboard(gameId, user.getId(), null));
        }
        for (User user : lobbyObj.getTeamB()) {
            leaderboardPlayerDtos.add(gameAccessRepository.getUserInLeaderboard(gameId, user.getId(), null));
        }
        return leaderboardPlayerDtos;
    }

    public RankDistributionDto getRankDistribution(Long gameId) {
        RankDistributionDto rankDistribution = cache.getRankDistribution(gameId);
        if (rankDistribution == null) {
            List<LeaderboardPlayerDto> players = gameAccessRepository.getLeaderboard(gameId, null, null);
            rankDistribution = calculateRankDistribution(players, GRANULARITY);
            cache.setRankDistribution(gameId, rankDistribution);
        }
        return rankDistribution;

    }

    private RankDistributionDto calculateRankDistribution(List<LeaderboardPlayerDto> players, Long granularity) {
        RankDistributionDto output = new RankDistributionDto();
        List<Pair<Integer, Double>> rankDistribution = new ArrayList<>();

        double lowerBound = players.get(players.size() - 1).getRating();
        double upperBound = players.get(0).getRating();

        double spanningWidth = upperBound - lowerBound;
        int stepSize = (int) ((spanningWidth / granularity) / 10) * 10;

        int currentBound = (int) Math.round(((int) lowerBound) / 100.0) * 100 + stepSize;
        int playerCount = 0;
        for (int i = players.size() - 1; i >= 0; i--) {
            playerCount++;
            if (players.get(i).getRating() >= currentBound) {
                rankDistribution.add(Pair.of(currentBound, ((double) playerCount / players.size()) * 100));
                currentBound += stepSize;
                playerCount = 0;
            }
        }
        rankDistribution.add(Pair.of(currentBound, (double) playerCount / players.size()));
        output.setRankDistribution(rankDistribution);
        output.setTotalPlayers(players.size());
        return output;
    }

    private static List<EloByDayDto> interpolateData(List<EloByDayDto> existingData) {
        List<EloByDayDto> interpolatedData = new ArrayList<>();

        if (existingData.isEmpty()) {
            return interpolatedData; // No existing data to interpolate
        }

        List<EloByDayDto> sorted = existingData.stream().sorted(Comparator.comparingLong(e -> e.getFinishTime().toEpochSecond(ZoneOffset.UTC))).toList();

        sorted = sorted.stream().map(e -> {
            LocalDateTime normalizedDateTime = e.getFinishTime();
            normalizedDateTime = normalize(normalizedDateTime);
            return new EloByDayDto(e.getUserId(), normalizedDateTime, e.getRatingAfter());
        }).collect(Collectors.toList());

        for (int i = 0; i < sorted.size() - 1; i++) {
            EloByDayDto current = sorted.get(i);
            EloByDayDto next = sorted.get(i + 1);

            while (!next.getFinishTime().isEqual(current.getFinishTime())) {
                interpolatedData.add(new EloByDayDto(current.getUserId(), current.getFinishTime(), current.getRatingAfter()));
                current.setFinishTime(current.getFinishTime().plusDays(1));
            }
        }

        EloByDayDto lastDay = sorted.get(sorted.size() - 1);
        LocalDateTime lastDateTime = normalize(lastDay.getFinishTime());
        LocalDateTime today = normalize(LocalDateTime.now());

        while (!lastDateTime.isAfter(today)) {
            interpolatedData.add(new EloByDayDto(lastDay.getUserId(), lastDateTime, lastDay.getRatingAfter()));
            lastDateTime = lastDateTime.plusDays(1);
        }

        return interpolatedData;
    }

    private static LocalDateTime normalize(LocalDateTime dateTime) {
        dateTime = dateTime.withSecond(0);
        dateTime = dateTime.withMinute(0);
        dateTime = dateTime.withHour(0);
        dateTime = dateTime.withNano(0);
        return dateTime;
    }
}
