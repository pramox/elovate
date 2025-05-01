package com.tuwien.elovate.controllers.users;

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

import com.tuwien.elovate.dtos.authentication.JwtAuthenticationResponseDto;
import com.tuwien.elovate.dtos.authentication.LoginRequestDto;
import com.tuwien.elovate.dtos.game.Base64StringDto;
import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.dtos.user.PasswordResetDto;
import com.tuwien.elovate.dtos.user.RegisterRequestDto;
import com.tuwien.elovate.dtos.user.UserGameRankingPagedDto;
import com.tuwien.elovate.dtos.user.UserProfileDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.dtos.user.UserResponsePagedDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.services.authentication.AuthenticationService;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.users.UserService;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller class for managing user-related operations.
 * This class provides endpoints for user registration, login, OAuth login, profile management,
 * friend-related operations, and more. Endpoints are secured with role-based access control
 * to ensure that only authorized users can perform certain operations.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final SessionUtils sessionUtils;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Value("${elovate.hostname}")
    private String hostname;

    /**
     * Registers a new user.
     *
     * @param request The registration request data.
     * @return ResponseEntity containing the JWT authentication response.
     */
    @PermitAll
    @PostMapping("/register")
    public ResponseEntity<JwtAuthenticationResponseDto> register(@RequestBody RegisterRequestDto request) {
        log.info("POST request for /api/v1/users/register received with username: {}, email: {}", request.getNickname(), request.getEmail());
        return new ResponseEntity<>(authenticationService.register(request), HttpStatus.OK);
    }

    /**
     * Logs in a user.
     *
     * @param request The login request data.
     * @return ResponseEntity containing the JWT authentication response.
     */
    @PermitAll
    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponseDto> login(@RequestBody LoginRequestDto request) {
        log.info("POST request for /api/v1/users/login received with username: {}", request.getNickname());
        return new ResponseEntity<>(authenticationService.login(request), HttpStatus.OK);
    }

    /**
     * Endpoint called by Oauth Google Process. Redirects to the corresponding frontend page and
     * adds jwt as query parameter so frontend can handle appropriately.
     *
     * @param code returned by Google after logging in with a Google Account
     * @return Redirect to the login Page with Set query parameter if oauth process was successful.
     */
    @PermitAll
    @GetMapping("/oauth/token")
    public RedirectView loginWithOauth(
            @RequestParam("code") String code) {
        log.info("GET request for /api/v1/users/oauth/token received");
        final String userEmail = authenticationService.getEmailFromOauthLogin(code);
        String redirectUrl = hostname + "/login";
        if (userEmail != null) {
            final JwtAuthenticationResponseDto jwt =
                    authenticationService.createAccountOrLoginWithOauth(userEmail);
            redirectUrl += "?jwt=" + jwt.getToken();
        }
        return new RedirectView(redirectUrl);
    }


    /**
     * Retrieves the basic user information by ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing the UserResponseDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("´GET request for /api/v1/users/{id} received with id: {}", id);
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }

    /**
     * Updates the user information by ID.
     *
     * @param id        The ID of the user.
     * @param updateDto The ExtendedUserResponseDto containing updated information.
     * @return ResponseEntity containing the ExtendedUserResponseDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ExtendedUserResponseDto> updateUserById(@PathVariable Long id, ExtendedUserResponseDto updateDto) {
        log.info("PUT request for /api/v1/users/{id} received with username: {}", id);
        User sender = sessionUtils.getActiveUser();
        return new ResponseEntity<>(userService.updateUserById(id, updateDto, sender), HttpStatus.OK);
    }

    /**
     * Retrieves extended user information by ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing the ExtendedUserResponseDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER', 'ROLE_GAME_DEVELOPER')")
    @GetMapping("/extended/{id}")
    public ResponseEntity<ExtendedUserResponseDto> getExtendedUserById(@PathVariable Long id) {
        log.info("GET request for /api/v1/users/extended/{id} received with username: {}", id);
        User sender = sessionUtils.getActiveUser();
        return new ResponseEntity<>(userService.getExtendedUserById(id, sender), HttpStatus.OK);
    }

    /**
     * Resets the password for the currently authenticated user.
     *
     * @param passwordResetDto The PasswordResetDto containing the new password.
     * @return ResponseEntity containing the UserResponseDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PutMapping("/reset-password")
    public ResponseEntity<UserResponseDto> resetPassword(@RequestBody PasswordResetDto passwordResetDto) {
        User sender = sessionUtils.getActiveUser();
        log.info("PUT request for /api/v1/users/reset-password received with username: {}", sender);
        return new ResponseEntity<>(authenticationService.resetPassword(passwordResetDto, sender), HttpStatus.OK);
    }

    /**
     * Retrieves the user image as Base64-encoded string by user ID.
     *
     * @param userId The ID of the user.
     * @return ResponseEntity containing the Base64StringDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/{userId}/image")
    public ResponseEntity<Base64StringDto> getImageAsBase64ByUserId(@PathVariable Long userId) {
        log.info("GET request for /api/v1/users/:id/image with id: {}", userId);
        return new ResponseEntity<>(userService.getImageByUserId(userId), HttpStatus.OK);
    }

    /**
     * Retrieves a paged list of users based on nickname, blocked, and friends filters.
     *
     * @param nickname The nickname to filter users.
     * @param blocked  Boolean flag to filter blocked users.
     * @param friends  Boolean flag to filter friends.
     * @param page     The page number for pagination.
     * @param size     The number of items per page.
     * @return ResponseEntity containing the UserResponsePagedDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<UserResponsePagedDto> getUsersByNickname(@RequestParam(value = "nickname") String nickname,
                                                                   @RequestParam(value = "blocked", required = false, defaultValue = "false") boolean blocked,
                                                                   @RequestParam(value = "friends", required = false, defaultValue = "false") boolean friends,
                                                                   @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                                   @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        log.info("GET request for /api/v1/users?nickname={}&blocked={}&friends={}&page={}&size={}", nickname, blocked, friends, page, size);
        return new ResponseEntity<>(userService.getUserByNickName(nickname, blocked, friends, page, size), HttpStatus.OK);
    }

    /**
     * Retrieves a paged list of friends.
     *
     * @param page The page number for pagination.
     * @param size The number of items per page.
     * @return ResponseEntity containing the UserResponsePagedDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/friends")
    public ResponseEntity<UserResponsePagedDto> getFriends(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("GET request for /api/v1/users/friends?page={}&size={}", page, size);
        return new ResponseEntity<>(userService.getAllFriends(page, size), HttpStatus.OK);
    }

    /**
     * Retrieves a paged list of blocked users.
     *
     * @param page The page number for pagination.
     * @param size The number of items per page.
     * @return ResponseEntity containing the UserResponsePagedDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/blocked")
    public ResponseEntity<UserResponsePagedDto> getBlocked(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                           @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("GET request for /api/v1/users/blocked?page={}&size={}", page, size);
        return new ResponseEntity<>(userService.getAllBlocked(page, size), HttpStatus.OK);
    }

    /**
     * Retrieves a paged list of friend requests.
     *
     * @param page The page number for pagination.
     * @param size The number of items per page.
     * @return ResponseEntity containing the UserResponsePagedDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/requests")
    public ResponseEntity<UserResponsePagedDto> getFriendRequests(@RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                                  @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("GET request for /api/v1/users/blocked?page={}&size={}", page, size);
        return new ResponseEntity<>(userService.getFriendRequests(page, size), HttpStatus.OK);
    }

    /**
     * Sends a friend request to the specified user.
     *
     * @param id The ID of the user to befriend.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/befriend/{id}")
    public ResponseEntity<UserProfileDto> befriend(@PathVariable("id") Long id) {
        log.info("POST request for /api/v1/users/befriend/{}", id);
        return new ResponseEntity<>(userService.befriend(id), HttpStatus.OK);
    }

    /**
     * Confirms a friend request from the specified user.
     *
     * @param id The ID of the user to confirm the friend request.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/confirm/{id}")
    public ResponseEntity<UserProfileDto> confirm(@PathVariable("id") Long id) {
        log.info("POST request for /api/v1/users/confirm/{}", id);
        return new ResponseEntity<>(userService.confirm(id), HttpStatus.OK);
    }

    /**
     * Unfriends the specified user.
     *
     * @param id The ID of the user to unfriend.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/unfriend/{id}")
    public ResponseEntity<UserProfileDto> unfriend(@PathVariable("id") Long id) {
        log.info("POST request for /api/v1/users/unfriend/{}", id);
        return new ResponseEntity<>(userService.unfriend(id), HttpStatus.OK);
    }

    /**
     * Blocks the specified user.
     *
     * @param id The ID of the user to block.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/block/{id}")
    public ResponseEntity<UserProfileDto> block(@PathVariable("id") Long id) {
        log.info("POST request for /api/v1/users/block/{}", id);
        return new ResponseEntity<>(userService.block(id), HttpStatus.OK);
    }

    /**
     * Unblocks the specified user.
     *
     * @param id The ID of the user to unblock.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @PostMapping("/unblock/{id}")
    public ResponseEntity<UserProfileDto> unblock(@PathVariable("id") Long id) {
        log.info("POST request for /api/v1/users/unblock/{}", id);
        return new ResponseEntity<>(userService.unblock(id), HttpStatus.OK);
    }

    /**
     * Retrieves the user profile by ID.
     *
     * @param id The ID of the user.
     * @return ResponseEntity containing the UserProfileDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserProfileDto> getUserProfileById(@PathVariable Long id) {
        log.info("GET request for /api/v1/users/profile/{id} received with id: {}", id);
        return new ResponseEntity<>(userService.getUserProfileById(id), HttpStatus.OK);
    }

    /**
     * Retrieves a paged list of ranked games for the specified user.
     *
     * @param id   The ID of the user.
     * @param page The page number for pagination.
     * @param size The number of items per page.
     * @return ResponseEntity containing the UserGameRankingPagedDto.
     */
    @PreAuthorize("hasAnyRole('ROLE_GAME_DEVELOPER', 'ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/profile/{id}/games")
    public ResponseEntity<UserGameRankingPagedDto> getAllRankedGames(@PathVariable Long id,
                                                                     @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                                                     @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {
        log.info("GET request for /api/v1/users/profile/{}/games?page={}&size={}", id, page, size);
        return new ResponseEntity<>(userService.getAllRankedGames(id, page, size), HttpStatus.OK);
    }
}
