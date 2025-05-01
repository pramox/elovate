package com.tuwien.elovate.dtos.queue;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServerToClientWebSocketMessageDto {
    private String message;

    @JsonCreator // Indicates that Jackson should use this constructor for deserialization
    public ServerToClientWebSocketMessageDto(@JsonProperty("message") String message) {
        this.message = message;
    }
}
