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

import com.tuwien.elovate.dtos.gameapplication.GameApplicationRequestDto;
import com.tuwien.elovate.enums.GameApplicationStatus;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.gameapplication.GameApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GameApplicationValidator {

    private final GameApplicationRepository gameApplicationRepository;


    /**
     * Method checks whether a GameApplication name is already in the database and adds an errorMessages
     * if that's the case
     *
     * @param gameApplicationRequestDto GameApplication to be checked
     */
    public void validateGameApplicationRequest(final GameApplicationRequestDto gameApplicationRequestDto) throws ValidationException {
        List<Message> errorMessages = new ArrayList<>();
        if (gameApplicationRepository.getGameApplicationByName(gameApplicationRequestDto.getName()) != null) {
            errorMessages.add(Message.GAME_APPLICATION_NAME_ALREADY_TAKEN);
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException(errorMessages);
        }
    }

    /**
     * Method checks whether the oldStatus can transition to the newStatus and adds errorMessages
     * if that's the case
     *
     * @param oldStatus The old Status of the GameApplication
     * @param newStatus The new status the GameApplication wants to transition to
     * @throws ValidationException If the transition can not be done
     */
    public void validateGameApplicationStatusTransition(final GameApplicationStatus oldStatus,
                                                        final GameApplicationStatus newStatus) throws ValidationException {
        List<Message> errorMessages = new ArrayList<>();
        switch (oldStatus) {
            case PENDING -> {
                if (GameApplicationStatus.PENDING == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_ALREADY_PENDING);
                }
            }
            case REJECTED -> {
                if (GameApplicationStatus.REJECTED == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_ALREADY_REJECTED);
                }
                if (GameApplicationStatus.ACCEPTED == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_CANT_TRANSITION_REJECTED_ACCEPTED);
                }
            }
            case ACCEPTED -> {
                if (GameApplicationStatus.ACCEPTED == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_ALREADY_ACCEPTED);
                }
                if (GameApplicationStatus.PENDING == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_PENDING);
                }
                if (GameApplicationStatus.REJECTED == newStatus) {
                    errorMessages.add(Message.GAME_APPLICATION_CANT_TRANSITION_ACCEPTED_REJECTED);
                }
            }
            default -> errorMessages.add(Message.GAME_APPLICATION_HAS_INVALID_STATUS);
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException(errorMessages);
        }
    }
}
