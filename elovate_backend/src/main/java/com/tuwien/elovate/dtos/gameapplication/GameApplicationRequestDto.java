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

import com.tuwien.elovate.enums.Genre;
import com.tuwien.elovate.services.validator.ValidImage;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameApplicationRequestDto {

    private final static String IMAGE_ASPECT_RATIO = "3:4";

    @NotBlank(message = "GAME_NAME_INVALID")
    @Size(max = 255, message = "GAME_NAME_INVALID")
    private String name;

    @NotNull(message = "GAME_GENRE_IS_NULL")
    private Genre genre;

    @NotNull(message = "DRAW_POSSIBLE_IS_NULL")
    private Boolean drawPossible;

    @NotNull(message = "PLAYERS_PER_TEAM_IS_NULL")
    @Min(message = "PLAYER_PER_TEAM_INVALID", value = 1)
    @Max(message = "PLAYER_PER_TEAM_INVALID", value = 5)
    private Integer playersPerTeam;

    //@MaxFileSize(max = 2 * 1024 * 1024, message = "FILE_SIZE_LIMIT_EXCEEDED")
    //@FileType(fileTypes = {"image/png", "image/jpeg"}, message = "FILE_EXTENSION_WRONG")
    //@FileAspectRatio(allowedRatio = IMAGE_ASPECT_RATIO, percentageTolerance = 10f, message = "FILE_ASPECT_RATIO_WRONG")
    @ValidImage(allowedRatio = IMAGE_ASPECT_RATIO, percentageTolerance = 10f, maxSize = 2 * 1024 * 1024, fileTypes = {"image/png", "image/jpeg"}, message = "FILE_INVALID")
    private MultipartFile image;

    public boolean isDrawPossible() {
        return drawPossible != null && drawPossible;
    }

}
