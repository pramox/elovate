package com.tuwien.elovate.dtos.statistics;

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

import java.time.LocalDateTime;

public interface IEloByDayDto {
    Long getId();

    LocalDateTime getFinishTime();

    Double getRatingAfter();


}
