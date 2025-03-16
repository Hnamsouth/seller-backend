package com.vtp.vipo.seller.config.validation.annotation;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.EndsWithConstrainValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})  // Dùng trên trường hoặc tham số
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EndsWithConstrainValidator.class)
public @interface EndsWith {
    String message() default "The string must end with one of the specified values.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String[] value();  // Danh sách các đuôi hợp lệ để kiểm tra

    ErrorCodeResponse errorResponse() default ErrorCodeResponse.INVALID_REQUIRED_FIELD;

    boolean ignoreCase() default false;
}
