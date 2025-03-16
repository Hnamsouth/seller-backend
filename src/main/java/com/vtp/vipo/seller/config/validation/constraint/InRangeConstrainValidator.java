package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.InRange;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class InRangeConstrainValidator implements ConstraintValidator<InRange, Object> {
    private ErrorCodeResponse errorCodeResponse;
    private BigDecimal min;
    private BigDecimal max;

    @Override
    public void initialize(InRange constraintAnnotation) {
        errorCodeResponse = constraintAnnotation.errorResponse();
        min = ObjectUtils.isEmpty(constraintAnnotation.min()) ? null
                : new BigDecimal(constraintAnnotation.min());
        max = ObjectUtils.isEmpty(constraintAnnotation.max()) ? null :
                new BigDecimal(constraintAnnotation.max());
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t == null) return true;
        if (min == null && max == null) throw new VipoBusinessException(errorCodeResponse);

        try {
            BigDecimal value = new BigDecimal(t.toString());
            if (min != null && max != null) {
                check(value, min, max);
            } else if (min != null) {
                checkMinOnly(value, min);
            } else {
                checkMaxOnly(value, max);
            }
        } catch (VipoBusinessException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    private void check(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }

    private void checkMinOnly(BigDecimal value, BigDecimal min) {
        if (value.compareTo(min) < 0) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }

    private void checkMaxOnly(BigDecimal value, BigDecimal max) {
        if (value.compareTo(max) > 0) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }
}
