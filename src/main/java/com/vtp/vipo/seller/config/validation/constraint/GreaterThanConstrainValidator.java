package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.GreaterThan;
import com.vtp.vipo.seller.config.validation.annotation.InRange;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class GreaterThanConstrainValidator implements ConstraintValidator<GreaterThan, Object> {
    private ErrorCodeResponse errorCodeResponse;
    private BigDecimal value;

    @Override
    public void initialize(GreaterThan constraintAnnotation) {
        errorCodeResponse = constraintAnnotation.errorResponse();
        value = ObjectUtils.isEmpty(constraintAnnotation.value()) ? null
                : new BigDecimal(constraintAnnotation.value());
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t == null) return true;
        if (value != null) {
            try {
                BigDecimal valueCompare = new BigDecimal(t.toString());
                check(valueCompare,value);
            } catch (VipoBusinessException e) {
                throw e;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void check(BigDecimal valueCompare, BigDecimal value) {
        if (valueCompare.compareTo(value) <= 0) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }
}
