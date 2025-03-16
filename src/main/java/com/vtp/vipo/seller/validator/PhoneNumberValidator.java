package com.vtp.vipo.seller.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator class for validating phone numbers. This class ensures that the phone number follows a valid format:
 * 1. It must be 10 digits long if it starts with '0' (standard Vietnamese phone number).
 * 2. It must be 12 digits long if it starts with '+84' (Vietnam's country code).
 * If the number is blank, a constraint violation message will be added.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    /**
     * Validates a phone number.
     *
     * @param phoneNumber the phone number to validate.
     * @param context the context in which the constraint is evaluated.
     * @return {@code true} if the phone number is valid, {@code false} otherwise.
     */
    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        // Check if phone number is blank
        if (StringUtils.isBlank(phoneNumber)) {
            // If blank, add a custom constraint violation message
            context.buildConstraintViolationWithTemplate("Số điện thoại không được để trống.").addConstraintViolation();
            return false; // Invalid if blank
        }
        String regex = "^(1900|1800)[0-9]{4}$|(05|03|04|07|08|09|024|028|06)[0-9]{8}$|(\\+84|84)[0-9]{9}$|(02839159159\\:)[0-9]{6}$|(\\+84|84)[0-9]{8}$|(\\+84|84)[0-9]{10}$|(021[012345689]|023[23456789]|020[3456789]|022[0123456789]|029[01234679]|025[123456789]|026[01239]|027[01234567])[0-9]{7}$";

        if (phoneNumber.matches(regex)) {
            return true;
        } else {
            context.buildConstraintViolationWithTemplate("Số điện thoại sai định dạng").addConstraintViolation();
            return false;
        }
    }
}