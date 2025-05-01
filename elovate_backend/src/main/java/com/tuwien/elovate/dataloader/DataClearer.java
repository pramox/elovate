package com.tuwien.elovate.dataloader;

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

import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.gameapplication.comment.CommentRepository;
import com.tuwien.elovate.repositories.lobby.LobbyRepository;
import com.tuwien.elovate.repositories.queue.QueuePlayerRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataClearer {

    private static final Logger log = LoggerFactory.getLogger(DataClearer.class);
    private final UserRepository userRepository;
    private final GameApplicationRepository gameApplicationRepository;
    private final GameRepository gameRepository;
    private final CommentRepository commentRepository;
    private final LobbyRepository lobbyRepository;
    private final GameAccessRepository gameAccessRepository;
    private final QueuePlayerRepository queuePlayerRepository;
    private final RatingRepository ratingRepository;
    private final RatingParametersRepository ratingParametersRepository;
    private final FinishedGameRepository finishedGameRepository;
    private final PlayerStatsPostGameRepository playerStatsPostGameRepository;

    /**
     * Deletes all the data from the database.
     */
    @PostConstruct
    public void clear() {
        log.info("Clearing database completely");

        finishedGameRepository.findAll().forEach(finishedGame -> {
            finishedGame.setTeamA(null);
            finishedGame.setTeamB(null);
            finishedGame.setGame(null);
            finishedGameRepository.save(finishedGame);
        });
        playerStatsPostGameRepository.findAll().forEach(playerStatsPostGame -> {
            playerStatsPostGame.setUserId(null);
            playerStatsPostGameRepository.save(playerStatsPostGame);
        });
        userRepository.findAll().forEach(game -> {
            game.setConnectedGames(null);
            userRepository.save(game);
        });
        gameRepository.findAll().forEach(game -> {
            game.setGameApplication(null);
            game.setRatingParameters(null);
            gameRepository.save(game);
        });
        gameAccessRepository.findAll().forEach(gameAccess -> {
            gameAccess.setRating(null);
            gameAccessRepository.save(gameAccess);
        });
        gameApplicationRepository.findAll().forEach(gameApplication -> {
            gameApplication.setDeveloper(null);
            gameApplication.setGame(null);
            gameApplication.setComments(null);
            gameApplicationRepository.save(gameApplication);
        });

        lobbyRepository.findAll().forEach(lobby -> {
            lobby.setGame(null);
            lobbyRepository.save(lobby);
        });

        ratingRepository.deleteAll();
        ratingParametersRepository.deleteAll();
        lobbyRepository.deleteAll();
        gameAccessRepository.deleteAll();
        finishedGameRepository.deleteAll();
        playerStatsPostGameRepository.deleteAll();
        ratingRepository.deleteAll();
        ratingParametersRepository.deleteAll();
        lobbyRepository.deleteAll();
        gameAccessRepository.deleteAll();
        queuePlayerRepository.deleteAll();
        gameRepository.deleteAll();
        commentRepository.findAll().forEach(c -> c.setUser(null));
        commentRepository.deleteAll();
        gameApplicationRepository.deleteAll();
        userRepository.findAll().forEach(user -> {
            user.setFriends(null);
            user.setBlocked(null);
            userRepository.save(user);
        });
        userRepository.deleteAll();
    }
}
