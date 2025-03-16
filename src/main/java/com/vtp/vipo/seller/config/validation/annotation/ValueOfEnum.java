package com.vtp.vipo.seller.config.validation.annotation;

import com.vtp.vipo.seller.config.validation.constraint.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueOfEnum {
    Class<? extends Enum<?>> enumClass();

    String message() default "Value must match one of the enum constants";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
