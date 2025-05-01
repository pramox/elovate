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

import com.tuwien.elovate.dtos.user.ExtendedUserResponseDto;
import com.tuwien.elovate.entities.users.User;
import com.tuwien.elovate.services.session.SessionUtils;
import com.tuwien.elovate.services.users.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller class for managing admin-related operations.
 * This class provides endpoints for promoting a user to admin and demoting an admin to a regular user.
 * Endpoints are secured with role-based access control to ensure that only authorized admins can perform these operations.
 */
@RestController
@RequestMapping("/api/v1/admins")
@AllArgsConstructor
public class AdminController {

    private final UserService userService;

    private final SessionUtils sessionUtils;
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);


    /**
     * Promotes a user to admin.
     *
     * @param id The ID of the user to promote to admin.
     * @return ResponseEntity containing the ExtendedUserResponseDto for the promoted admin user.
     */
    @PatchMapping("/promote/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ExtendedUserResponseDto> promoteToAdmin(@PathVariable Long id) {
        log.info("PATCH request for /api/v1/admins/promote/{id} received with id: {}", id);
        return new ResponseEntity<>(userService.promoteUserToAdmin(id), HttpStatus.OK);
    }

    /**
     * Demotes an admin to a regular user.
     *
     * @param id The ID of the admin to demote to a regular user.
     * @return ResponseEntity containing the ExtendedUserResponseDto for the demoted user.
     */
    @PatchMapping("/demote/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ExtendedUserResponseDto> demoteToUser(@PathVariable Long id) {
        log.info("PATCH request for /api/v1/admins/demote/{id} received with id: {}", id);
        User sender = sessionUtils.getActiveUser();
        return new ResponseEntity<>(userService.demoteAdminToUser(id, sender), HttpStatus.OK);
    }
}
