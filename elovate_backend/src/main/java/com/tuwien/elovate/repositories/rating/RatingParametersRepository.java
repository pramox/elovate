package com.tuwien.elovate.repositories.rating;

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

import com.tuwien.elovate.entities.rating.RatingParameters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingParametersRepository extends JpaRepository<RatingParameters<?>, Long> {
}
