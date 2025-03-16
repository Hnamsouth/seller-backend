package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.MinLength;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class MinLengthConstrainValidator implements ConstraintValidator<MinLength, Object> {
    private ErrorCodeResponse errorCodeResponse;
    private int value;

    @Override
    public void initialize(MinLength constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        errorCodeResponse = constraintAnnotation.errorResponse();
        value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if (obj == null) return true;
        if ((obj instanceof String && ((String) obj).length() < value)
                || (obj instanceof Number && String.valueOf(obj).length() < value)
        ) {
            throw new VipoBusinessException(errorCodeResponse);
        }
        return true;
    }
}
