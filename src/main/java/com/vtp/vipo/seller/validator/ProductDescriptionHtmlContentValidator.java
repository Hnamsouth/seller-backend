package com.vtp.vipo.seller.validator;

import com.vtp.vipo.seller.common.exception.ErrorCodeResponse;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductDescriptionHtmlContentValidator
        implements ConstraintValidator<ValidProductDescriptionHtmlContent, String> {

    @Value("${custom.product.description.min-length:50}")
    private int minLength;

    @Value("${custom.product.description.max-length:5000}")
    private int maxLength;

    @Override
    public boolean isValid(String productDescription, ConstraintValidatorContext constraintValidatorContext) {
        constraintValidatorContext.disableDefaultConstraintViolation();
        if (StringUtils.isBlank(productDescription)) {
            modifyConstraintMessage(constraintValidatorContext, "{invalid.product.description.notBlank}");
            return false;
        }
        if (!productDescription.equals(productDescription.trim())) {
            modifyConstraintMessage(constraintValidatorContext, "{invalid.product.description.notTrim}");
            return false;
        }
        String plainText = Jsoup.parse(productDescription).text();
        // Check the minimum length using injected value
        if (plainText.length() < minLength) {
            modifyConstraintMessage(
                    constraintValidatorContext, "{" + ErrorCodeResponse.INVALID_MIN_50_CHAR.getMessage()+ "}"
            );
            return false;
        }
        // Check the maximum length using injected value
        if (plainText.length() > maxLength) {
            modifyConstraintMessage(
                    constraintValidatorContext, "{" + ErrorCodeResponse.INVALID_MAX_5000_CHAR.getMessage() + "}"
            );
            return false;
        }

        return true;
    }

    private void modifyConstraintMessage(ConstraintValidatorContext constraintValidatorContext, String message) {
        constraintValidatorContext.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

}
