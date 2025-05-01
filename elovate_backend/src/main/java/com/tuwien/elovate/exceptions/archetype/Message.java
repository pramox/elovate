package com.tuwien.elovate.exceptions.archetype;

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

import lombok.Getter;

@Getter
public enum Message {

    // User-related exceptions
    USER_NOT_FOUND("User not found"),
    USER_IS_ALREADY_ADMIN("User is already an admin"),
    USER_IS_NOT_ADMIN("User is not an admin"),
    NO_EDIT_PERMISSION("No edit permission"),
    ID_MUST_NOT_BE_CHANGED("ID must not be changed"),
    ROLES_MUST_NOT_BE_CHANGED("Roles must not be changed"),
    MUST_HAVE_USER_STATUS("Must have user status"),
    MUST_HAVE_COUNTRY_CODE("Must have country code"),
    INVALID_COUNTRY_CODE("Invalid country code"),
    MUST_HAVE_USER_NAME("Must have user name"),
    USERNAME_DOES_NOT_MEET_REQUIREMENTS("Username does not meet requirements"),
    USERNAME_ALREADY_TAKEN("Username already taken"),
    EMAIL_ALREADY_TAKEN("Email already taken"),
    EMAIL_INVALID("Email invalid"),
    USERNAME_INVALID("Username invalid"),
    PASSWORD_INVALID("Password invalid"),
    PASSWORDS_DO_NOT_MATCH("Passwords have to match"),
    OLD_PASSWORD_INVALID("Your old password is incorrect"),
    PASSWORD_DOES_NOT_MEET_REQUIREMENTS("Password does not meet requirements"),
    EMAIL_OR_PASSWORD_INVALID("Email or password is invalid"),
    ADMIN_CANT_DEMOTE_HIMSELF("You can't demote yourself"),
    MUST_NOT_ACCESS_OTHER_USER("You must not access this user"),
    UUID_NOT_VALID("UUID is invalid"),

    // Game-related exceptions
    GAME_NOT_FOUND("Game not found"),
    NO_GAME_RESULT("Game has to be decided: WIN/DRAW/LOSS"),
    DRAW_IS_NOT_ALLOWED("Draw is not allowed for this game!"),

    // API key-related exceptions
    NOT_AUTHORIZED_TO_CREATE_API_KEY("You are not authorized to create an API key"),
    NOT_AUTHORIZED_TO_DELETE_API_KEY("You are not authorized to delete this API key"),
    NO_API_KEY_FOUND("No API key found"),
    NOT_AUTHORIZED_TO_RETRIEVE_API_KEY("You are not authorized to retrieve this API key"),
    API_KEY_IS_INVALID("API key is invalid"),

    // Game application-related exceptions
    GAME_APPLICATION_CONTRACT_NOT_FOUND("Game application contract not found"),
    GAME_APPLICATION_NOT_FOUND("Game application not found"),
    GAME_APPLICATION_NAME_ALREADY_TAKEN("Game application name already taken"),
    GAME_APPLICATION_ALREADY_PENDING("Game application already pending"),
    GAME_APPLICATION_ALREADY_REJECTED("Game application already rejected"),
    GAME_APPLICATION_ALREADY_ACCEPTED("Game application already accepted"),
    GAME_APPLICATION_HAS_INVALID_STATUS("Game application has an invalid status"),
    GAME_APPLICATION_CANT_TRANSITION_REJECTED_ACCEPTED("Game application cannot transition from rejected to accepted"),
    GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_PENDING("Game application cannot transition from accepted to pending"),
    GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_REJECTED("Game application cannot transition from accepted to rejected"),
    PLAYERS_PER_TEAM_IS_NULL("Players per team must be set"),
    DRAW_POSSIBLE_IS_NULL("Game draw possibility setting is required."),
    GAME_GENRE_IS_NULL("Game genre is required."),
    GAME_NAME_INVALID("Game name is required with a maximum length of 255"),
    PLAYER_PER_TEAM_INVALID("Players per team is invalid."),

    // Use this for the tests, when the ID is irrelevant
    MOCK_STATUS_ID("Mock status id"),

