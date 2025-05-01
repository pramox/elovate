package com.tuwien.elovate.services;

import com.tuwien.elovate.dtos.user.UserGameRankingPagedDto;
import com.tuwien.elovate.dtos.user.UserProfileDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.gameapplication.GameApplication;
import com.tuwien.elovate.entities.rating.Glicko2Rating;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.game.GameService;
import com.tuwien.elovate.services.mapper.GameMapperImpl;
import com.tuwien.elovate.services.mapper.UserMapperImpl;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.users.UserService;
import com.tuwien.elovate.services.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerInteractionUnitTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionUtils sessionUtils;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private UserService userService;

    @Mock
    private GameService gameService;

    private User mockUser1, mockUser2, mockUser3;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        mockUser1 = User.builder()
                .id(1L)
                .roles(Set.of(Role.USER))
                .email("user1@email.com")
                .nickName("user1")
                .build();
        mockUser2 = User.builder()
                .id(2L)
                .roles(Set.of(Role.USER))
                .email("user2@email.com")
                .nickName("user2")
                .build();
        mockUser3 = User.builder()
                .id(3L)
                .roles(Set.of(Role.USER))
                .email("user3@email.com")
                .nickName("user3")
                .build();

        mockUser1.setFriends(new HashSet<>(Set.of(mockUser2)));
        mockUser2.setFriends(new HashSet<>(Set.of(mockUser1)));
        mockUser3.setFriends(new HashSet<>());
        mockUser1.setBlocked(new HashSet<>(Set.of(mockUser3)));
        mockUser2.setBlocked(new HashSet<>());
        mockUser3.setBlocked(new HashSet<>());

        User mockDeveloper = User.builder()
                .id(1L)
                .nickName("developer")
                .roles(Set.of(Role.GAME_DEVELOPER, Role.USER))
                .build();
        mockGame = Game.builder()
                .id(1L)
                .name("game")
                .gameApplication(GameApplication.builder()
                        .developer(mockDeveloper)
                        .drawPossible(false)
                        .playersPerTeam(5).build())
                .drawPossible(false)
                .playersPerTeam(5).build();
        mockUser1.setConnectedGames(new HashSet<>(Set.of(mockGame)));

        userService.setMappers(new UserMapperImpl(), new GameMapperImpl());
    }

    @Test
    void block_user_successful() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser3));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDto response = userService.block(mockUser3.getId());

        assertNotNull(response);
        assertTrue(response.isBlocked());
        verify(userValidator, times(1)).validateUsersBlockable(mockUser2, mockUser3);
        verify(userRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).save(mockUser2);
    }

    @Test
    void block_themself_throws_validationException() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser3);
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser3));
        doThrow(ValidationException.class).when(userValidator).validateUsersBlockable(mockUser3, mockUser3);

        long id = mockUser3.getId();
        assertThrows(ValidationException.class, () -> userService.block(id));
        verify(userRepository, times(1)).findById(3L);
        verify(sessionUtils, times(1)).getActiveUser();
        verify(userValidator, times(1)).validateUsersBlockable(mockUser3, mockUser3);
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void block_unknown_user_throws_entityNotFoundException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.block(6L));
        verify(userRepository, times(1)).findById(6L);
        verify(sessionUtils, never()).getActiveUser();
        verify(userValidator, never()).validateUsersBlockable(any(User.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unblock_user_successful() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser1);
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser3));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDto response = userService.unblock(mockUser3.getId());

        assertNotNull(response);
        assertFalse(response.isBlocked());
        verify(userValidator, times(1)).validateUsersUnblockable(mockUser1, mockUser3);
        verify(userRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).save(mockUser1);
    }

    @Test
    void unblock_unblocked_user_throws_validationException() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser3));
        doThrow(ValidationException.class).when(userValidator).validateUsersUnblockable(mockUser2, mockUser3);

        long id = mockUser3.getId();
        assertThrows(ValidationException.class, () -> userService.unblock(id));
        verify(userRepository, times(1)).findById(3L);
        verify(sessionUtils, times(1)).getActiveUser();
        verify(userValidator, times(1)).validateUsersUnblockable(mockUser2, mockUser3);
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void unblock_unknown_user_throws_entityNotFoundException() {
        when(userRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.unblock(6L));
        verify(userRepository, times(1)).findById(6L);
        verify(sessionUtils, never()).getActiveUser();
        verify(userValidator, never()).validateUsersUnblockable(any(User.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void unfriend_user_successful() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser1);
        when(userRepository.findById(2L)).thenReturn(Optional.of(mockUser2));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfileDto response = userService.unfriend(mockUser2.getId());

        assertNotNull(response);
        assertFalse(response.isFriend());
        verify(userValidator, times(1)).validateFriendsRemovable(mockUser1, mockUser2);
        verify(userRepository, times(1)).findById(2L);
        verify(userRepository, times(1)).save(mockUser1);
        verify(userRepository, times(1)).save(mockUser2);
    }

    @Test
    void unfriend_stranger_throws_validationException() {
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);
        when(userRepository.findById(3L)).thenReturn(Optional.of(mockUser3));
        doThrow(ValidationException.class).when(userValidator).validateFriendsRemovable(mockUser2, mockUser3);

        long id = mockUser3.getId();
        assertThrows(ValidationException.class, () -> userService.unfriend(id));
        verify(userRepository, times(1)).findById(3L);
        verify(userValidator, times(1)).validateFriendsRemovable(mockUser2, mockUser3);
        verify(userRepository, never()).save(mockUser2);
        verify(userRepository, never()).save(mockUser3);
    }

    @Test
    void unfriend_unknown_user_throws_entityNotFoundException() {
        when(userRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.unfriend(6L));
        verify(userRepository, times(1)).findById(6L);
        verify(sessionUtils, never()).getActiveUser();
        verify(userValidator, never()).validateFriendsRemovable(any(User.class), any(User.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void user_getAllRankedGames_successful() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser1));
        when(userRepository.findAllConnectedGames(1L, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(Collections.singletonList(mockGame)));
        var rating = new Glicko2Rating();
        rating.setRating(1000.);
        when(gameService.findRating(any(), any())).thenReturn(rating);

        UserGameRankingPagedDto response = userService.getAllRankedGames(1L, 0, 5);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(mockGame.getName(), response.getValues().get(0).getGame().getName());
        assertEquals(mockUser1.getEmail(), response.getValues().get(0).getUser().getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1))
                .findAllConnectedGames(1L, PageRequest.of(0, 5));
    }

    @Test
    void unknown_user_getAllRankedGames_throws_entityNotFoundException() {
        when(userRepository.findById(6L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getAllRankedGames(6L, 0, 10));
        verify(userRepository, times(1)).findById(6L);
        verify(sessionUtils, never()).getActiveUser();
        verify(userRepository, never()).findAllConnectedGames(any(Long.class), any(PageRequest.class));
    }

    @Test
    void unknown_user_confirm_throws_entityNotFoundException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.confirm(6L));
    }

    @Test
    void unknown_user_befriend_throws_entityNotFoundException() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.befriend(6L));
    }

    @Test
    void user_confirm_returns_correctDto() {
        mockUser2.setFriendRequests(new HashSet<>(Set.of(mockUser3)));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);

        UserProfileDto response = userService.confirm(mockUser3.getId());
        assertFalse(mockUser2.getFriendRequests().contains(mockUser3));
        assertTrue(response.isFriend());
    }

    @Test
    void user_getProfile_when_blocked_throws_entityNotFoundException() {
        mockUser3.setBlocked(new HashSet<>(Set.of(mockUser2)));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);
        long id = mockUser3.getId();
        assertThrows(EntityNotFoundException.class, () -> userService.getUserProfileById(id));
    }


    @Test
    void user_getProfile_when_friends_returnsCorrectDto() {
        mockUser3.setFriends(new HashSet<>(Set.of(mockUser2)));
        mockUser2.setFriends(new HashSet<>(Set.of(mockUser3)));
        mockUser3.setFriendRequests(new HashSet<>());
        mockUser2.setFriendRequests(new HashSet<>());
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);

        UserProfileDto userProfileDto = userService.getUserProfileById(mockUser3.getId());
        assertTrue(userProfileDto.isFriend());
        assertFalse(userProfileDto.isBlocked());
        assertFalse(userProfileDto.isRequested());
        assertFalse(userProfileDto.isRequesting());
    }

    @Test
    void user_getProfile_when_notFriends_returnsCorrectDto() {
        mockUser3.setFriendRequests(new HashSet<>());
        mockUser2.setFriendRequests(new HashSet<>());
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);

        UserProfileDto userProfileDto = userService.getUserProfileById(mockUser3.getId());
        assertFalse(userProfileDto.isFriend());
        assertFalse(userProfileDto.isBlocked());
        assertFalse(userProfileDto.isRequested());
        assertFalse(userProfileDto.isRequesting());
    }

    @Test
    void user_getProfile_when_friendRequestSent_returnsCorrectDto() {
        mockUser3.setFriendRequests(new HashSet<>(Set.of(mockUser2)));
        mockUser2.setFriendRequests(new HashSet<>());
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);

        UserProfileDto userProfileDto = userService.getUserProfileById(mockUser3.getId());
        assertFalse(userProfileDto.isFriend());
        assertFalse(userProfileDto.isBlocked());
        assertTrue(userProfileDto.isRequested());
        assertFalse(userProfileDto.isRequesting());
    }

    @Test
    void user_getProfile_when_friendRequesting_returnsCorrectDto() {
        mockUser3.setFriendRequests(new HashSet<>(Set.of(mockUser2)));
        mockUser2.setFriendRequests(new HashSet<>());
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser2));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser3);

        UserProfileDto userProfileDto = userService.getUserProfileById(mockUser2.getId());
        assertFalse(userProfileDto.isFriend());
        assertFalse(userProfileDto.isBlocked());
        assertFalse(userProfileDto.isRequested());
        assertTrue(userProfileDto.isRequesting());
    }

    @Test
    void user_getProfile_when_blocked_returnsCorrectDto() {
        mockUser3.setFriendRequests(new HashSet<>());
        mockUser2.setFriendRequests(new HashSet<>());
        mockUser2.setBlocked(new HashSet<>(Set.of(mockUser3)));
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(mockUser3));
        when(sessionUtils.getActiveUser()).thenReturn(mockUser2);

        UserProfileDto userProfileDto = userService.getUserProfileById(mockUser3.getId());
        assertFalse(userProfileDto.isFriend());
        assertTrue(userProfileDto.isBlocked());
        assertFalse(userProfileDto.isRequested());
        assertFalse(userProfileDto.isRequesting());
    }


}
