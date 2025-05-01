package com.tuwien.elovate.repositories.apikey;

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

import com.tuwien.elovate.entities.apikey.ApiKey;
import com.tuwien.elovate.entities.apikey.ApiKeyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface APIKeyRepository extends JpaRepository<ApiKey, ApiKeyId> {

    Optional<ApiKey> findByKey(String key);

}
