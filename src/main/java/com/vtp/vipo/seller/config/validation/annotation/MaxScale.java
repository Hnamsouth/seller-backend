package com.vtp.vipo.seller.config.validation.annotation;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.config.validation.constraint.MaxScaleConstrainValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxScaleConstrainValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxScale {
    String message() default "The value exceeds the maximum allowed scale.";

    int value(); // Max scale allowed

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    ErrorCodeResponse errorResponse();
}
