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

public class EntityNotFoundException extends AbstractElovateException {

    public EntityNotFoundException(Message message) {
        super(message);
    }

    @Override
    public HttpStatus getStatusCode() {
        return HttpStatus.NOT_FOUND;
    }
}
