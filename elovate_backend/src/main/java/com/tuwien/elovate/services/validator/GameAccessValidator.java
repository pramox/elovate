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

import com.tuwien.elovate.dtos.game.GameAccessDto;
import com.tuwien.elovate.exceptions.archetype.Message;
import com.tuwien.elovate.exceptions.impl.ValidationException;
import com.tuwien.elovate.repositories.game.GameRepository;
import com.tuwien.elovate.repositories.users.UserRepository;
import com.tuwien.elovate.services.session.SessionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GameAccessValidator {

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SessionUtils sessionUtils;

    public void validateGameAccessDto(GameAccessDto gameAccessDTO) throws ValidationException {

        List<Message> exceptions = new ArrayList<>();

        if (gameAccessDTO.getUuid() != null) {
            exceptions.add(Message.GAME_ACCESS_UUID_NOT_EMPTY);
        }

        if (gameAccessDTO.getGame() == null) {
            exceptions.add(Message.GAME_ACCESS_GAME_IS_EMPTY);
        }

        if (gameAccessDTO.getUser() == null) {
            exceptions.add(Message.GAME_ACCESS_USER_IS_EMPTY);
        }

        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }

        if (gameRepository.findById(gameAccessDTO.getGame()).isEmpty()) {
            exceptions.add(Message.GAME_ACCESS_USER_OR_GAME_ID_INCORRECT);
        }

        if (userRepository.findById(gameAccessDTO.getUser()).isEmpty()) {
            exceptions.add(Message.GAME_ACCESS_USER_OR_GAME_ID_INCORRECT);
        }

        if (!sessionUtils.getActiveUser().getId().equals(gameAccessDTO.getUser())) {
            exceptions.add(Message.GAME_ACCESS_REQUESTED_USER_DIFFERS_FROM_SESSION);
        }

        if (!exceptions.isEmpty()) {
            throw new ValidationException(exceptions);
        }
    }
}
