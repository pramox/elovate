package com.tuwien.elovate.services.queue;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuwien.elovate.dtos.lobby.LobbyCreationDto;
import com.tuwien.elovate.dtos.lobby.LobbyDto;
import com.tuwien.elovate.dtos.queue.ClientToServerWebSocketMessageDto;
import com.tuwien.elovate.dtos.queue.ServerToClientWebSocketMessageDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.queue.QueuePlayer;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.queue.QueuePlayerRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.lobby.LobbyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing game queues.
 */
@Service
@AllArgsConstructor
public class QueueService {

    private static final Logger log = LoggerFactory.getLogger(QueueService.class);

    private static final String MATCH_STARTING_MESSAGE = "MATCH STARTING: ";
    private static final String QUEUEING_AMOUNT_MESSAGE = "QUEUED: ";
    private static final String QUEUE_JOIN_REQUEST_MESSAGE = "Queue-Requested";

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final QueuePlayerService queuePlayerService;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final QueuePlayerRepository queuePlayerRepository;
    private final GameAccessRepository gameAccessRepository;

    /**
     * Checks the status of the game queue at fixed intervals and creates
     * the lobbies for the games.
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void createLobbies() {
        List<Game> games = gameRepository.findAll();

        for (Game game : games) {
            while (true) {
                final long startTime = System.currentTimeMillis();
                List<Pair<List<QueuePlayer>, List<QueuePlayer>>> teamList = queuePlayerService.findPlayersForMatch(game);
                final long endTime = System.currentTimeMillis();
                if (endTime - startTime > 1000) {
                    log.warn("Execution time of matchmaking routine is slow: " + (endTime - startTime) + "ms [" + game.getName() + "]");
                }
                if (teamList.isEmpty()) {
                    break; // no match could be found --> try again in 10 seconds
                }

                for (Pair<List<QueuePlayer>, List<QueuePlayer>> teams : teamList) {
                    List<QueuePlayer> teamOne = teams.getFirst();
                    List<QueuePlayer> teamTwo = teams.getSecond();

                    List<Long> teamOneUserIds = teamOne.stream().map(player -> player.getUser().getId()).toList();
                    List<Long> teamTwoUserIds = teamTwo.stream().map(player -> player.getUser().getId()).toList();

                    LobbyCreationDto lobbyCreationDto = new LobbyCreationDto(teamOneUserIds, teamTwoUserIds, game.getId());
                    LobbyDto lobby = lobbyService.createLobby(lobbyCreationDto);

                    List<QueuePlayer> allPlayers = new ArrayList<>(teamOne);
                    allPlayers.addAll(teamTwo);

                    for (QueuePlayer user : allPlayers) {
                        if (user.getSessionId() == null) {
                            continue; // is always set in production, but for testing the session does not have to be created
                        }
                        ServerToClientWebSocketMessageDto privateMessage = new ServerToClientWebSocketMessageDto(MATCH_STARTING_MESSAGE + lobby.getId());
                        sendMessageToUser(privateMessage, user.getUser().getUsername());
                    }

                    queuePlayerService.deleteAll(allPlayers);
                }
            }
        }
    }

    /**
     * Send the amount of queued players into the public channel for the queue for updating the frontend.
     */
    @Scheduled(fixedRate = 3, timeUnit = TimeUnit.SECONDS)
    public void updatePlayersPerQueue() {
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            int queuedPlayerForGame = queuePlayerService.getUserCountForGame(game);
            if (queuedPlayerForGame > 0) {
                ServerToClientWebSocketMessageDto publicMessage = new ServerToClientWebSocketMessageDto(QUEUEING_AMOUNT_MESSAGE + queuedPlayerForGame);
                sendMessageToAllInQueueForGame(game, publicMessage);
            }
        }
    }

    /**
     * Sends a message to all users in the queue for a specific game.
     *
     * @param game    The game for which users are queued.
     * @param message The message to send.
     */
    public void sendMessageToAllInQueueForGame(Game game, ServerToClientWebSocketMessageDto message) {
        try {
            String converted = new ObjectMapper().writeValueAsString(message);
            simpMessagingTemplate.convertAndSend("/topic/message/" + game.getId(), converted);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new BadRequestException(Message.SERVER_ERROR);
        }
    }

    /**
     * Sends a message to a specific user.
     *
     * @param message  The message to be sent.
     * @param username The username of the recipient.
     */
    public void sendMessageToUser(ServerToClientWebSocketMessageDto message, String username) {
        try {
            String converted = new ObjectMapper().writeValueAsString(message);
            simpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", converted);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new BadRequestException(Message.SERVER_ERROR);
        }
    }

    /**
     * Handles incoming messages from clients that want to join the queue.
     *
     * @param message The client-to-server message.
     */
    public void handleQueueMessage(ClientToServerWebSocketMessageDto message) {
        Optional<QueuePlayer> queuePlayerOpt = queuePlayerService.findQueuePlayerBySessionId(message.getSimpSessionId());
        if (queuePlayerOpt.isEmpty()) {
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }

        String messageString = message.getMessage();
        if (!QUEUE_JOIN_REQUEST_MESSAGE.equals(messageString)) {
            throw new BadRequestException(Message.INVALID_QUEUE_ACTION);
        }

        QueuePlayer queuePlayer = queuePlayerOpt.get();
        if (message.getGameId() == null) {
            queuePlayerService.delete(queuePlayer);
            throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
        }

        Optional<Game> gameOpt = gameRepository.findById(message.getGameId());

        if (gameOpt.isEmpty()) {
            queuePlayerService.delete(queuePlayer);
            throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
        }

        Game game = gameOpt.get();

        Optional<GameAccess> accessOpt = gameAccessRepository.findByUser_IdAndGame_Id(queuePlayer.getUser().getId(), game.getId());
        if (accessOpt.isEmpty() || !accessOpt.get().isUnlocked()) {
            queuePlayerRepository.delete(queuePlayer);
            throw new ValidationException(Message.GAME_ACCESS_USER_OR_GAME_ID_INCORRECT);
        }
        GameAccess access = accessOpt.get();

        log.info("User {} joined queue for game {} with sessionId {}", queuePlayer.getUser().getUsername(), game.getName(), message.getSimpSessionId());
        queuePlayer.setGame(game);
        queuePlayer.setStartedQueueingOn(Instant.now());
        queuePlayer.setRating(access.getRating().getRating());

        queuePlayerService.save(queuePlayer);

        int queuedPlayerForGame = queuePlayerService.getUserCountForGame(game);
        ServerToClientWebSocketMessageDto messageDto = new ServerToClientWebSocketMessageDto(QUEUEING_AMOUNT_MESSAGE + queuedPlayerForGame);
        sendMessageToUser(messageDto, queuePlayer.getUser().getUsername());
    }

    /**
     * Handles a new WebSocket connection.
     *
     * @param userName         The username of the connecting user.
     * @param convertedMessage The initial message from the user.
     */
    public void handleWebSocketConnect(String userName, ClientToServerWebSocketMessageDto convertedMessage) {
        Optional<QueuePlayer> queuePlayerOpt = queuePlayerService.findQueuePlayerByUserName(userName);
        if (queuePlayerOpt.isEmpty()) {
            Optional<User> user = userRepository.findByEmail(userName);
            if (user.isEmpty()) {
                throw new EntityNotFoundException(Message.USER_NOT_FOUND);
            }
            QueuePlayer queuePlayer = QueuePlayer.builder()
                    .user(user.get())
                    .sessionId(convertedMessage.getSimpSessionId())
                    .build();
            queuePlayerService.save(queuePlayer);
        }
    }

    /**
     * Handles a user's disconnection from the WebSocket.
     *
     * @param sessionId The session ID of the disconnected user.
     */
    public void handleWebSocketDisconnect(String sessionId) {
        Optional<QueuePlayer> queuePlayerOpt = queuePlayerService.findQueuePlayerBySessionId(sessionId);
        if (queuePlayerOpt.isPresent()) {
            QueuePlayer queuePlayer = queuePlayerOpt.get();
            log.info("User {} disconnected", queuePlayer.getUser().getUsername());
            queuePlayerService.delete(queuePlayer);
        }
    }
}
