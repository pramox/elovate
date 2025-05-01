package com.tuwien.elovate.services.session;

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

import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.EntityNotFoundException;
import com.tuwien.elovate.repositories.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionUtils {

    private final UserRepository userRepository;

    /**
     * Method queries the active user using the SecurityContext
     *
     * @return User object of the active user
     */
    public User getActiveUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = Optional.empty();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = null;
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                email = userDetails.getUsername();
            } else if (authentication.getPrincipal() instanceof String string) {
                email = string;
            }
            user = userRepository.findByEmail(email);

        }
        if (user.isEmpty()) {
            throw new EntityNotFoundException(Message.USER_NOT_FOUND);
        }
        return user.get();
    }
}
