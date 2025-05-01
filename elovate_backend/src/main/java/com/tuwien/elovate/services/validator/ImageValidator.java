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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private long maxSize;

    private float allowedRatio;

    private float percentageTolerance;

    private List<String> fileTypes;


    @Override
    public void initialize(ValidImage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        maxSize = constraintAnnotation.maxSize();
        percentageTolerance = constraintAnnotation.percentageTolerance();
        fileTypes = Arrays.stream(constraintAnnotation.fileTypes()).toList();
        String[] ratio = constraintAnnotation.allowedRatio().split(":");
        allowedRatio = Float.parseFloat(ratio[0]) / Float.parseFloat(ratio[1]);
        percentageTolerance = constraintAnnotation.percentageTolerance();
    }

    @Override
    public boolean isValid(MultipartFile multipartFile, ConstraintValidatorContext constraintValidatorContext) {
        if (multipartFile == null) {
            return true;
        }

        // Order of the following Checks is important

        if (maxSize < multipartFile.getSize() && multipartFile.getSize() > 0) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("FILE_SIZE_LIMIT_EXCEEDED").addConstraintViolation();
            return false;
        }

        if (!fileTypes.contains(multipartFile.getContentType())) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("FILE_EXTENSION_WRONG").addConstraintViolation();
            return false;
        }

        if (!isRatioValid(multipartFile)) {
            constraintValidatorContext.buildConstraintViolationWithTemplate("FILE_ASPECT_RATIO_WRONG").addConstraintViolation();
            return false;
        }

        return true;

    }

    private boolean isRatioValid(MultipartFile multipartFile) {
        try {
            BufferedImage image = ImageIO.read(multipartFile.getInputStream());
            float ratio = (float) image.getWidth() / (float) image.getHeight();
            return Math.abs(1 - ratio / allowedRatio) * 100 <= percentageTolerance;

        } catch (IOException e) {
            return false;
        }
    }
}
