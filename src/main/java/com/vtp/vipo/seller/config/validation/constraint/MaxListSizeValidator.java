package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.GreaterThan;
import com.vtp.vipo.seller.config.validation.annotation.MaxListSize;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Collection;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class MaxListSizeValidator implements ConstraintValidator<MaxListSize, Collection<?>> {
    private ErrorCodeResponse errorCodeResponse;
    private int value;

    @Override
    public void initialize(MaxListSize constraintAnnotation) {
        errorCodeResponse = constraintAnnotation.errorResponse();
        value = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Collection<?> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (ObjectUtils.isEmpty(collection)) return true;
        try {
            if (collection.size() > value) {
                throw new VipoBusinessException(errorCodeResponse, String.valueOf(value));
            }
        } catch (VipoBusinessException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
