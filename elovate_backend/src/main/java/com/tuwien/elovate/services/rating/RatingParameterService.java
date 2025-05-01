package com.tuwien.elovate.services.rating;

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

import com.tuwien.elovate.dtos.rating.Glicko2RatingParametersDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.rating.RatingParameters;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.BadRequestException;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.services.mapper.RatingParametersMapper;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.validator.RatingParametersValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RatingParameterService {

    private final SessionUtils sessionUtils;
    private final GameRepository gameRepository;
    private final RatingParametersRepository ratingParametersRepository;
    private final RatingParametersValidator ratingParametersValidator;
    private final RatingParametersMapper ratingParametersMapper;

    public Glicko2RatingParametersDto save(Glicko2RatingParametersDto dto) {
        ratingParametersValidator.validateGlicko2RatingParameters(dto);
        Glicko2RatingParameters mappedParameters = ratingParametersMapper.mapDtoToEntity(dto);
        return ratingParametersMapper.mapEntityToDto(ratingParametersRepository.save(mappedParameters));
    }

    public Glicko2RatingParametersDto getGlicko2RatingParametersForGame(Long gameId) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
        }
        Game game = gameOpt.get();

        User user = sessionUtils.getActiveUser();

        if (!user.isAdmin() && !Objects.equals(game.getGameApplication().getDeveloper().getId(), user.getId())) {
            throw new InvalidCredentialsException(Message.NOT_AUTHORIZED_TO_FETCH_RATING_PARAMETERS);
        }

        RatingParameters<? extends Rating> ratingParameters = game.getRatingParameters();
        if (ratingParameters.getRatingClass() != Glicko2Rating.class) {
            throw new BadRequestException(Message.GAME_HAS_DIFFERENT_RATING_TYPE);
        }
        Glicko2RatingParameters glicko2RatingParameters = (Glicko2RatingParameters) ratingParameters;

        return ratingParametersMapper.mapEntityToDto(glicko2RatingParameters);
    }

    public Glicko2RatingParametersDto updateGlicko2RatingParametersForGame(Long gameId, Glicko2RatingParametersDto dto) {
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
        }
        Game game = gameOpt.get();

        User user = sessionUtils.getActiveUser();

        if (!user.isAdmin() && !Objects.equals(game.getGameApplication().getDeveloper().getId(), user.getId())) {
            throw new InvalidCredentialsException(Message.NOT_AUTHORIZED_TO_FETCH_RATING_PARAMETERS);
        }

        RatingParameters<? extends Rating> ratingParameters = game.getRatingParameters();
        if (ratingParameters.getRatingClass() != Glicko2Rating.class) {
            throw new BadRequestException(Message.GAME_HAS_DIFFERENT_RATING_TYPE);
        }
        Glicko2RatingParameters currentGlicko2RatingParameters = (Glicko2RatingParameters) ratingParameters;

        ratingParametersValidator.validateGlicko2RatingParameters(dto);

        Glicko2RatingParameters updatedGlicko2RatingParameters = ratingParametersMapper.mapDtoToEntity(dto);
        updatedGlicko2RatingParameters.setId(currentGlicko2RatingParameters.getId());

        ratingParametersRepository.save(updatedGlicko2RatingParameters);
        game.setRatingParameters(updatedGlicko2RatingParameters);
        gameRepository.save(game);

        return ratingParametersMapper.mapEntityToDto(updatedGlicko2RatingParameters);
    }
}
