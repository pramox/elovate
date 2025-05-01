package com.tuwien.elovate.services.validator;

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

import com.tuwien.elovate.dtos.user.PasswordResetDto;
import com.tuwien.elovate.dtos.user.RegisterRequestDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final ValidatorUtils validatorUtils;
    private final UserRepository userRepository;

    /**
     * Password has to contain one: lower letter, upper letter, number and special character respectively,
     * be between 8-50 characters
     */
    @RegExp
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.\\\\/-])(?=\\S+$).{8,50}$";

    /**
     * Username has to be alphanumeric and between 4 and 50 characters
     */
    @RegExp
    private static final String USERNAME_REGEX = "^[a-zA-Z0-9]{4,50}$";

    private static final int MAX_FRIENDS = 100;
    private static final int MAX_BLOCKED_USERS = 10;


    public void validateRegisterRequest(final RegisterRequestDto registerRequestDTO)
            throws ValidationException {
        List<Message> errorMessages = new ArrayList<>();
        if (registerRequestDTO.getEmail() == null) {
            errorMessages.add(Message.EMAIL_INVALID);
        }
        if (registerRequestDTO.getNickname() == null) {
            errorMessages.add(Message.USERNAME_INVALID);
        }
        if (registerRequestDTO.getPassword() == null) {
            errorMessages.add(Message.PASSWORD_INVALID);
        }
        if (!validatorUtils.regexMatches(PASSWORD_REGEX, registerRequestDTO.getPassword())) {
            errorMessages.add(Message.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }
        if (!validatorUtils.regexMatches(USERNAME_REGEX, registerRequestDTO.getNickname())) {
            errorMessages.add(Message.USERNAME_DOES_NOT_MEET_REQUIREMENTS);
        }
        if (!validatorUtils.isValidEmail(registerRequestDTO.getEmail())) {
            errorMessages.add(Message.EMAIL_INVALID);
        }
        userAlreadyRegistered(registerRequestDTO.getNickname(), registerRequestDTO.getEmail(), errorMessages);
        if (!errorMessages.isEmpty()) {
            throw new ValidationException(errorMessages);
        }
    }

    /**
     * Method checks whether the email or username has already been registered in the database and adds an errorMessages
     * if that's the case
     *
     * @param username      username of user
     * @param email         email of user
     * @param errorMessages errorMessages so far from the validator
     */
    private void userAlreadyRegistered(final String username, final String email, final List<Message> errorMessages) {
        Optional<User> userOptionalEmail = userRepository.findByEmail(email);
        Optional<User> userOptionalUsername = userRepository.findByNickName(username);
        if (userOptionalEmail.isPresent()) {
            errorMessages.add(Message.EMAIL_ALREADY_TAKEN);
        }
        if (userOptionalUsername.isPresent()) {
            errorMessages.add(Message.USERNAME_ALREADY_TAKEN);
        }
    }

    public void validateUpdate(Long userId, User updatedUser, User sender) {

        boolean senderIsAdmin = sender.isAdmin();

        // if the user that should be updated doesn't exist
        User original = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException(Message.USER_NOT_FOUND));

        // only allowed to update if the sender is the same user as the user he wants to update or the sender is an admin
        boolean allowedToUpdate = sender.getId().equals(updatedUser.getId()) || userRepository.hasRoleById(sender.getId(), Role.ADMIN);
        if (!allowedToUpdate) {
            throw new ValidationException(Message.NO_EDIT_PERMISSION);
        }

        // the updatedUser must not have a different id than the one of the user to update
        boolean updatedUserIsValid = userId.equals(updatedUser.getId());
        if (!updatedUserIsValid) {
            throw new ValidationException(Message.ID_MUST_NOT_BE_CHANGED);
        }

        // only admins may change the roles!
        if (!original.getRoles().equals(updatedUser.getRoles()) && !senderIsAdmin) {
            throw new ValidationException(Message.ROLES_MUST_NOT_BE_CHANGED);
        }

        if (updatedUser.getStatus() == null) {
            throw new ValidationException(Message.MUST_HAVE_USER_STATUS);
        }

        if (updatedUser.getCountryCode() == null) {
            throw new ValidationException(Message.MUST_HAVE_COUNTRY_CODE);
        }

        // validate country code
        if (!validatorUtils.isCountryCodeValid(updatedUser.getCountryCode())) {
            throw new ValidationException(Message.INVALID_COUNTRY_CODE);
        }

        if (updatedUser.getUsername() == null) {
            throw new ValidationException(Message.MUST_HAVE_USER_NAME);
        }

        if (!original.getNickName().equals(updatedUser.getNickName())) {
            if (!validatorUtils.regexMatches(USERNAME_REGEX, updatedUser.getNickName())) {
                throw new ValidationException(Message.USERNAME_DOES_NOT_MEET_REQUIREMENTS);
            }
            if (userRepository.findByNickName(updatedUser.getNickName()).isPresent()) {
                throw new ValidationException(Message.USERNAME_ALREADY_TAKEN);
            }
        }

        if (!original.getEmail().equals(updatedUser.getEmail())) {
            if (!validatorUtils.isValidEmail(updatedUser.getEmail())) {
                throw new ValidationException(Message.EMAIL_INVALID);
            }
            if (userRepository.findByEmail(updatedUser.getEmail()).isPresent()) {
                throw new ValidationException(Message.EMAIL_ALREADY_TAKEN);
            }
        }
    }

    /**
     * Validates the password reset dto. The old password is NOT checked for the regex, since there could be changes in the
     * accepted password regex which could lead to the original passwords not matching it anymore, which would lead to a
     * ValidationException.
     *
     * @param passwordResetDto The dto to validate.
     */
    public void validatePasswordResetDto(PasswordResetDto passwordResetDto) {
        if (!validatorUtils.regexMatches(PASSWORD_REGEX, passwordResetDto.getNewPassword()) || !validatorUtils.regexMatches(PASSWORD_REGEX, passwordResetDto.getNewPasswordRepeated())) {
            throw new ValidationException(Message.PASSWORD_DOES_NOT_MEET_REQUIREMENTS);
        }

        if (!Objects.equals(passwordResetDto.getNewPassword(), passwordResetDto.getNewPasswordRepeated())) {
            throw new ValidationException(Message.PASSWORDS_DO_NOT_MATCH);
        }
    }

    public void validateUsersBefriendable(User sender, User recipient) {
        if (recipient.getBlocked().contains(sender)) {
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }
        if (recipient.getId().equals(sender.getId())) {
            throw new ValidationException(Message.USER_IS_THEMSELVES);
        }
        if (recipient.getFriends().contains(sender)) {
            throw new ValidationException(Message.USER_IS_ALREADY_FRIEND);
        }
        if (recipient.getFriendRequests().contains(sender)) {
            throw new ValidationException(Message.FRIEND_REQUEST_ALREADY_SENT);
        }
        if (recipient.getFriends().size() >= MAX_FRIENDS) {
            throw new ValidationException(Message.RECIPIENT_CANT_HAVE_MORE_FRIENDS);
        }
        if (sender.getFriends().size() >= MAX_FRIENDS) {
            throw new ValidationException(Message.SENDER_CANT_HAVE_MORE_FRIENDS);
        }
    }

    public void validateFriendRequestConfirmable(User sender, User recipient) {
        // Checks if the recipient has a friend request from the sender
        if (!recipient.getFriendRequests().contains(sender)) {
            throw new EntityNotFoundException(Message.FRIEND_REQUEST_NOT_FOUND);
        }
        if (sender.getId().equals(recipient.getId())) {
            recipient.getFriendRequests().remove(sender);
            userRepository.save(recipient);
            throw new ValidationException(Message.USER_IS_THEMSELVES);
        }
        if (recipient.getFriends().size() >= MAX_FRIENDS) {
            recipient.getFriendRequests().remove(sender);
            userRepository.save(recipient);
            throw new ValidationException(Message.RECIPIENT_CANT_HAVE_MORE_FRIENDS);
        }
        if (sender.getFriends().size() >= MAX_FRIENDS) {
            recipient.getFriendRequests().remove(sender);
            userRepository.save(recipient);
            throw new ValidationException(Message.SENDER_CANT_HAVE_MORE_FRIENDS);
        }
        if (sender.getBlocked().contains(recipient)) {
            recipient.getFriendRequests().remove(sender);
            userRepository.save(recipient);
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }
        if (sender.getFriends().contains(recipient)) {
            recipient.getFriendRequests().remove(sender);
            userRepository.save(recipient);
            throw new ValidationException(Message.USER_IS_ALREADY_FRIEND);
        }
    }

    public void validateUsersBlockable(User sender, User recipient) {
        if (sender.getId().equals(recipient.getId())) {
            throw new ValidationException(Message.USER_CANT_BLOCK_THEMSELVES);
        }
        if (sender.getBlocked().size() >= MAX_BLOCKED_USERS) {
            throw new ValidationException(Message.USER_CANT_BLOCK_MORE_USERS);
        }
        if (sender.getBlocked().contains(recipient)) {
            throw new ValidationException(Message.USER_IS_ALREADY_BLOCKED);
        }
    }

    public void validateUsersUnblockable(User sender, User recipient) {
        if (sender.getId().equals(recipient.getId())) {
            throw new ValidationException(Message.USER_CANT_UNBLOCK_THEMSELVES);
        }
        if (!sender.getBlocked().contains(recipient)) {
            throw new ValidationException(Message.USER_IS_NOT_BLOCKED);
        }
    }

    public void validateFriendsRemovable(User remover, User removee) {
        if (removee.getId().equals(remover.getId())) {
            throw new ValidationException(Message.CANT_UNFRIEND_YOURSELF);
        }
        if (!remover.getFriends().contains(removee) || !removee.getFriends().contains(remover)) {
            throw new ValidationException(Message.CANT_UNFRIEND_STRANGER);
        }
    }
}
