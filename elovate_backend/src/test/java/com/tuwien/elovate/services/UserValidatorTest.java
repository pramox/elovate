package com.tuwien.elovate.services;

import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserValidatorTest {

    private User sender;

    private User receiver;

    private User user;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserValidator userValidator;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .nickName("sender")
                .friendRequests(new HashSet<>())
                .friends(new HashSet<>())
                .blocked(new HashSet<>())
                .build();
        receiver = User.builder()
                .id(2L)
                .nickName("receiver")
                .friendRequests(new HashSet<>())
                .friends(new HashSet<>())
                .blocked(new HashSet<>())
                .build();
    }

    @Test
    void user_befriendable_when_blocked_throws_entityNotFoundException() {
        receiver.setBlocked(Set.of(sender));
        assertThrows(EntityNotFoundException.class, () -> userValidator.validateUsersBefriendable(sender, receiver));
    }

    @Test
    void user_befriendable_when_self_throws_ValidationException() {
        receiver.setId(sender.getId());
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBefriendable(sender, receiver)
        );
        assertEquals(Message.USER_IS_THEMSELVES, exception.getMessages()[0]);
    }

    @Test
    void user_befriendable_when_alreadFriend_throws_ValidationException() {
        receiver.setFriends(Set.of(sender));
        sender.setFriends(Set.of(receiver));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBefriendable(sender, receiver)
        );
        assertEquals(Message.USER_IS_ALREADY_FRIEND, exception.getMessages()[0]);
    }

    @Test
    void user_befriendable_when_alreadFriendRequestSent_throws_ValidationException() {
        receiver.setFriendRequests(Set.of(sender));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBefriendable(sender, receiver)
        );
        assertEquals(Message.FRIEND_REQUEST_ALREADY_SENT, exception.getMessages()[0]);
    }

    @Test
    void user_befriendable_when_senderAlreadyMaxFriends_throws_ValidationException() {
        sender.setFriends(buildUserSet(100));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBefriendable(sender, receiver)
        );
        assertEquals(Message.SENDER_CANT_HAVE_MORE_FRIENDS, exception.getMessages()[0]);
    }

    @Test
    void user_befriendable_when_receiverAlreadyMaxFriends_throws_ValidationException() {
        receiver.setFriends(buildUserSet(100));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBefriendable(sender, receiver)
        );
        assertEquals(Message.RECIPIENT_CANT_HAVE_MORE_FRIENDS, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_noFriendRequest_throws_EntityNotFoundException() {
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.FRIEND_REQUEST_NOT_FOUND, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_receiverAlreadyMaxFriends_throws_ValidationException() {
        receiver.setFriendRequests(new HashSet<>(Set.of(sender)));
        receiver.setFriends(buildUserSet(100));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.RECIPIENT_CANT_HAVE_MORE_FRIENDS, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_senderAlreadyMaxFriends_throws_ValidationException() {
        receiver.setFriendRequests(new HashSet<>(Set.of(sender)));
        sender.setFriends(buildUserSet(100));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.SENDER_CANT_HAVE_MORE_FRIENDS, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_blockedBySender_throws_ValidationException() {
        receiver.setFriendRequests(new HashSet<>(Set.of(sender)));
        sender.setBlocked(new HashSet<>(Set.of(receiver)));
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.USER_NOT_FOUND, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_self_throws_ValidationException() {
        sender.setId(receiver.getId());
        receiver.setFriendRequests(new HashSet<>(Set.of(sender)));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.USER_IS_THEMSELVES, exception.getMessages()[0]);
    }

    @Test
    void user_confirmable_when_alreadyFriends_throws_ValidationException() {
        sender.setFriends(new HashSet<>(Set.of(receiver)));
        receiver.setFriends(new HashSet<>(Set.of(sender)));
        receiver.setFriendRequests(new HashSet<>(Set.of(sender)));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendRequestConfirmable(sender, receiver)
        );
        assertEquals(Message.USER_IS_ALREADY_FRIEND, exception.getMessages()[0]);
    }

    @Test
    void user_blockable_when_themself_throws_ValidationExceptions() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBlockable(sender, sender)
        );
        assertEquals(Message.USER_CANT_BLOCK_THEMSELVES, exception.getMessages()[0]);
    }

    @Test
    void user_blockable_when_alreadyMaxBlocked_throws_ValidationExceptions() {
        sender.setBlocked(buildUserSet(10));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBlockable(sender, receiver)
        );
        assertEquals(Message.USER_CANT_BLOCK_MORE_USERS, exception.getMessages()[0]);
    }

    @Test
    void user_blockable_when_receiverAlreadyBlocked_throws_ValidationExceptions() {
        sender.setBlocked(Set.of(receiver));
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersBlockable(sender, receiver)
        );
        assertEquals(Message.USER_IS_ALREADY_BLOCKED, exception.getMessages()[0]);
    }

    @Test
    void user_unblockable_when_themself_throws_ValidationExceptions() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersUnblockable(sender, sender)
        );
        assertEquals(Message.USER_CANT_UNBLOCK_THEMSELVES, exception.getMessages()[0]);
    }

    @Test
    void user_unblockable_when_receiverNotBlocked_throws_ValidationExceptions() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateUsersUnblockable(sender, receiver)
        );
        assertEquals(Message.USER_IS_NOT_BLOCKED, exception.getMessages()[0]);
    }

    @Test
    void user_unfriendable_when_themself_throws_ValidationExceptions() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendsRemovable(sender, sender)
        );
        assertEquals(Message.CANT_UNFRIEND_YOURSELF, exception.getMessages()[0]);
    }

    @Test
    void user_unfriendable_when_noFriendship_throws_ValidationExceptions() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userValidator.validateFriendsRemovable(sender, receiver)
        );
        assertEquals(Message.CANT_UNFRIEND_STRANGER, exception.getMessages()[0]);
    }


    private HashSet<User> buildUserSet(int numUsers) {
        HashSet<User> users = new HashSet<>();
        for (int i = 0; i < numUsers; i++) {
            users.add(User.builder().id((long) i + 10).build());
        }
        return users;
    }
}
