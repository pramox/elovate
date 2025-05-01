package com.tuwien.elovate.services;

import com.tuwien.elovate.dtos.gameapplication.GameApplicationRequestDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationResponseDto;
import com.tuwien.elovate.dtos.gameapplication.GameApplicationUpdateRequestDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.enums.Genre;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import com.tuwien.elovate.services.gameapplication.GameApplicationService;
import com.tuwien.elovate.services.mapper.GameApplicationMapperImpl;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.validator.GameApplicationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameApplicationServiceUnitTest {

    @Mock
    private GameApplicationRepository gameApplicationRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameApplicationValidator gameApplicationValidator;

    @Mock
    private SessionUtils sessionUtils;

    @InjectMocks
    private GameApplicationService gameApplicationService;

    private GameApplicationRequestDto requestDto;

    private GameApplicationUpdateRequestDto updateRequestDTO;

    private User mockDeveloper;

    private GameApplication mockGameApplication;

    @BeforeEach
    void setUp() {
        requestDto = GameApplicationRequestDto.builder()
                .name("Test Game")
                .drawPossible(true)
                .genre(Genre.FIGHTING)
                .playersPerTeam(5)
                .build();
        updateRequestDTO = GameApplicationUpdateRequestDto.builder()
                .status(GameApplicationStatus.ACCEPTED)
                .build();

        mockDeveloper = User.builder()
                .id(1L)
                .roles(Set.of(Role.USER, Role.GAME_DEVELOPER))
                .build();

        mockGameApplication = GameApplication.builder()
                .id(1L)
                .developer(mockDeveloper)
                .status(GameApplicationStatus.PENDING)
                .drawPossible(true)
                .playersPerTeam(5)
                .build();

        gameApplicationService.setMapper(new GameApplicationMapperImpl());
    }

    @Test
    void save_gameApplication_successful() throws ValidationException {

        when(sessionUtils.getActiveUser()).thenReturn(mockDeveloper);

        when(gameApplicationRepository.save(any(GameApplication.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        GameApplicationResponseDto responseDto = gameApplicationService.save(requestDto);

        assertNotNull(responseDto);
        assertEquals(requestDto.getName(), responseDto.getName());
        verify(gameApplicationValidator, times(1)).validateGameApplicationRequest(requestDto);
        verify(gameApplicationRepository, times(1)).save(any(GameApplication.class));
    }

    @Test
    void save_gameApplication_throwsValidationException() throws ValidationException {

        doThrow(new ValidationException(Message.MOCK_STATUS_ID))
                .when(gameApplicationValidator).validateGameApplicationRequest(requestDto);

        assertThrows(ValidationException.class, () -> gameApplicationService.save(requestDto));
        verify(gameApplicationRepository, never()).save(any(GameApplication.class));
    }

    @Test
    void accept_gameApplicationStatus_successful() throws ValidationException {
        when(gameApplicationRepository.findById(1L))
                .thenReturn(Optional.of(mockGameApplication));
        when(gameApplicationRepository.save(any(GameApplication.class)))
                .thenAnswer(i -> i.getArguments()[0]);
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        GameApplicationResponseDto responseDto = gameApplicationService.updateGameApplicationStatusById(1L, updateRequestDTO);

        assertNotNull(responseDto);
        assertEquals(GameApplicationStatus.ACCEPTED, responseDto.getStatus());
        verify(gameApplicationValidator, times(1))
                .validateGameApplicationStatusTransition(GameApplicationStatus.PENDING, GameApplicationStatus.ACCEPTED);
        verify(gameApplicationRepository, times(1)).findById(1L);
        verify(gameApplicationRepository, times(1)).save(any(GameApplication.class));
    }

    @Test
    void accept_gameApplicationStatus_throwsEntityNotFoundException() {
        when(gameApplicationRepository.findById(2L))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> gameApplicationService.updateGameApplicationStatusById(2L, updateRequestDTO));
        verify(gameApplicationRepository, times(1)).findById(2L);
        verify(gameApplicationRepository, never()).save(any(GameApplication.class));
    }

    @Test
    void accept_gameApplicationStatus_throwsValidationException() throws ValidationException {
        doThrow(new ValidationException(Message.MOCK_STATUS_ID))
                .when(gameApplicationValidator).validateGameApplicationStatusTransition(GameApplicationStatus.PENDING,
                        GameApplicationStatus.PENDING);

        when(gameApplicationRepository.findById(1L))
                .thenReturn(Optional.of(mockGameApplication));
        updateRequestDTO.setStatus(GameApplicationStatus.PENDING);

        assertThrows(ValidationException.class, () -> gameApplicationService.updateGameApplicationStatusById(1L, updateRequestDTO));
        verify(gameApplicationRepository, never()).save(any(GameApplication.class));
    }

    @Test
    void reject_gameApplicationStatus_successful() throws ValidationException {
        updateRequestDTO.setStatus(GameApplicationStatus.REJECTED);

        when(gameApplicationRepository.findById(1L))
                .thenReturn(Optional.of(mockGameApplication));
        when(gameApplicationRepository.save(any(GameApplication.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        GameApplicationResponseDto responseDto = gameApplicationService.updateGameApplicationStatusById(1L, updateRequestDTO);

        assertNotNull(responseDto);
        assertEquals(GameApplicationStatus.REJECTED, responseDto.getStatus());
        verify(gameApplicationValidator, times(1))
                .validateGameApplicationStatusTransition(GameApplicationStatus.PENDING, GameApplicationStatus.REJECTED);
        verify(gameApplicationRepository, times(1)).findById(1L);
        verify(gameApplicationRepository, times(1)).save(any(GameApplication.class));
    }
}
