package com.vtp.vipo.seller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for phone number validation.
 * <p>
 * This annotation is used to validate that a phone number is in the correct format,
 * either a standard Vietnamese phone number or one starting with the international prefix "+84".
 * </p>
 *
 * This annotation works with the {@link PhoneNumberValidator} class to perform validation.
 */
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class) // This annotation is linked to the PhoneNumberValidator class
public @interface ValidPhoneNumber {

    /**
     * The error message that will be shown if validation fails.
     *
     * @return the error message string
     */
    String message() default "Số điện thoại sai định dạng"; // Default message in Vietnamese for invalid phone number format

    /**
     * Used to group different validation constraints together.
     * Can be used to apply validation rules in certain scenarios.
     *
     * @return array of validation groups
     */
    Class<?>[] groups() default {};

    /**
     * Used to pass additional data to the validator.
     * Can be used to carry metadata about the validation process.
     *
     * @return array of Payload classes
     */
    Class<? extends Payload>[] payload() default {};
}