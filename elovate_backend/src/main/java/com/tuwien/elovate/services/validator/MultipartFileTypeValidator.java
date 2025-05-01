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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MultipartFileTypeValidator implements ConstraintValidator<FileType, MultipartFile> {

    private List<String> allowedTypes;


    @Override
    public void initialize(FileType constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        allowedTypes = List.of(constraintAnnotation.fileTypes());
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        return multipartFile == null || allowedTypes.contains(multipartFile.getContentType());
    }
}
