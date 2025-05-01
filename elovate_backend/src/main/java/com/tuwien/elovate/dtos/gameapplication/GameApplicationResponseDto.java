package com.tuwien.elovate.dtos.gameapplication;

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

import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GameApplicationResponseDto {

    private Long id;

    private String name;

    private Genre genre;

    private boolean drawPossible;

    private Integer playersPerTeam;

    private GameApplicationStatus status;

    private Long developerId;

    private String image;

    private Long gameId;

}
