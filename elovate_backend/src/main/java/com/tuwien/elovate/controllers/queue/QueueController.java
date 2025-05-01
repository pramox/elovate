package com.tuwien.elovate.controllers.queue;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dtos.queue.ClientToServerWebSocketMessageDto;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.services.queue.QueueService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.io.IOException;
import java.security.Principal;
import java.util.Objects;

/**
 * Controller class for managing WebSocket connections and queue-related messages.
 */
@Controller
@AllArgsConstructor
public class QueueController {

    private static final Logger log = LoggerFactory.getLogger(QueueController.class);

    private final QueueService queueService;

    /**
     * Processes and routes incoming queue messages from clients.
     *
     * @param message The client-to-server message data transfer object.
     */
    @MessageMapping("/send/message")
    @SendTo("/topic/message")
    public void queue(ClientToServerWebSocketMessageDto message) {
        queueService.handleQueueMessage(message);
    }

    /**
     * Handles a new WebSocket connection event.
     *
     * @param event The session connected event.
     * @throws BadRequestException if there is an issue in message conversion.
     */
    @EventListener
    @Transactional
    public void handleWebSocketConnectListener(SessionConnectedEvent event) throws BadRequestException {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        GenericMessage<byte[]> message = (GenericMessage<byte[]>) accessor.getHeader("simpConnectMessage");
        ClientToServerWebSocketMessageDto convertedMessage = convertMessage(Objects.requireNonNull(message).getPayload());
        Principal principal = Objects.requireNonNull(event.getUser()); // interceptor assures this is set
        queueService.handleWebSocketConnect(principal.getName(), convertedMessage);
    }

    /**
     * Handles WebSocket disconnection events.
     *
     * @param event The session disconnect event.
     */
    @EventListener
    @Transactional
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        queueService.handleWebSocketDisconnect(sessionId);
    }

    /**
     * Converts a byte array payload into a ClientToServerWebSocketMessageDto.
     *
     * @param payload The byte array message payload.
     * @return The converted message as ClientToServerWebSocketMessageDto.
     */
    private ClientToServerWebSocketMessageDto convertMessage(byte[] payload) {
        try {
            String answer = new String(payload);
            return new ObjectMapper().readValue(answer, ClientToServerWebSocketMessageDto.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadRequestException(Message.SERVER_ERROR);
        }
    }
}
