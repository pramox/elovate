package com.tuwien.elovate.services.queue;

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
import com.tuwien.elovate.repositories.queue.QueuePlayerRepository;
import com.tuwien.elovate.services.rating.RatingService;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class QueuePlayerService {

    private static final double MATCHMAKING_PERCENTAGE = 0.6;

    private final QueuePlayerRepository queuePlayerRepository;
    private final RatingService ratingService;

    public boolean isUserInQueue(String username) {
        return queuePlayerRepository.hasQueuePlayerByUserName(username);
    }

    public List<Pair<List<QueuePlayer>, List<QueuePlayer>>> findPlayersForMatch(Game game) {

        List<QueuePlayer> queuedPlayers = queuePlayerRepository.findByGame_Id(game.getId());

        int playersPerMatch = game.getPlayersPerMatch();

        // if not enough players are even in the queue
        if (queuedPlayers.size() < playersPerMatch) {
            return new ArrayList<>();
        }

        if (game.getRatingParameters() == null) {
            return new ArrayList<>();
        }

        double maxRating = ratingService.getMaxRatingForGame(game);
        double minRating = ratingService.getMinRatingForGame(game);

        List<List<QueuePlayer>> groups = matchPlayers(queuedPlayers, playersPerMatch, maxRating, minRating);

        // if no group was found return empty game.
        if (groups.isEmpty()) {
            return new ArrayList<>();
        }

        List<Pair<List<QueuePlayer>, List<QueuePlayer>>> foundMatches = new ArrayList<>();

        double maxTeamRatingDiscrepancy = ratingService.getMaxRatingDiscrepancyForGame(game, MATCHMAKING_PERCENTAGE);

        for (List<QueuePlayer> group : groups) {
            if (group != null) {
                foundMatches.add(findValidTeamPartitioning(group, maxTeamRatingDiscrepancy));
            }
        }
        return foundMatches;
    }

    /**
     * Matches a List of Players in the queue in a lobby.
     * <p>
     * How does it work?
     * If for two intervals given in the form [lowerbound, rating, upperbound] both subintervals [lowerbound, rating], [rating, upperbound] overlap,
     * the initial two intervals also overlap.
     * This can be done in somewhat reasonable time O(n^2) but the average case is probably around Θ(n*log(n)).
     * After that the matching intervals will be returned
     *
     * @param players   players to be matched
     * @param matchSize size of match (teamSize * 2)
     * @param maxRating maximal rating for the provided game
     * @return a list of matches. This List can be empty if no valid match was found or can conntain multiple found matches.
     * If multiple lists are returned the Players are a disjoint set, thus it is ensured that no player can be in two matches.
     */
    private List<List<QueuePlayer>> matchPlayers(List<QueuePlayer> players, int matchSize, double maxRating, double minRating) {

        List<MatchmakingInterval> leftIntervals = new ArrayList<>();
        List<MatchmakingInterval> rightIntervals = new ArrayList<>();

        for (QueuePlayer player : players) { // O(n)
            MatchmakingInterval parentInterval =
                    new MatchmakingInterval(
                            player.getId(),
                            Math.max(player.getRating() - mapTimeToInterval(player.getStartedQueueingOn(), maxRating), minRating),
                            player.getRating() + mapTimeToInterval(player.getStartedQueueingOn(), maxRating),
                            player);

            leftIntervals.add(
                    new MatchmakingInterval(
                            player.getId(),
                            Math.max(player.getRating() - mapTimeToInterval(player.getStartedQueueingOn(), maxRating), minRating),
                            player.getRating(),
                            parentInterval));

            rightIntervals.add(
                    new MatchmakingInterval(
                            player.getId(),
                            player.getRating(),
                            player.getRating() + mapTimeToInterval(player.getStartedQueueingOn(), maxRating),
                            parentInterval));
        }

        List<List<MatchmakingInterval>> leftGroups = findOverlappingIntervals(leftIntervals); // O(n * log(n))
        List<List<MatchmakingInterval>> rightGroups = findOverlappingIntervals(rightIntervals); // O(n * log(n))

        List<List<MatchmakingInterval>> matchingGroupsTemp = new ArrayList<>();
        Set<Long> matchedPlayers = new HashSet<>(); // players which are already in a group and should not be matched again

        for (int i = 0; i < leftGroups.size(); i++) { // this is theoretically in O(n^2), but faster on average

            List<MatchmakingInterval> current = leftGroups.get(i);
            current.retainAll(rightGroups.get(i));
            current.removeIf(e -> matchedPlayers.contains(e.id)); // remove player if already matched

            if (current.size() == matchSize) {
                matchingGroupsTemp.add(current);
                matchedPlayers.addAll(current.stream().map(e -> e.id).toList());

            } else if (current.size() > matchSize) {
                matchingGroupsTemp.add(current.subList(0, matchSize));
                matchedPlayers.addAll(current.subList(0, matchSize).stream().map(e -> e.id).toList());
            }
        }

        List<List<MatchmakingInterval>> matchingGroups = matchingGroupsTemp.stream().map(
                outer -> outer.stream().map(inner -> inner.parent).collect(Collectors.toList())).toList();

        List<List<QueuePlayer>> queuePlayerGroups = new ArrayList<>();

        for (List<MatchmakingInterval> elem : matchingGroups) {
            queuePlayerGroups.add(elem.stream().map(e -> e.player).collect(Collectors.toList()));
        }
        return queuePlayerGroups;
    }

    /**
     * Finds intervals that overlap.
     * Runs in O(n * log(n))
     *
     * @param intervals a list of intervals [lowerbound, upperbound]
     * @return a list of overlapping intervals
     */
    private static List<List<MatchmakingInterval>> findOverlappingIntervals(List<MatchmakingInterval> intervals) {
        intervals.sort(Comparator.comparingDouble(o -> o.upperBound));
        Collections.reverse(intervals);

        List<IntervalEvent> events = new ArrayList<>();

        for (MatchmakingInterval interval : intervals) { // O(n)
            events.add(new IntervalEvent(interval, interval.lowerBound, Type.LOWERBOUND));
            events.add(new IntervalEvent(interval, interval.upperBound, Type.UPPERBOUND));
        }

        events.sort(Comparator.comparingDouble(o -> o.value));
        Collections.reverse(events);
        List<List<MatchmakingInterval>> groups = new ArrayList<>();

        List<MatchmakingInterval> currentGroup = new ArrayList<>();
        for (IntervalEvent event : events) {
            if (event.type == Type.UPPERBOUND) {
                currentGroup.add(event.interval);
            } else if (event.type == Type.LOWERBOUND) {
                currentGroup.remove(event.interval);
            }
            groups.add(new ArrayList<>(currentGroup));
        }
        return groups;
    }

    /**
     * Calculates the width of the rating search interval depending on how long a player already queued.
     *
     * @param startetQueueingOn start of queue time
     * @param maxRating         maximal rating for provided game
     * @return the width of the rating interval
     */
    private static double mapTimeToInterval(Instant startetQueueingOn, double maxRating) {
        double passedTimesInMinutes = Math.abs((Duration.between(startetQueueingOn, Instant.now()).toSeconds()) / 60d);
        return 0.15 / (1 + Math.pow(6, -passedTimesInMinutes + 1)) * maxRating;
    }

    /**
     * Tries to find valid and fair teams
     *
     * @param queuePlayers             the list of players that can be put into a team
     * @param maxTeamRatingDiscrepancy maximum rating that two teams may differ
     * @return the tuple of players in teamA and teamB, or null if none could be found
     */
    public Pair<List<QueuePlayer>, List<QueuePlayer>> findValidTeamPartitioning(final List<QueuePlayer> queuePlayers, double maxTeamRatingDiscrepancy) {
        if (queuePlayers.size() % 2 != 0 || queuePlayers.isEmpty()) {
            throw new IllegalStateException("Cannot build teams from uneven number of players");
        }
        final int teamSize = queuePlayers.size() / 2;
        queuePlayers.sort(Comparator.comparing(QueuePlayer::getRating));
        final Pair<Double, List<QueuePlayer>> min = findClosestTeamByAverage(queuePlayers, 0, 0, teamSize);
        final Pair<List<QueuePlayer>, List<QueuePlayer>> teamPair = findComplementaryTeam(queuePlayers, min.getSecond());
        if (min.getFirst() == 0
                || Math.abs(getAvgRanking(teamPair.getFirst()) - getAvgRanking(teamPair.getSecond())) < maxTeamRatingDiscrepancy) {
            return teamPair;
        }
        return null;
    }

    private Pair<Double, List<QueuePlayer>> findClosestTeamByAverage(final List<QueuePlayer> playerRatings,
                                                                     final double ratingSoFar,
                                                                     final int iteration,
                                                                     final int teamSize) {
        if (iteration == teamSize) {
            return Pair.of(Math.abs((getRankingSum(playerRatings)) - ratingSoFar), playerRatings);
        }
        Pair<Double, List<QueuePlayer>> min = Pair.of(-1D, List.of());
        for (int i = 0; i < playerRatings.size(); i++) {
            if (min.getFirst() == 0) {
                break;
            }
            final List<QueuePlayer> subList = new ArrayList<>(playerRatings);
            final QueuePlayer currentPlayerRating = playerRatings.get(i);
            subList.remove(currentPlayerRating);
            final Pair<Double, List<QueuePlayer>> result = findClosestTeamByAverage(subList, currentPlayerRating.getRating() + ratingSoFar, iteration + 1, teamSize);
            if (min.getFirst() == -1 || result.getFirst() < min.getFirst()) {
                min = result;
            }
        }
        return min;
    }

    private Pair<List<QueuePlayer>, List<QueuePlayer>> findComplementaryTeam(final List<QueuePlayer> allUsers, final List<QueuePlayer> bestMatchedTeam) {
        final List<QueuePlayer> teamA = new ArrayList<>();
        final List<QueuePlayer> teamB = new ArrayList<>();
        for (QueuePlayer queuePlayer : allUsers) {
            if (bestMatchedTeam.contains(queuePlayer)) {
                teamA.add(queuePlayer);
            } else {
                teamB.add(queuePlayer);
            }
        }
        return Pair.of(teamA, teamB);
    }

    private Double getAvgRanking(List<QueuePlayer> team) {
        final OptionalDouble average = team
                .stream()
                .map(QueuePlayer::getRating)
                .mapToDouble(Double::doubleValue)
                .average();
        return average.orElseThrow(
                () -> new IllegalStateException("Team may not be empty")
        );
    }

    private double getRankingSum(final List<QueuePlayer> queuePlayers) {
        long sum = 0;
        for (QueuePlayer p : queuePlayers) {
            sum += p.getRating();
        }
        return sum;
    }

    public int getUserCountForGame(Game game) {
        return queuePlayerRepository.getUserCountForGame(game);
    }

    public void deleteAll(List<QueuePlayer> queuePlayerList) {
        queuePlayerRepository.deleteAll(queuePlayerList);
    }

    public Optional<QueuePlayer> findQueuePlayerBySessionId(String simpSessionId) {
        return queuePlayerRepository.findQueuePlayerBySessionId(simpSessionId);
    }

    public void delete(QueuePlayer queuePlayer) {
        queuePlayerRepository.delete(queuePlayer);
    }

    public QueuePlayer save(QueuePlayer queuePlayer) {
        return queuePlayerRepository.save(queuePlayer);
    }

    public Optional<QueuePlayer> findQueuePlayerByUserName(String userName) {
        return queuePlayerRepository.findQueuePlayerByUserName(userName);
    }

    private static class MatchmakingInterval {

        private QueuePlayer player;
        private final Long id;
        private final double lowerBound;
        private final double upperBound;
        private MatchmakingInterval parent;

        /**
         * All classes below are helper classes only used for calculation purposes
         **/
        private MatchmakingInterval(Long id, double lowerBound, double upperBound, QueuePlayer player) {
            this.id = id;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.player = player;
        }

        private MatchmakingInterval(Long id, double lowerBound, double upperBound, MatchmakingInterval parent) {
            this.id = id;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            return Objects.equals(((MatchmakingInterval) o).id, this.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    private record IntervalEvent(MatchmakingInterval interval, double value, Type type) {
    }

    private enum Type {
        LOWERBOUND,
        UPPERBOUND,
        RATING
    }
}
