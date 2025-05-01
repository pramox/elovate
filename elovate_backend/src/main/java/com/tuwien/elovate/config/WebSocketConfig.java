package com.tuwien.elovate.config;

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
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.services.authentication.JwtService;
import com.tuwien.elovate.services.queue.QueuePlayerService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.io.IOException;
import java.util.Objects;

/**
 * Configuration class for WebSocket communication, including STOMP endpoints and message broker.
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Order(value = Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketConfig.class);

    private final JwtService jwtService;
    private final QueuePlayerService queuePlayerService;

    @Value("${elovate.hostname}")
    private String hostname;

    /**
     * Configures the message broker for WebSocket communication.
     *
     * @param config The message broker registry.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/api/v1/app");
    }

    /**
     * Registers STOMP endpoints for WebSocket communication.
     *
     * @param registry The STOMP endpoint registry.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/api/v1/queue")
                .setAllowedOrigins(hostname)
                .withSockJS();
    }

    /**
     * Configures the client inbound channel for WebSocket communication.
     * Adds a custom interceptor to handle JWT validation and user authentication.
     *
     * @param registration The channel registration.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<byte[]> preSend(@NotNull Message<?> message1, @NotNull MessageChannel channel) {
                if (message1.getPayload().getClass() != byte[].class) {
                    throw new IllegalArgumentException("Invalid payload.");
                }

                @SuppressWarnings("unchecked")
                Message<byte[]> message = (Message<byte[]>) message1;

                StompHeaderAccessor accessor = Objects.requireNonNull(MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class));
                String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

                String userName = null;

                if (isValidAuthorizationHeader(authorizationHeader)) {
                    String token = authorizationHeader.substring(7);
                    try {
                        userName = jwtService.extractUserName(token);
                        String finalUserName = userName;
                        accessor.setUser(() -> finalUserName);
                        if (isQueueingEvent(accessor.getCommand()) && queuePlayerService.isUserInQueue(userName)) {
                            throw new ValidationException(com.tuwien.elovate.exceptions.archetype.Message.USER_ALREADY_IN_QUEUE);
                        }
                    } catch (Exception e) {
                        throw new InvalidCredentialsException(com.tuwien.elovate.exceptions.archetype.Message.JWT_TOKEN_INVALID);
                    }
                } else {
                    if (accessor.getCommand() != StompCommand.DISCONNECT) {
                        throw new InvalidCredentialsException(com.tuwien.elovate.exceptions.archetype.Message.NO_JWT_TOKEN_IN_HEADER);
                    }
                }

                message = editPayload(message, accessor, userName);

                return message;
            }
        });
    }

    /**
     * Returns whether the authorization header is correctly set, but does not validate the content.
     *
     * @param authorizationHeader The authorization header.
     * @return whether the authorization header is correctly set.
     */
    private boolean isValidAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader != null && authorizationHeader.startsWith("Bearer ");
    }

    /**
     * Edits the payload of a WebSocket message.
     * Parses and modifies the message content based on the user's authentication status.
     *
     * @param message  The original message.
     * @param accessor The STOMP header accessor.
     * @param userName The username extracted from the JWT token.
     * @return The modified message.
     */
    private Message<byte[]> editPayload(Message<byte[]> message, StompHeaderAccessor accessor, String userName) {
        byte[] messagePayload = message.getPayload();

        ObjectMapper mapper = new ObjectMapper();
        ClientToServerWebSocketMessageDto parsedMessage;

        if (messagePayload.length == 0) {
            parsedMessage = new ClientToServerWebSocketMessageDto(null, accessor.getSessionId(), userName, 0L);
        } else {
            try {
                parsedMessage = mapper.readValue(messagePayload, ClientToServerWebSocketMessageDto.class);
                parsedMessage.setSimpSessionId(accessor.getSessionId());
                parsedMessage.setSenderUserName(userName);
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new BadRequestException(com.tuwien.elovate.exceptions.archetype.Message.SERVER_ERROR);
            }
        }
        try {
            return new GenericMessage<>(mapper.writeValueAsBytes(parsedMessage), message.getHeaders());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException(com.tuwien.elovate.exceptions.archetype.Message.SERVER_ERROR);
        }
    }

    private boolean isQueueingEvent(StompCommand command) {
        return StompCommand.CONNECT == command;
    }
}

