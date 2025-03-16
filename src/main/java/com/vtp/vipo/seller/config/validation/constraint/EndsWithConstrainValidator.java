package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.EndsWith;
import com.vtp.vipo.seller.config.validation.annotation.GreaterThan;
import org.apache.commons.lang3.ObjectUtils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Author: hieuhm12
 * Date: 9/12/2024
 */
public class EndsWithConstrainValidator implements ConstraintValidator<EndsWith, Object> {
    private String[] suffixes;
    private ErrorCodeResponse errorCodeResponse;
    private boolean ignoreCase;

    @Override
    public void initialize(EndsWith constraintAnnotation) {
        errorCodeResponse = constraintAnnotation.errorResponse();
        suffixes = constraintAnnotation.value();
        this.ignoreCase = constraintAnnotation.ignoreCase();
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t == null) return true;
        if (t instanceof String) {
            check((String) t);
        }
        if (t instanceof List) {
            List<?> list = (List<?>) t;

            for (Object item : list) {
                if (item instanceof String) {
                    check((String) item);
                }
            }
            return true;
        }
        return true;
    }

    private void check(String input) {
        if (ignoreCase) {
            input = input.toLowerCase();
        }
        // Kiểm tra nếu value kết thúc bằng bất kỳ hậu tố nào trong mảng suffixes
        String finalValue = input;
        boolean endsWithValidSuffix = Arrays.stream(suffixes)
                .anyMatch(suffix -> finalValue.endsWith(ignoreCase ? suffix.toLowerCase() : suffix));
        if (!endsWithValidSuffix) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }
}
