package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import com.vtp.vipo.seller.common.exception.VipoBusinessException;
import com.vtp.vipo.seller.config.validation.annotation.Regex;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * Author: hieuhm12
 * Date: 9/11/2024
 */
public class RegexConstrainValidator implements ConstraintValidator<Regex, String> {
    private String pattern;

    private ErrorCodeResponse errorCodeResponse;

    @Override
    public void initialize(Regex constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        pattern = constraintAnnotation.pattern();
        errorCodeResponse = constraintAnnotation.errorResponse();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return true;
        }

        if (pattern != null) {
            try {
                check(pattern, s);
            } catch (VipoBusinessException e) {
                throw e;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private void check(String pattern, String s) {
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(s);
        if (!matcher.matches()) {
            throw new VipoBusinessException(errorCodeResponse);
        }
    }
}
