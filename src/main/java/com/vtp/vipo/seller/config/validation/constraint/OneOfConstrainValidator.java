package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.OneOf;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class OneOfConstrainValidator implements ConstraintValidator<OneOf, Object> {
    private ErrorCodeResponse errorCodeResponse;

    private String[] collection;

    @Override
    public void initialize(OneOf constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        errorCodeResponse = constraintAnnotation.errorResponse();
        collection = constraintAnnotation.collection();
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t != null) {
            String s = null;
            if (t instanceof Number) {
                s = t.toString();
            }
            if (t instanceof String) {
                s = (String) t;
            }
            if (!isExist(s, collection)) {
                throw new VipoBusinessException(errorCodeResponse);
            }
        }
        return true;
    }

    public boolean isExist(String s, String[] ss) {
        for (String str : ss) {
            if (Objects.equals(s, str)) {
                return true;
            }
        }
        return false;
    }
}
