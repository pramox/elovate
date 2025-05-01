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

public class MultipartFileSizeValidator implements ConstraintValidator<MaxFileSize, MultipartFile> {

    private long maxSize;


    @Override
    public void initialize(MaxFileSize constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        maxSize = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        return multipartFile == null || maxSize >= multipartFile.getSize();
    }
}
