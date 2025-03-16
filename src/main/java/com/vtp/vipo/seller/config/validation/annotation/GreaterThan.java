package com.vtp.vipo.seller.config.validation.annotation;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.GreaterThanConstrainValidator;
import com.vtp.vipo.seller.config.validation.constraint.InRangeConstrainValidator;

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
@Constraint(validatedBy = GreaterThanConstrainValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GreaterThan {
    // error message
    String message() default "";

    // represents group of constraints
    Class<?>[] groups() default {};

    // represents additional information about annotation
    Class<? extends Payload>[] payload() default {};

    String value();

    ErrorCodeResponse errorResponse();
}
