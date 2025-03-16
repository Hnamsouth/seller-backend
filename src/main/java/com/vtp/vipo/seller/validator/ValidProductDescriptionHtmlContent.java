package com.vtp.vipo.seller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProductDescriptionHtmlContentValidator.class)
public @interface ValidProductDescriptionHtmlContent {

    String message() default "Nội dung chưa hợp lệ";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
