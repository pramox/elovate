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
public class ClientToServerWebSocketMessageDto {
    private String message;
    private String simpSessionId;
    private String senderUserName;

    private Long gameId;

    @JsonCreator // Indicates that Jackson should use this constructor for deserialization
    public ClientToServerWebSocketMessageDto(@JsonProperty("message") String message,
                                             @JsonProperty("simpSessionId") String simpSessionId,
                                             @JsonProperty("senderUserName") String senderUserName,
                                             @JsonProperty("gameId") Long gameId) {
        this.message = message;
        this.simpSessionId = simpSessionId;
        this.senderUserName = senderUserName;
        this.gameId = gameId;
    }
}
