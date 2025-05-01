package com.tuwien.elovate.services.external;

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

import com.tuwien.elovate.dtos.external.CustomStatDto;
import com.tuwien.elovate.dtos.external.CustomUserStatsDto;
import com.tuwien.elovate.dtos.external.ExternalGlicko2RatingParametersDto;
import com.tuwien.elovate.dtos.external.ExternalMatchmakingDto;
import com.tuwien.elovate.dtos.external.ExternalUser;
import com.tuwien.elovate.dtos.external.ExternalUserInfoDto;
import com.tuwien.elovate.dtos.external.GameEndInfoDto;
import com.tuwien.elovate.dtos.lobby.LobbyDto;
import com.tuwien.elovate.dtos.lobby.SimpleLobbyDto;
import com.tuwien.elovate.dtos.user.UserDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.game.GameAccess;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.rating.Rating;
import com.tuwien.elovate.entities.rating.RatingParameters;
import com.tuwien.elovate.entities.stats.CustomStat;
import com.tuwien.elovate.entities.stats.FinishedGame;
import com.tuwien.elovate.entities.stats.PlayerStatsPostGame;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameEndResult;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.stats.CustomStatRepository;
import com.tuwien.elovate.repositories.stats.FinishedGameRepository;
import com.tuwien.elovate.repositories.stats.PlayerStatsPostGameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.apikey.APIKeyService;
import com.tuwien.elovate.services.game.GameService;
import com.tuwien.elovate.services.lobby.LobbyService;
import com.tuwien.elovate.services.rating.RatingService;
import com.tuwien.elovate.services.validator.RatingParametersValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExternalGameApiService {

    private final RatingService ratingService;
    private final GameService gameService;
    private final APIKeyService apiKeyService;
    private final LobbyService lobbyService;
    private final FinishedGameRepository finishedGameRepository;
    private final PlayerStatsPostGameRepository playerStatsPostGameRepository;
    private final CustomStatRepository customStatRepository;
    private final UserRepository userRepository;
    private final RatingParametersValidator validator;

    private final Random random = new Random();

    public void validateApiKey(final String apiKey) {
        if (apiKey == null || !apiKeyService.validateAPIKey(apiKey)) {
            throw new InvalidCredentialsException(Message.API_KEY_IS_INVALID);
        }
    }

    public void validateApiKey(final String apiKey, final Long gameId) {
        if (apiKey == null || gameId == null || !apiKeyService.validateAPIKey(apiKey, gameId)) {
            throw new InvalidCredentialsException(Message.API_KEY_IS_INVALID);
        }
    }

    @Transactional
    public ExternalUserInfoDto unlockUser(final String uuid, String apiKey) {
        if (uuid == null) {
            throw new ValidationException(Message.UUID_NOT_VALID);
        }
        final GameAccess gameAccess = gameService.getGameAccessById(uuid);
        Game game = gameAccess.getGame();
        User user = gameAccess.getUser();
        apiKeyService.validateAPIKey(game.getId(), user.getId(), apiKey);
        gameAccess.setUnlocked(true);
        Set<Game> games = user.getConnectedGames();
        games.add(game);
        user.setConnectedGames(games);
        userRepository.save(user);
        gameService.saveGameAccess(gameAccess);
        return new ExternalUserInfoDto(gameAccess.getUser().getId(), gameAccess.getGame().getId(), true);
    }

    public List<SimpleLobbyDto> getAllLobbies(final Long gameId) {
        return lobbyService.findLobbiesByGameId(gameId);
    }

    public void updateUsersRatings(final String lobbyId, final GameEndInfoDto gameEndInfoDto) {
        if (gameEndInfoDto.getGameEndResult() == null) {
            throw new ValidationException(Message.NO_GAME_RESULT);
        }
        final LobbyDto lobby = lobbyService.findLobbyById(lobbyId);
        final Game game = gameService.getGameById(gameEndInfoDto.getGameId());

        if (!game.isDrawPossible() && gameEndInfoDto.getGameEndResult() == GameEndResult.DRAW) {
            throw new ValidationException(Message.DRAW_IS_NOT_ALLOWED);
        }

        final List<Rating> teamA = lobby.getTeamA().stream().map(userDto -> gameService.findRating(gameEndInfoDto.getGameId(), userDto.getId())).toList();
        final List<Rating> teamB = lobby.getTeamB().stream().map(userDto -> gameService.findRating(gameEndInfoDto.getGameId(), userDto.getId())).toList();
        // copy to keep old rating values
        final List<Double> teamACopy = teamA.stream().map(Rating::getRating).collect(Collectors.toList());
        final List<Double> teamBCopy = teamB.stream().map(Rating::getRating).collect(Collectors.toList());

        RatingParameters<? extends Rating> params = gameService.getGameParameters(gameEndInfoDto.getGameId());

        ratingService.updateRating(params != null ? params : Glicko2RatingParameters.createNewRatingParameters(), teamA, teamB, gameEndInfoDto.getGameEndResult(), true);
        lobbyService.deleteById(lobbyId);

        final FinishedGame finishedGame = createFinishedGame(gameEndInfoDto, game);
        finishedGame.setTeamA(createPlayerStats(lobby, teamACopy, teamA, true, gameEndInfoDto));
        finishedGame.setTeamB(createPlayerStats(lobby, teamBCopy, teamB, false, gameEndInfoDto));
        finishedGame.setDeletedLobby(lobbyId);
        finishedGameRepository.save(finishedGame);
    }

    private FinishedGame createFinishedGame(final GameEndInfoDto gameEndInfoDto, final Game game) {
        final FinishedGame finishedGame = new FinishedGame();
        finishedGame.setGame(game);
        finishedGame.setGameEndResult(gameEndInfoDto.getGameEndResult());
        finishedGame.setFinishTime(LocalDateTime.now());
        return finishedGame;
    }

    private List<PlayerStatsPostGame> createPlayerStats(final LobbyDto lobby, final List<Double> oldRatings, final List<Rating> newRatings, final boolean isTeamA, final GameEndInfoDto gameEndInfoDto) {
        final List<PlayerStatsPostGame> playerStats = new ArrayList<>();
        final List<UserDto> team = isTeamA ? lobby.getTeamA() : lobby.getTeamB();
        for (int i = 0; i < team.size(); i++) {
            Optional<User> userOpt = userRepository.findById(team.get(i).getId());
            if (userOpt.isEmpty()) {
                throw new ValidationException(Message.USER_NOT_FOUND);
            }
            User user = userOpt.get();
            final PlayerStatsPostGame playerStatsPostGame = new PlayerStatsPostGame(
                    null,
                    user,
                    team.get(i).getId(),
                    oldRatings.get(i),
                    newRatings.get(i).getRating(),
                    mapCustomerStatsDtoToCustomerStats(
                            getCustomStatsByUserId(team.get(i).getId(), gameEndInfoDto.getStats())
                    )
            );
            playerStats.add(playerStatsPostGame);
            playerStatsPostGameRepository.save(playerStatsPostGame);
        }
        return playerStats;
    }

    private List<CustomStatDto> getCustomStatsByUserId(final Long userId, final List<CustomUserStatsDto> customUserStatsDtos) {
        for (CustomUserStatsDto customUserStatsDto : customUserStatsDtos) {
            if (customUserStatsDto.getUserId().equals(userId)) {
                return customUserStatsDto.getStats();
            }
        }
        throw new ValidationException(Message.USER_NOT_FOUND); // theoretically unreachable
    }

    private List<CustomStat> mapCustomerStatsDtoToCustomerStats(final List<CustomStatDto> customStatDtos) {
        List<CustomStat> stats = customStatDtos
                .stream()
                .map(customStatDto -> new CustomStat(null, customStatDto.getStatName(), customStatDto.getStatValue()))
                .toList();
        return customStatRepository.saveAll(stats);
    }

    public ExternalMatchmakingDto performGlickoMatchmaking(final ExternalMatchmakingDto externalMatchmakingDto) {
        return mapRatingsAndPerformGlickoCalculation(externalMatchmakingDto, Glicko2RatingParameters.createNewRatingParameters());
    }

    public ExternalMatchmakingDto performGlickoMatchmaking(final ExternalMatchmakingDto externalMatchmakingDto,
                                                           final ExternalGlicko2RatingParametersDto externalGlicko2RatingParametersDto) {
        validator.validateExternalGlicko2RatingParameters(externalGlicko2RatingParametersDto);
        final Glicko2RatingParameters glicko2RatingParameters = Glicko2RatingParameters.builder()
                .tau(externalGlicko2RatingParametersDto.getTau())
                .defaultRating(externalGlicko2RatingParametersDto.getDefaultRating())
                .defaultRatingDeviation(externalGlicko2RatingParametersDto.getDefaultRatingDeviation())
                .defaultRatingVolatility(externalGlicko2RatingParametersDto.getDefaultRatingVolatility())
                .maxRating(externalGlicko2RatingParametersDto.getMax())
                .minRating(externalGlicko2RatingParametersDto.getMin())
                .build();
        return mapRatingsAndPerformGlickoCalculation(externalMatchmakingDto, glicko2RatingParameters);
    }

    private ExternalMatchmakingDto mapRatingsAndPerformGlickoCalculation(final ExternalMatchmakingDto externalMatchmakingDto,
                                                                         final Glicko2RatingParameters glicko2RatingParameters) {
        if (externalMatchmakingDto.getMatchResult() == null) {
            throw new ValidationException(Message.NO_GAME_RESULT);
        }
        if (externalMatchmakingDto.getTeamA().size() != externalMatchmakingDto.getTeamB().size()) {
            throw new ValidationException(Message.TEAM_SIZES_DIFFERENT);
        }
        final List<Rating> teamARatings = createRatings(externalMatchmakingDto.getTeamA());
        final List<Rating> teamBRatings = createRatings(externalMatchmakingDto.getTeamB());
        ratingService.updateRating(glicko2RatingParameters, teamARatings, teamBRatings, externalMatchmakingDto.getMatchResult(), false);
        return mapToExternalMatchmakingDto(teamARatings, teamBRatings, externalMatchmakingDto);
    }

    private List<Rating> createRatings(final List<ExternalUser> users) {
        return users.stream().map(externalUser -> {
            Glicko2Rating rating = new Glicko2Rating(
                    externalUser.getRatingVolatility(),
                    externalUser.getRatingDeviation(),
                    0.0,
                    0.0,
                    0.0
            );
            rating.setId(random.nextLong());
            rating.setRating(externalUser.getRating());
            return rating;
        }).collect(Collectors.toList());
    }

    private ExternalMatchmakingDto mapToExternalMatchmakingDto(final List<Rating> teamARatings,
                                                               final List<Rating> teamBRatings,
                                                               final ExternalMatchmakingDto externalMatchmakingDto) {
        for (int i = 0; i < externalMatchmakingDto.getTeamA().size(); i++) {
            final ExternalUser user = externalMatchmakingDto.getTeamA().get(i);
            final Glicko2Rating currentRating = (Glicko2Rating) teamARatings.get(i);
            user.setRating(currentRating.getRating());
            user.setRatingDeviation(currentRating.getRatingDeviation());
            user.setRatingVolatility(currentRating.getRatingVolatility());
        }
        for (int i = 0; i < externalMatchmakingDto.getTeamB().size(); i++) {
            final ExternalUser user = externalMatchmakingDto.getTeamB().get(i);
            final Glicko2Rating currentRating = (Glicko2Rating) teamBRatings.get(i);
            user.setRating(currentRating.getRating());
            user.setRatingDeviation(currentRating.getRatingDeviation());
            user.setRatingVolatility(currentRating.getRatingVolatility());
        }
        return externalMatchmakingDto;
    }
}
