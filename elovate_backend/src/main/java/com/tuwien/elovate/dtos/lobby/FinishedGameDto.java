package com.tuwien.elovate.dtos.lobby;

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

import com.tuwien.elovate.dtos.game.GameDto;
import com.tuwien.elovate.enums.GameEndResult;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinishedGameDto {
    private GameDto game;
    private GameEndResult gameEndResult;
    private List<PlayerStatsPostGameDto> teamA;
    private List<PlayerStatsPostGameDto> teamB;
    private LocalDateTime finishTime;
    private String deletedLobby;
}
