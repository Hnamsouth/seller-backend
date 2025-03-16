package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.NotNull;

import jakarta.validation.ConstraintValidator;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class NotNullConstrainValidator  implements ConstraintValidator<NotNull, Object> {
    private ErrorCodeResponse errorCodeResponse;

    @Override
    public void initialize(NotNull constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        errorCodeResponse = constraintAnnotation.errorResponse();
    }

    @Override
    public boolean isValid(Object t, ConstraintValidatorContext constraintValidatorContext) {
        if (t == null) {
            // Hiển thị tên trường lỗi
            String fieldName = "";
            var context = (ConstraintValidatorContextImpl) constraintValidatorContext;
            List<ConstraintViolationCreationContext> list = context.getConstraintViolationCreationContexts();
            if (list != null) {
                for (ConstraintViolationCreationContext e : list) {
                    fieldName = e.getPath().getLeafNode().getName();
                }
            }
            String errorCode = errorCodeResponse.getErrorCode();
            String message = errorCodeResponse.getMessage();
            throw new VipoBusinessException(errorCodeResponse, fieldName);
        }
        return true;
    }
}

