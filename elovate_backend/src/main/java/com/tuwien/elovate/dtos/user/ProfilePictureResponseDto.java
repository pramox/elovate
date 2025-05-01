package com.tuwien.elovate.dtos.user;

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

import com.tuwien.elovate.services.validator.FileType;
import com.tuwien.elovate.services.validator.MaxFileSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfilePictureResponseDto {
    @MaxFileSize(max = 2 * 1024 * 1024)
    @FileType(fileTypes = {"image/png", "image/jpeg"})
    private MultipartFile image;

    private String imageBase64;
}
