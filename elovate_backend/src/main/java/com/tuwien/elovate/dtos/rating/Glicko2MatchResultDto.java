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
import com.tuwien.elovate.enums.GameEndResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class Glicko2MatchResultDto extends MatchResultDto<Glicko2Rating> {

    public Glicko2MatchResultDto(List<Glicko2Rating> teamOne, List<Glicko2Rating> teamTwo, GameEndResult result) {
        super(teamOne, teamTwo, result);
    }

    public double getOpponentRatingDeviation(Glicko2Rating player) {
        List<Glicko2Rating> opponents = teamOne.contains(player) ? teamTwo : teamOne;
        if (opponents.size() == 1) {
            return opponents.get(0).getRatingDeviation();
        }
        double sum = opponents.stream().map(Glicko2Rating::getRatingDeviation).mapToDouble(v -> v).sum();
        return sum / opponents.size();
    }

}
