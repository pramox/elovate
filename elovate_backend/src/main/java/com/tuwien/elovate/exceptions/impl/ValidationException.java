package com.tuwien.elovate.exceptions.impl;

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

import com.tuwien.elovate.exceptions.archetype.AbstractElovateException;
import com.tuwien.elovate.exceptions.archetype.Message;
import org.springframework.http.HttpStatus;

import java.util.List;

public class ValidationException extends AbstractElovateException {

    public ValidationException(Message message) {
        super(message);
    }

    public ValidationException(List<Message> messages) {
        super(messages.toArray(new Message[0]));
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
