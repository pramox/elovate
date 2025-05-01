package com.tuwien.elovate.enums;

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

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GameEndResult {
    DRAW(0.5),
    TEAM_A_WINNER(1.0),
    TEAM_B_WINNER(0.0);

    final double score;

    public GameEndResult invert() {
        switch (this) {
            case TEAM_B_WINNER -> {
                return TEAM_A_WINNER;
            }
            case TEAM_A_WINNER -> {
                return TEAM_B_WINNER;
            }
            case DRAW -> {
                return DRAW;
            }
            default -> throw new IllegalStateException(); // this can't happen, but the compiler needs the default case
        }
    }
}
