package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.InRange;
import com.vtp.vipo.seller.config.validation.annotation.MaxScale;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Slf4j
public class MaxScaleConstrainValidator implements ConstraintValidator<MaxScale, Object> {
    private ErrorCodeResponse errorCodeResponse;
    private int maxScale;

    @Override
    public void initialize(MaxScale constraintAnnotation) {
        errorCodeResponse = constraintAnnotation.errorResponse();
        maxScale = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t == null) {
            return true; // Consider null values as valid, if null is not allowed use @NotNull annotation
        }
        try {
            BigDecimal valueCol = new BigDecimal(t.toString());
            if (valueCol.stripTrailingZeros().scale() > maxScale) {
                throw new VipoBusinessException(errorCodeResponse);
            }
        } catch (VipoBusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }
}
