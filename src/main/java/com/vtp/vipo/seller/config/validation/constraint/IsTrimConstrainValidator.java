package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.IsTrim;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class IsTrimConstrainValidator implements ConstraintValidator<IsTrim, Object> {

    @Override
    public void initialize(IsTrim constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return true; // Null values are considered valid
        }
        if (value instanceof String) {
            return isTrimmed((String) value);
        }
        if (value instanceof Collection) {
            for (Object item : (Collection<?>) value) {
                if (item instanceof String) isTrimmed((String) item);
            }

        }

        if (value instanceof Map) {
            // Trả về kết quả kiểm tra từ allMatch
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                if (entry.getKey() instanceof String) isTrimmed((String) entry.getKey());
                if (entry.getValue() instanceof String) isTrimmed((String) entry.getValue());
            }
        }
        return true;
    }

    private boolean isTrimmed(String str) {
        if (!str.equals(str.trim())) {
            throw new VipoBusinessException(ErrorCodeResponse.INVALID_REQUIRED_TRIM);
        }
        return str.equals(str.trim());
    }
}
