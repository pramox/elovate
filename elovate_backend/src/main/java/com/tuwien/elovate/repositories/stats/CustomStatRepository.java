package com.tuwien.elovate.repositories.stats;

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

import com.tuwien.elovate.entities.stats.CustomStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomStatRepository extends JpaRepository<CustomStat, Long> {
}
