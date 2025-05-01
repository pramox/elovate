package com.tuwien.elovate.services.gameapplication;

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

import com.tuwien.elovate.dtos.gameapplication.GameApplicationContractDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationPagedFilterDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationRequestDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDetailDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponsePagedDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationUpdateRequestDto;
import com.tuwien.elovate.dtos.gameapplication.comment.CommentRequestDto;
import com.tuwien.elovate.entities.common.Image;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.gameapplication.comment.Comment;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.PdfException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.repositories.gameapplication.comment.CommentRepository;
import com.tuwien.elovate.repositories.rating.RatingParametersRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.mapper.GameApplicationMapper;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.validator.GameApplicationValidator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class GameApplicationService {
    private final GameApplicationRepository gameApplicationRepository;
    private final GameRepository gameRepository;
    private final CommentRepository commentRepository;
    private final ResourceLoader resourceLoader;

    private final UserRepository userRepository;
    private final GameApplicationValidator gameApplicationValidator;

    @Autowired
    private GameApplicationMapper mapper; // must not be replaced by constructor injection for now - breaks tests

    @Autowired
    private RatingParametersRepository ratingParametersRepository;

    private final SessionUtils sessionUtils;

    private static final Logger log = LoggerFactory.getLogger(GameApplicationService.class);

    public GameApplicationContractDto getGameApplicationContract() {
        try {
            Resource resource = resourceLoader.getResource("classpath:game-application-contract/game-application-contract.pdf");
            File pdfFile = resource.getFile();
            byte[] pdfBytes = Files.readAllBytes(pdfFile.toPath());
            String encodedPdf = Base64.getEncoder().encodeToString(pdfBytes);
            return new GameApplicationContractDto(encodedPdf);
        } catch (IOException e) {
            throw new PdfException(Message.GAME_APPLICATION_CONTRACT_NOT_FOUND);
        }
    }

    public GameApplicationResponseDto save(GameApplicationRequestDto gameApplicationRequestDto) throws ValidationException {
        log.info("Attempt save on: {}", gameApplicationRequestDto);
        gameApplicationValidator.validateGameApplicationRequest(gameApplicationRequestDto);
        GameApplication gameApplication = gameApplicationRepository.save(gameApplicationRequestDTOToGameApplication(gameApplicationRequestDto));
        return mapper.gameApplicationToGameApplicationResponseDTO(gameApplication);
    }

    public GameApplicationResponsePagedDto getAllGameApplicationsForActiveUser(GameApplicationPagedFilterDto filter) {
        User user = sessionUtils.getActiveUser();
        Page<GameApplication> result;
        if (!user.getRoles().contains(Role.ADMIN)) {
            filter.setDeveloperId(user.getId());
            log.info("Attempt to get Game Applications for active user with id: {}", user.getId());
            user = userRepository.getReferenceById(filter.getDeveloperId());
            result = gameApplicationRepository.findAllByDeveloperAndStatus(user,
                    filter.getStatus(), PageRequest.of(filter.getPage(), filter.getSize()));
        } else if (filter.getDeveloperId() != null) {
            log.info("Attempt to get Game Applications for user with id: {}", filter.getDeveloperId());
            user = userRepository.getReferenceById(filter.getDeveloperId());
            result = gameApplicationRepository.findAllByDeveloperAndStatus(user,
                    filter.getStatus(), PageRequest.of(filter.getPage(), filter.getSize()));
        } else {
            log.info("Attempt to get all Game Application with status: {}", filter.getStatus());
            result = gameApplicationRepository.findAllByStatus(filter.getStatus(),
                    PageRequest.of(filter.getPage(), filter.getSize()));
        }
        return new GameApplicationResponsePagedDto(result.map(mapper::gameApplicationToGameApplicationResponseDTO));
    }

    public GameApplicationResponseDetailDto getGameApplicationById(Long id) {
        User user = sessionUtils.getActiveUser();
        GameApplication gameApplication = this.activeUserIsAllowedToAccessGameApplication(id, user);
        return mapper.gameApplicationToGameApplicationResponseDetailDTO(gameApplication);
    }

    public Long getGameByGameApplicationId(Long id) {
        Optional<Game> game = gameApplicationRepository.findGameByGameApplicationId(id);
        if (game.isEmpty()) {
            throw new EntityNotFoundException(Message.GAME_NOT_FOUND);
        }
        return game.get().getId();
    }

    public GameApplicationResponseDto updateGameApplicationStatusById(Long id, GameApplicationUpdateRequestDto gameApplicationUpdateRequestDTO)
            throws ValidationException {
        log.info("Attempt to get Game Application with id: {}", id);
        Optional<GameApplication> optional = gameApplicationRepository.findById(id);
        if (optional.isEmpty()) {
            log.error("Game Application with id: {} not found", id);
            throw new EntityNotFoundException(Message.GAME_APPLICATION_NOT_FOUND);
        }
        log.info("Attempt to update status of Game Application with id: {}", id);
        GameApplication gameApplication = optional.get();
        GameApplicationStatus newStatus = gameApplicationUpdateRequestDTO.getStatus();
        gameApplicationValidator.validateGameApplicationStatusTransition(gameApplication.getStatus(), newStatus);
        gameApplication.setStatus(newStatus);
        if (GameApplicationStatus.ACCEPTED == gameApplication.getStatus()) {
            log.info("Game Application with id {} was ACCEPTED, create game", id);
            Game newGame = gameRepository.save(Game.builder()
                    .name(gameApplication.getName())
                    .genre(gameApplication.getGenre())
                    .drawPossible(gameApplication.isDrawPossible())
                    .gameApplication(gameApplication)
                    .playersPerTeam(gameApplication.getPlayersPerTeam())
                    .build());
            if (ratingParametersRepository != null) { // can be null when mocked
                Glicko2RatingParameters ratingParameters = Glicko2RatingParameters.createNewRatingParameters();
                ratingParametersRepository.save(ratingParameters);
                newGame.setRatingParameters(ratingParameters);
            }
            if (gameApplication.getImage() != null) {
                Image image = gameApplication.getImage();
                newGame.setImage(image);
            }
            gameApplication.setGame(newGame);
            User dev = gameApplication.getDeveloper();
            if (!dev.getRoles().contains(Role.GAME_DEVELOPER)) {
                ArrayList<Role> roleSet = new ArrayList<>(dev.getRoles());
                roleSet.add(Role.GAME_DEVELOPER);
                dev.setRoles(new HashSet<>(roleSet));
            }
        }
        return mapper.gameApplicationToGameApplicationResponseDTO(gameApplicationRepository.save(gameApplication));
    }

    public GameApplicationResponseDetailDto commentOnGameApplication(Long id, CommentRequestDto commentRequestDTO) {
        User user = sessionUtils.getActiveUser();
        GameApplication gameApplication = this.activeUserIsAllowedToAccessGameApplication(id, user);
        gameApplication = gameApplicationRepository.save(addCommentToGameApplicationAsActiveUser(gameApplication,
                commentRequestDTO, user));
        return mapper.gameApplicationToGameApplicationResponseDetailDTO(gameApplication);
    }

    /*
    PRIVATE HELPERS
     */

    private GameApplication activeUserIsAllowedToAccessGameApplication(Long gameApplicationId, User user) throws EntityNotFoundException {
        log.info("Attempt to get Game Application with id: {}", gameApplicationId);
        Optional<GameApplication> optional = gameApplicationRepository.findById(gameApplicationId);
        if (optional.isEmpty()) {
            log.error("Game Application with id: {} not found", gameApplicationId);
            throw new EntityNotFoundException(Message.GAME_APPLICATION_NOT_FOUND);
        }
        if (!user.getRoles().contains(Role.ADMIN) &&
                !optional.get().getDeveloper().getId().equals(user.getId())) {
            log.error("Game Application with id: {} not found", gameApplicationId);
            throw new EntityNotFoundException(Message.GAME_APPLICATION_NOT_FOUND);
        }
        return optional.get();
    }

    private GameApplication addCommentToGameApplicationAsActiveUser(GameApplication gameApplication,
                                                                    CommentRequestDto commentRequestDTO,
                                                                    User activeUser) {
        log.info("Attempt to comment on Game Application with id: {}", gameApplication.getId());
        Comment comment = Comment.builder()
                .content(commentRequestDTO.getCommentContent())
                .gameApplication(gameApplication)
                .timestamp(Instant.now())
                .user(activeUser)
                .build();
        List<Comment> comments = gameApplication.getComments();
        comments.add(commentRepository.save(comment));
        gameApplication.setComments(comments);
        return gameApplication;
    }

    private GameApplication gameApplicationRequestDTOToGameApplication(GameApplicationRequestDto gameApplicationRequestDto) throws ValidationException {
        GameApplication gameApplication = GameApplication.builder()
                .name(gameApplicationRequestDto.getName())
                .genre(gameApplicationRequestDto.getGenre())
                .drawPossible(gameApplicationRequestDto.isDrawPossible())
                .status(GameApplicationStatus.PENDING)
                .playersPerTeam(gameApplicationRequestDto.getPlayersPerTeam())
                .developer(sessionUtils.getActiveUser())
                .build();
        if (gameApplicationRequestDto.getImage() != null && !gameApplicationRequestDto.getImage().isEmpty()) {
            try {
                Image image = new Image();
                image.setFileContent(gameApplicationRequestDto.getImage().getBytes());
                image.setName(gameApplicationRequestDto.getImage().getName());
                image.setOriginalFilename(gameApplication.getName() + "_" + "image");
                image.setContentType(gameApplicationRequestDto.getImage().getContentType());
                gameApplication.setImage(image);
            } catch (IOException e) {
                throw new ValidationException(Message.IMAGE_COULD_NOT_BE_SAVED);
            }
        }
        return gameApplication;
    }

    /**
     * Must only be used in testing.
     *
     * @param mapper The mapper to set.
     */
    public void setMapper(GameApplicationMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Must only be used in testing.
     *
     * @param ratingParametersRepository The repository to set.
     */
    public void setRatingParametersRepository(RatingParametersRepository ratingParametersRepository) {
        this.ratingParametersRepository = ratingParametersRepository;
    }
}
