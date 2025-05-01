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

import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.enums.GameEndResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@NoArgsConstructor
public class MatchResultDto<T extends Rating> {

    List<T> teamOne;
    List<T> teamTwo;
    GameEndResult result;

    public MatchResultDto(List<T> teamOne, List<T> teamTwo, GameEndResult result) {
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
        this.result = result;
    }

    public double getScore(T player) {
        return teamOne.contains(player) ? result.getScore() : result.invert().getScore();
    }

    public List<T> getTeamOfPlayer(T player) {
        return teamOne.contains(player) ? teamOne : teamTwo;
    }

    public List<T> getOpponentTeamOfPlayer(T player) {
        return teamOne.contains(player) ? teamTwo : teamOne;
    }
}
