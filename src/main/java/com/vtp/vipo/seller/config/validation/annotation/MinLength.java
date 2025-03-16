package com.vtp.vipo.seller.config.validation.annotation;
import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.MinLengthConstrainValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */

@Constraint(validatedBy = MinLengthConstrainValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MinLength {
    // error message
    String message() default "";

    // represents group of constraints
    Class<?>[] groups() default {};

    // represents additional information about annotation
    Class<? extends Payload>[] payload() default {};

    int value();

    ErrorCodeResponse errorResponse();
}
