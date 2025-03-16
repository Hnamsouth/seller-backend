package com.vtp.vipo.seller.config.validation.annotation;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.MaxListSizeValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxListSizeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxListSize {
    String message() default "The size of the collection exceeds the maximum allowed.";

    int value(); // Max size allowed

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ErrorCodeResponse errorResponse() default ErrorCodeResponse.INVALID_MAX_SIZE;
}
