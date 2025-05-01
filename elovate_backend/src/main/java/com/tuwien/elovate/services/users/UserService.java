package com.tuwien.elovate.services.users;

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

import com.tuwien.elovate.dtos.game.Base64StringDto;
import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.dtos.user.RegisterRequestDto;
import com.tuwien.elovate.dtos.user.UserGameRankingDto;
import com.tuwien.elovate.dtos.user.UserGameRankingPagedDto;
import com.tuwien.elovate.dtos.user.UserProfileDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.dtos.user.UserResponsePagedDto;
import com.tuwien.elovate.entities.game.Game;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.game.GameService;
import com.tuwien.elovate.services.mapper.GameMapper;
import com.tuwien.elovate.services.mapper.UserMapper;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.validator.UserValidator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private UserMapper userMapper;
    private GameMapper gameMapper;
    private final SessionUtils sessionUtils;
    private final GameService gameService;

    public void validateRegisterRequest(final RegisterRequestDto registerRequestDTO)
            throws ValidationException {
        userValidator.validateRegisterRequest(registerRequestDTO);
    }

    /**
     * @return given UserDetailsService for Authentication Manager
     */
    public UserDetailsService userDetailsService() {
        return email ->
                userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        return userMapper.mapUserToUserResponseDto(user);
    }


    @Transactional
    public ExtendedUserResponseDto getExtendedUserById(Long userId, User sender) {
        if (!sender.getRoles().contains(Role.ADMIN) && !sender.getId().equals(userId)) {
            throw new ValidationException(Message.MUST_NOT_ACCESS_OTHER_USER);
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        return userMapper.mapUserToExtendedUserResponseDto(user);
    }

    public ExtendedUserResponseDto promoteUserToAdmin(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));

        if (user.getRoles().contains(Role.ADMIN)) {
            throw new ValidationException(Message.USER_IS_ALREADY_ADMIN);
        }

        user.addRole(Role.ADMIN);
        user = userRepository.save(user);

        return userMapper.mapUserToExtendedUserResponseDto(user);
    }

    @Transactional
    public ExtendedUserResponseDto demoteAdminToUser(Long userId, User sender) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));

        if (!user.getRoles().contains(Role.ADMIN)) {
            throw new ValidationException(Message.USER_IS_NOT_ADMIN);
        }

        if (sender.getId().equals(userId)) {
            throw new ValidationException(Message.ADMIN_CANT_DEMOTE_HIMSELF);
        }

        user.removeRole(Role.ADMIN);
        user = userRepository.save(user);

        return userMapper.mapUserToExtendedUserResponseDto(user);
    }

    @Transactional
    public ExtendedUserResponseDto updateUserById(Long userId, ExtendedUserResponseDto updateDto, User sender) {
        User userToUpdate = userMapper.mapExtendedUserResponseDtoToUser(updateDto);

        userValidator.validateUpdate(userId, userToUpdate, sender);

        User original = userRepository.getReferenceById(userId);

        original.setNickName(updateDto.getNickName());
        original.setEmail(updateDto.getEmail());
        original.setRoles(updateDto.getRoles());
        original.setStatus(updateDto.getStatus());
        original.setCountryCode(updateDto.getCountryCode());
        if (userToUpdate.getImage() != null) {
            original.setImage(userToUpdate.getImage());
        }

        User updated = userRepository.save(original);

        return userMapper.mapUserToExtendedUserResponseDto(updated);
    }

    public Base64StringDto getImageByUserId(final Long userId) {
        try {
            final User user = userRepository.findById(userId).orElseThrow(
                    () -> new EntityNotFoundException(Message.USER_NOT_FOUND)
            );
            if (user.getImage() != null && user.getImage().getBytes().length != 0) {
                return new Base64StringDto(user.getImage().getImageBase64());
            } else {
                throw new EntityNotFoundException(Message.IMAGE_NOT_FOUND);
            }
        } catch (IOException e) {
            throw new EntityNotFoundException(Message.IMAGE_NOT_FOUND);
        }
    }

    public UserResponsePagedDto getUserByNickName(String nickName, boolean blocked, boolean friends, int page, int size) {
        Page<User> users;
        Page<UserResponseDto> response;
        PageRequest pageRequest = PageRequest.of(page, size);

        if (blocked) {
            users = userRepository.findBlockedUsersByNickname(sessionUtils.getActiveUser().getId(), nickName, pageRequest);
        } else if (friends) {
            users = userRepository.findFriendsByNickname(sessionUtils.getActiveUser().getId(), nickName, pageRequest);
        } else {
            users = userRepository.findAllByNicknameExcludingFriendsAndBlocked(nickName, sessionUtils.getActiveUser().getId(), pageRequest);
        }
        if (users.isEmpty()) {
            response = Page.empty();
        } else {
            response = users.map(userMapper::mapUserToUserResponseDto);
        }

        return new UserResponsePagedDto(response);
    }

    public UserResponsePagedDto getAllFriends(int page, int size) {
        Page<User> users = userRepository.findFriends(sessionUtils.getActiveUser().getId(), PageRequest.of(page, size));
        Page<UserResponseDto> response;
        if (users.isEmpty()) {
            response = Page.empty();
        } else {
            response = users.map(userMapper::mapUserToUserResponseDto);
        }

        return new UserResponsePagedDto(response);
    }

    public UserResponsePagedDto getAllBlocked(int page, int size) {
        Page<User> users = userRepository.findBlocked(sessionUtils.getActiveUser().getId(), PageRequest.of(page, size));
        Page<UserResponseDto> response;
        if (users.isEmpty()) {
            response = Page.empty();
        } else {
            response = users.map(userMapper::mapUserToUserResponseDto);
        }

        return new UserResponsePagedDto(response);
    }

    @Transactional
    public UserProfileDto befriend(Long id) {
        User recipient = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        User sender = sessionUtils.getActiveUser();

        this.userValidator.validateUsersBefriendable(sender, recipient);

        recipient.getFriendRequests().add(sender);
        sender.getBlocked().remove(recipient);

        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(recipient);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);

        if (sender.getFriendRequests().contains(recipient)) {
            sender.getFriendRequests().remove(recipient);
            sender.getFriends().add(recipient);
            recipient.getFriends().add(sender);
            sender.getFriendRequests().remove(recipient);
            recipient.getFriendRequests().remove(sender);
            userProfileDto.setFriend(true);
            userRepository.save(sender);
        } else {
            userProfileDto.setRequested(true);
        }
        userRepository.save(recipient);

        return userProfileDto;
    }

    public UserResponsePagedDto getFriendRequests(int page, int size) {
        Page<User> users = userRepository.findFriendRequests(sessionUtils.getActiveUser().getId(), PageRequest.of(page, size));
        Page<UserResponseDto> response;
        if (users.isEmpty()) {
            response = Page.empty();
        } else {
            response = users.map(userMapper::mapUserToUserResponseDto);
        }

        return new UserResponsePagedDto(response);
    }

    @Transactional
    public UserProfileDto confirm(Long id) {
        User sender = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        User recipient = sessionUtils.getActiveUser();

        this.userValidator.validateFriendRequestConfirmable(sender, recipient);

        Set<User> senderFriends = sender.getFriends();
        Set<User> recipientFriends = recipient.getFriends();
        senderFriends.add(recipient);
        recipientFriends.add(sender);
        recipient.getFriendRequests().remove(sender);

        userRepository.save(sender);
        userRepository.save(recipient);
        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(sender);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);
        userProfileDto.setFriend(true);
        return userProfileDto;
    }

    @Transactional
    public UserProfileDto unfriend(Long id) {
        User recipient = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        User sender = sessionUtils.getActiveUser();

        recipient = removeFriend(sender, recipient);

        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(recipient);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);
        userProfileDto.setFriend(false);
        return userProfileDto;
    }

    public UserProfileDto getUserProfileById(Long userId) {
        User current = sessionUtils.getActiveUser();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        if (user.getBlocked().contains(current)) {
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }
        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(user);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);

        if (current.getFriendRequests().contains(user)) {
            userProfileDto.setRequesting(true);
        }
        if (current.getFriends().contains(user)) {
            userProfileDto.setFriend(true);
        }
        if (current.getBlocked().contains(user)) {
            userProfileDto.setBlocked(true);
        }
        if (user.getFriendRequests().contains(current)) {
            userProfileDto.setRequested(true);
        }
        if (Objects.equals(current.getId(), user.getId())) {
            userProfileDto.setSelf(true);
        }

        return userProfileDto;
    }

    @Transactional
    public UserProfileDto block(Long userId) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        User sender = sessionUtils.getActiveUser();

        this.userValidator.validateUsersBlockable(sender, recipient);
        if (sender.getFriends().contains(recipient) && recipient.getFriends().contains(sender)) {
            recipient = this.removeFriend(sender, recipient);
        }
        Set<User> blockedUsers = sender.getBlocked();
        blockedUsers.add(recipient);

        userRepository.save(sender);
        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(recipient);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);
        userProfileDto.setBlocked(true);
        return userProfileDto;
    }

    @Transactional
    public UserProfileDto unblock(Long userId) {
        User recipient = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        User sender = sessionUtils.getActiveUser();

        this.userValidator.validateUsersUnblockable(sender, recipient);
        Set<User> blockedUsers = sender.getBlocked();
        blockedUsers.remove(recipient);

        userRepository.save(sender);
        UserResponseDto userResponseDto = userMapper.mapUserToUserResponseDto(recipient);
        UserProfileDto userProfileDto = new UserProfileDto(userResponseDto);
        userProfileDto.setBlocked(false);
        return userProfileDto;
    }

    @Transactional
    public UserGameRankingPagedDto getAllRankedGames(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));
        Page<Game> games = userRepository.findAllConnectedGames(user.getId(), PageRequest.of(page, size));
        Page<UserGameRankingDto> response;
        if (games.isEmpty()) {
            response = Page.empty();
        } else {
            response = games.map((game) -> {
                UserGameRankingDto gameRankingDto = new UserGameRankingDto();
                gameRankingDto.setGame(gameMapper.gameToGameDto(game));
                gameRankingDto.setUser(userMapper.mapUserToUserResponseDto(user));
                gameRankingDto.setRanking(gameService.findRating(game.getId(), userId).getRating().intValue());
                return gameRankingDto;
            });
        }
        return new UserGameRankingPagedDto(response);
    }

    private User removeFriend(User remover, User removee) {
        this.userValidator.validateFriendsRemovable(remover, removee);

        Set<User> removerFriends = remover.getFriends();
        Set<User> removeeFriends = removee.getFriends();
        removerFriends.remove(removee);
        removeeFriends.remove(remover);
        userRepository.save(remover);
        return userRepository.save(removee);
    }

    /**
     * Must only be used in testing.
     *
     * @param userMapper The mapper to set.
     * @param gameMapper The mapper to set.
     */
    public void setMappers(UserMapper userMapper, GameMapper gameMapper) {
        this.userMapper = userMapper;
        this.gameMapper = gameMapper;
    }
}
