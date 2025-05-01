package com.tuwien.elovate.services.game;

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

import com.tuwien.elovate.dtos.game.DeveloperDto;
import com.tuwien.elovate.dtos.game.GameAccessDto;
import com.tuwien.elovate.dtos.game.GameDetailDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.rating.RatingParameters;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameAccessRepository;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.rating.RatingRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.mapper.GameAccessMapper;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.validator.GameAccessValidator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final GameAccessRepository gameAccessRepository;
    private final GameAccessValidator gameAccessValidator;
    private final UserRepository userRepository;
    private final GameAccessMapper gameAccessMapper;
    private final RatingRepository ratingRepository;

    private final SessionUtils sessionUtils;

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private static final Long ACCESS_TOKEN_VALID_TIME = 3600L;

    public Game save(final Game game) {
        return gameRepository.save(game);
    }

    public GameAccess saveGameAccess(final GameAccess gameAccess) {
        return gameAccessRepository.save(gameAccess);
    }

    public Game getGameById(final Long id) {
        return gameRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND));
    }

    public GameAccess getGameAccessById(final String id) {
        return gameAccessRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Message.GAME_ACCESS_NOT_FOUND));
    }

    public GameDetailDto getGameDtoById(final Long id) {
        return gameToGameDTO(getGameById(id));
    }

    public RatingParameters<? extends Rating> getGameParameters(final Long gameId) {
        return gameRepository.findById(gameId).orElseThrow(() -> new EntityNotFoundException(Message.GAME_NOT_FOUND)).getRatingParameters();
    }

    private GameDetailDto gameToGameDTO(Game game) {
        GameDetailDto gameDetailDTO = GameDetailDto.builder()
                .id(game.getId())
                .name(game.getName())
                .genre(game.getGenre())
                .playersPerTeam(game.getPlayersPerTeam())
                .build();

        Image image = game.getGameApplication().getImage();
        if (image != null) {
            gameDetailDTO.setImage(image.getImageBase64());
        }
        gameDetailDTO.setDeveloperDetails(developerToDeveloperDTO(game.getGameApplication()));
        return gameDetailDTO;
    }

    private DeveloperDto developerToDeveloperDTO(GameApplication game) {
        return DeveloperDto.builder()
                .id(game.getDeveloper().getId())
                .nickname(game.getDeveloper().getNickName())
                .email(game.getDeveloper().getEmail())
                .build();
    }

    public Rating findRating(Long gameId, Long userId) {
        return gameAccessRepository
                .findByUser_IdAndGame_Id(userId, gameId)
                .orElseThrow(() -> new EntityNotFoundException(Message.GAME_ACCESS_USER_ID_DOES_NOT_MATCH_SESSION))
                .getRating();
    }

    public GameAccessDto getGameAccessId(GameAccessDto gameAccessDto) throws ValidationException {

        gameAccessValidator.validateGameAccessDto(gameAccessDto);

        Rating rating = null;

        // check if access code already exists
        Optional<GameAccess> gameAccessOldOpt = gameAccessRepository.findByUser_IdAndGame_Id(gameAccessDto.getUser(), gameAccessDto.getGame());
        if (gameAccessOldOpt.isPresent()) {
            Duration duration = Duration.between(gameAccessOldOpt.get().getCreatedOn(), Instant.now());
            // if still valid return it
            if (duration.getSeconds() <= ACCESS_TOKEN_VALID_TIME) {
                log.debug("Return valid game access with UUID {}", gameAccessOldOpt.get().getUuid());
                return gameAccessMapper.gameAccessToGameAccessDto(gameAccessOldOpt.get());
            } else { // else delete old token
                GameAccess gameAccessOld = gameAccessOldOpt.get();
                rating = gameAccessOld.getRating();
                gameAccessRepository.delete(gameAccessOld);
            }
        }

        GameAccess gameAccessNew = gameAccessMapper.gameAccessDtoToGameAccess(gameAccessDto);

        // if the rating is null we must create a new rating for the game access
        if (rating == null) {
            Optional<Game> game = gameRepository.findById(gameAccessDto.getGame());
            if (game.isEmpty()) {
                throw new ValidationException(Message.GAME_NOT_FOUND);
            }
            RatingParameters<? extends Rating> parameters = game.get().getRatingParameters();
            rating = parameters.createNewRating();
            rating = ratingRepository.save(rating);
        }

        gameAccessNew.setRating(rating);
        gameAccessNew = gameAccessRepository.save(gameAccessNew);
        log.debug("Created game access Id {} with timestamp {}", gameAccessNew.getUuid(), gameAccessNew.getCreatedOn());

        return gameAccessMapper.gameAccessToGameAccessDto(gameAccessNew);
    }

    public Stream<GameDetailDto> getAll(Long userId, boolean activated) {
        User user;
        if (userId == null) {
            user = sessionUtils.getActiveUser();
        } else {
            user = userRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException(Message.USER_NOT_FOUND)
            );
        }
        Set<Game> ownedGames = user.getConnectedGames();
        if (activated) {
            return ownedGames.stream().map(this::gameToGameDTO);
        }
        // return all games that are not owned/activated by the user
        Set<Game> allGames = new HashSet<>(gameRepository.findAll());
        allGames.removeAll(ownedGames);
        return allGames.stream().map(this::gameToGameDTO);
    }
}
