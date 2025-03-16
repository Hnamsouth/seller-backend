package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.GreaterThan;
import com.vtp.vipo.seller.config.validation.annotation.IsInteger;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class IsIntegerValidator implements ConstraintValidator<IsInteger, Object> {
    @Override
    public void initialize(IsInteger constraintAnnotation) {
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Consider null values as valid, if null is not allowed use @NotNull annotation
        }
        try {
            BigDecimal valueCol = new BigDecimal(value.toString());
            if (valueCol.stripTrailingZeros().scale() > 0) {
                throw new VipoBusinessException(ErrorCodeResponse.INVALID_INTEGER);
            }
        }catch (VipoBusinessException e){
            throw e;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
