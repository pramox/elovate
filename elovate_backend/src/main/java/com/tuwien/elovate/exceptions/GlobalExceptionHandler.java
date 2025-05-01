package com.tuwien.elovate.exceptions;

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
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String ERROR_NAME_KEY = "errorKey";
    private static final String ERROR_MESSAGE_KEY = "message";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NotNull HttpHeaders headers,
                                                                  @NotNull HttpStatusCode status,
                                                                  @NotNull WebRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessageString = error.getDefaultMessage();
            try {
                Message errorMessage = Message.valueOf(errorMessageString);
                Map<String, String> response = new HashMap<>();
                response.put(ERROR_NAME_KEY, errorMessage.name());
                response.put(ERROR_MESSAGE_KEY, errorMessage.getMessage());
                errors.add(response);
            } catch (IllegalArgumentException e) {
                log.warn("Message not defined for: {}", errorMessageString);
                Map<String, String> response = new HashMap<>();
                response.put(ERROR_NAME_KEY, errorMessageString);
                response.put(ERROR_MESSAGE_KEY, errorMessageString);
                errors.add(response);
            }
        });
        return new ResponseEntity<>(errors, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(AbstractElovateException.class)
    public ResponseEntity<List<Map<String, String>>> handleElovateException(AbstractElovateException ex) {

        List<Map<String, String>> errors = new ArrayList<>();
        for (Message error : ex.getMessages()) {
            Map<String, String> response = new HashMap<>();
            response.put(ERROR_NAME_KEY, error.name());
            response.put(ERROR_MESSAGE_KEY, error.getMessage());
            errors.add(response);
            log.warn(error.getMessage());
        }
        HttpStatus status = ex.getStatusCode();
        return new ResponseEntity<>(errors, status);
    }

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<List<Map<String, String>>> handleFileSizeException(FileSizeLimitExceededException ignoredEx) {
        List<Map<String, String>> errors = new ArrayList<>();
        Map<String, String> response = new HashMap<>();
        response.put(ERROR_NAME_KEY, Message.FILE_SIZE_LIMIT_EXCEEDED.name());
        response.put(ERROR_MESSAGE_KEY, Message.FILE_SIZE_LIMIT_EXCEEDED.getMessage());
        errors.add(response);
        HttpStatus status = HttpStatus.PAYLOAD_TOO_LARGE;
        return new ResponseEntity<>(errors, status);
    }
}
