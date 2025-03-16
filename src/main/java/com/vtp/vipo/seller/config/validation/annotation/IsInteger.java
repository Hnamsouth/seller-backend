package com.vtp.vipo.seller.config.validation.annotation;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.IsIntegerValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
@Documented
@Constraint(validatedBy = IsIntegerValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsInteger {
    String message() default "The value must be an integer.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
