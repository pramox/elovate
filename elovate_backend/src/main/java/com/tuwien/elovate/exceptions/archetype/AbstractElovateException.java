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
import org.springframework.http.HttpStatus;

@Getter
public abstract class AbstractElovateException extends RuntimeException {

    private final Message[] messages;

    protected AbstractElovateException(Message... messages) {
        this.messages = messages;
    }

    public abstract HttpStatus getStatusCode();
}