    // Game Access related exceptions
    GAME_ACCESS_USER_OR_GAME_ID_INCORRECT("You are not unlocked for this game."),
    GAME_ACCESS_NOT_FOUND("UUID was invalid"),
    GAME_ACCESS_GAME_ACCESS_CODE_INCORRECT("Game access code is incorrect"),
    GAME_ACCESS_UUID_NOT_EMPTY("UUID must be empty"),
    GAME_ACCESS_GAME_IS_EMPTY("Game is empty"),
    GAME_ACCESS_USER_IS_EMPTY("User is empty"),
    GAME_ACCESS_REQUESTED_USER_DIFFERS_FROM_SESSION("User ID differs from logged in user"),
    GAME_ACCESS_USER_ID_DOES_NOT_MATCH_SESSION("User ID differs from logged in user"),

    // Lobby related exceptions
    LOBBY_NOT_FOUND("Lobby id is incorrect or could not be found"),
    TEAM_SIZES_DIFFERENT("Team sizes must be the same size"),

    // Image related exceptions
    IMAGE_COULD_NOT_BE_SAVED("Image could not be saved"),
    IMAGE_NOT_FOUND("Image could not be found"),

    // For the websockets
    JWT_TOKEN_INVALID("JWT Token invalid"),
    NO_JWT_TOKEN_IN_HEADER("No JWT token in header"),

    // Queue
    USER_ALREADY_IN_QUEUE("User already in queue"),
    INVALID_QUEUE_ACTION("Invalid queue message"),

    // Files
    FILE_SIZE_LIMIT_EXCEEDED("File size limit of 2MB exceeded"),
    FILE_EXTENSION_WRONG("Image must be of type png or jpeg."),
    FILE_ASPECT_RATIO_WRONG("Image aspect ratio must be 3:4"),
    FILE_INVALID("File is invalid"),

    // Pageable
    SIZE_MUST_BE_SET("Size must be set"),
    SIZE_MUST_BE_AT_LEAST_ONE("Size must be at least one"),
    PAGE_MUST_BE_SET("Page must be set"),
    PAGE_MUST_NOT_BE_NEGATIVE("Page must not be negative"),

    // Unknown server error
    SERVER_ERROR("Unknown server error."),

    // Rating
    INVALID_DEFAULT_RATING("Default rating must be between 1000 and 2000"),
    INVALID_DEFAULT_RATING_DEVIATION("Default rating deviation must be between 50.0 and 500.0"),
    INVALID_DEFAULT_RATING_VOLATILITY("Default rating volatility must be between 0.02 and 0.1"),
    INVALID_TAU("Tau must be between 0.2 and 1.2"),
    INVALID_RATING_PERIOD("Rating period must be between 7 and 30 days"),
    NOT_AUTHORIZED_TO_FETCH_RATING_PARAMETERS("You are not allowed to fetch rating parameters for this game."),
    INVALID_MIN_RATING("Min rating must be between 0 and 1000"),
    INVALID_MAX_RATING("Max rating must be between 2000 and 6000"),
    GAME_HAS_DIFFERENT_RATING_TYPE("Game has different rating type."),

    // Friend errors
    SENDER_CANT_HAVE_MORE_FRIENDS("Sender can not have more friends."),
    RECIPIENT_CANT_HAVE_MORE_FRIENDS("Recipient can not have more friends."),
    USER_IS_THEMSELVES("User can not be friends with themselves."),
    USER_IS_ALREADY_FRIEND("User is already a friend."),
    FRIEND_REQUEST_ALREADY_SENT("Friend request already sent."),
    FRIEND_REQUEST_NOT_FOUND("Friend request not found."),
    CANT_UNFRIEND_YOURSELF("User can not unfriend themself."),
    CANT_UNFRIEND_STRANGER("User can not unfriend a stranger."),

    // Block errors
    USER_CANT_BLOCK_MORE_USERS("User can not block more users."),
    USER_CANT_BLOCK_THEMSELVES("User can not block themselves."),
    USER_IS_ALREADY_BLOCKED("User is already blocked."),
    USER_CANT_UNBLOCK_THEMSELVES("User can not unblock themselves."),
    USER_IS_NOT_BLOCKED("User was not blocked."),

    // Statistics
    STATISTICS_INVALID_USER_LOGGED_IN("User ID does not match the logged in User"),
    INVALID_LEADERBOARD_COUNT("Leaderboard count may not be smaller than 0");

    private final String message;

    Message(String message) {
        this.message = message;
    }
}
