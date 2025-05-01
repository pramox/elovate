package com.tuwien.elovate.services.authentication;

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

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.tuwien.elovate.dtos.authentication.GoogleOauthAccessTokenDto;
import com.tuwien.elovate.dtos.authentication.GoogleOauthCodeDto;
import com.tuwien.elovate.dtos.authentication.JwtAuthenticationResponseDto;
import com.tuwien.elovate.dtos.authentication.LoginRequestDto;
import com.tuwien.elovate.dtos.user.PasswordResetDto;
import com.tuwien.elovate.dtos.user.RegisterRequestDto;
import com.tuwien.elovate.dtos.user.UserResponseDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.enums.Role;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.InvalidCredentialsException;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.external.ApiService;
import com.tuwien.elovate.services.mapper.UserMapper;
import com.tuwien.elovate.services.users.UserService;
import com.tuwien.elovate.services.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApiService apiService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final UserValidator userValidator;

    @Value("${spring.security.oauth2.client.registration.google.client_id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client_secret}")
    private String clientSecret;

    @Value("${elovate.api}")
    private String hostname;

    public JwtAuthenticationResponseDto register(final RegisterRequestDto request)
            throws ValidationException {
        userService.validateRegisterRequest(request);
        User user = User
                .builder()
                .email(request.getEmail())
                .nickName(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(Role.USER))
                .friends(new HashSet<>())
                .blocked(new HashSet<>())
                .connectedGames(new HashSet<>())
                .comments(new HashSet<>())
                .gameApplications(new HashSet<>())
                .build();
        user = userService.save(user);
        return new JwtAuthenticationResponseDto(jwtService.generateToken(user));
    }

    public JwtAuthenticationResponseDto login(final LoginRequestDto request) throws InvalidCredentialsException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getNickname(), request.getPassword()));
        final User user = userRepository.findByEmail(request.getNickname())
                .orElseThrow(() -> new InvalidCredentialsException(Message.EMAIL_OR_PASSWORD_INVALID));
        return new JwtAuthenticationResponseDto(jwtService.generateToken(user));
    }

    public UserResponseDto resetPassword(PasswordResetDto passwordResetDto, User sender) {

        userValidator.validatePasswordResetDto(passwordResetDto);

        String oldPasswordEncoded = sender.getPassword();

        boolean oldPasswordMatches = passwordEncoder.matches(passwordResetDto.getOldPassword(), oldPasswordEncoded);

        if (!oldPasswordMatches) {
            throw new ValidationException(Message.OLD_PASSWORD_INVALID);
        }

        String newPasswordEncoded = passwordEncoder.encode(passwordResetDto.getNewPassword());
        sender.setPassword(newPasswordEncoded);
        userService.save(sender);

        return userService.getUserById(sender.getId());
    }

    public String getEmailFromOauthLogin(final String code) {
        final String idToken = requestGoogleIdToken(code);
        if (idToken == null || idToken.isEmpty()) {
            return null;
        }
        try {
            final GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken != null) {
                Payload payload = googleIdToken.getPayload();
                return payload.getEmail();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public JwtAuthenticationResponseDto createAccountOrLoginWithOauth(final String email)
            throws ValidationException {
        final Optional<User> userOptional = userRepository.findByEmail(email);
        return userOptional.map(user -> new JwtAuthenticationResponseDto(
                jwtService.generateToken(user))).orElseGet(() -> register(new RegisterRequestDto(
                email,
                UUID.randomUUID().toString().replace("-", ""),
                UUID.randomUUID() + "#1Aa"
        )));
    }

    private String requestGoogleIdToken(final String code) {
        final String url = "https://oauth2.googleapis.com/token";
        final GoogleOauthCodeDto googleOauthCodeDTO = new GoogleOauthCodeDto(
                clientId,
                clientSecret,
                code,
                "authorization_code",
                hostname + "/api/v1/users/oauth/token");
        final String googleOauthCodeJson;
        try {
            googleOauthCodeJson = apiService.sendPostRequest(url, googleOauthCodeDTO);
            final GoogleOauthAccessTokenDto googleOauthAccessTokenDTO
                    = apiService.mapJsonToEntity(googleOauthCodeJson, GoogleOauthAccessTokenDto.class);
            return googleOauthAccessTokenDTO.getId_token();
        } catch (Exception e) {
            return null;
        }
    }
}
