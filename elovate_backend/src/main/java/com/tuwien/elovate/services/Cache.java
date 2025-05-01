package com.tuwien.elovate.services;

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
import com.tuwien.elovate.dtos.statistics.RankDistributionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a cache used for frequently fetched entities to increase performance.
 */
@Service
public class Cache {
    private static final Logger log = LoggerFactory.getLogger(Cache.class);
    private static final int CACHE_VALID_TIME_FOR_LEADERBOARD = 10;
    private static final int CACHE_VALID_TIME_FOR_RANK_DISTRIBUTION = 10;
    Map<Long, Pair<List<LeaderboardPlayerDto>, Instant>> topPlayerMap;

    Map<Long, Pair<RankDistributionDto, Instant>> rankDistributionMap;

    public Cache() {
        this.topPlayerMap = new HashMap<>();
        this.rankDistributionMap = new HashMap<>();
    }

    public List<LeaderboardPlayerDto> getTopPlayers(Long count, Long gameId) {
        Pair<List<LeaderboardPlayerDto>, Instant> res = topPlayerMap.get(gameId);
        if (res == null) { // if cache miss
            return Collections.emptyList();
        }
        if (Duration.between(res.getSecond(), Instant.now()).toSeconds() > CACHE_VALID_TIME_FOR_LEADERBOARD) { // if cache is too old
            log.info("Cache refreshed for leaderboard");
            return Collections.emptyList();
        }
        if (res.getFirst().size() < count) { // if list of players is too short
            return Collections.emptyList();
        }
        log.info("Cached values used for leaderboard request");
        return res.getFirst().subList(0, count.intValue());
    }

    public void setTopPlayers(Long gameId, List<LeaderboardPlayerDto> topPlayers) {
        this.topPlayerMap.put(gameId, Pair.of(topPlayers, Instant.now())); // set cache with new time stamp
    }

    public RankDistributionDto getRankDistribution(Long gameId) {
        Pair<RankDistributionDto, Instant> rd = rankDistributionMap.get(gameId);
        if (rd == null) {
            return null;
        }
        if (Duration.between(rd.getSecond(), Instant.now()).toSeconds() > CACHE_VALID_TIME_FOR_RANK_DISTRIBUTION) { // if cache is too old
            log.info("Cache refreshed for rank distribution");
            return null;
        }
        log.info("Cached values used for rank distribution");
        return rd.getFirst();
    }

    public void setRankDistribution(Long gameId, RankDistributionDto rankDistribution) {
        this.rankDistributionMap.put(gameId, Pair.of(rankDistribution, Instant.now())); // set cache with new time stamp
    }
}
