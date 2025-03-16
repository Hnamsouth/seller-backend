package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.MaxLength;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Slf4j
public class MaxLengthConstrainValidator implements ConstraintValidator<MaxLength, Object> {
    private ErrorCodeResponse errorCodeResponse;
    private int max;

    @Override
    public void initialize(MaxLength constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        errorCodeResponse = constraintAnnotation.errorResponse();
        max = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext constraintValidatorContext) {
        if (obj == null) {
            return true;
        }
            try {
                check(obj, max);
            } catch (VipoBusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage());
                return false;
            }
        return true;
    }

    private void check(Object obj, int max) {
        if ((obj instanceof String && ((String) obj).length() > max)
                || (obj instanceof Number && String.valueOf(obj).length() > max)
        ) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }
}
