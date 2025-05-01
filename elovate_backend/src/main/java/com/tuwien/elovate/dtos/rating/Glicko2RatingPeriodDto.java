package com.tuwien.elovate.dtos.rating;

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
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor
public class Glicko2RatingPeriodDto {

    List<Glicko2MatchResultDto> results = new ArrayList<>();

    List<Glicko2Rating> otherPlayers = new ArrayList<>();

    public List<Glicko2Rating> getPlayersWithMatch() {
        return results.stream()
                .map(result -> List.of(result.getTeamOne(), result.getTeamTwo()))
                .flatMap(Collection::stream)
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    public List<Glicko2Rating> getPlayersWithoutMatch() {
        List<Glicko2Rating> playersWithMatch = getPlayersWithMatch();
        otherPlayers.removeAll(playersWithMatch);
        return otherPlayers;
    }

    public List<Glicko2Rating> getAllPlayers() {
        return Stream.of(getPlayersWithoutMatch(), getPlayersWithMatch())
                .flatMap(Collection::stream)
                .distinct()
                .toList();
    }

    public List<Glicko2MatchResultDto> getResultsForPlayer(Glicko2Rating player) {
        return results.stream()
                .filter(result -> result.getTeamOne().contains(player) || result.getTeamTwo().contains(player))
                .toList();
    }

    /**
     * Sets the results.
     *
     * @param results The results.
     */
    // use manual setters to create copy of list, to avoid UnsupportedOperationException if the list that is set is immutable
    public void setResults(List<Glicko2MatchResultDto> results) {
        this.results = new ArrayList<>(results);
    }

    /**
     * Sets the other players.
     *
     * @param otherPlayers The other players.
     */
    // use manual setters to create copy of list, to avoid UnsupportedOperationException if the list that is set is immutable
    public void setOtherPlayers(List<Glicko2Rating> otherPlayers) {
        this.otherPlayers = new ArrayList<>(otherPlayers);
    }
}
