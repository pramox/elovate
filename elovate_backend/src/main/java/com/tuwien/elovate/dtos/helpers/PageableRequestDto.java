package com.tuwien.elovate.dtos.helpers;

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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageableRequestDto {

    @NotNull(message = "SIZE_MUST_BE_SET")
    @Min(value = 1, message = "SIZE_MUST_BE_AT_LEAST_ONE")
    private Integer size;

    @NotNull(message = "PAGE_MUST_BE_SET")
    @Min(value = 0, message = "PAGE_MUST_NOT_BE_NEGATIVE")
    private Integer page;

}
