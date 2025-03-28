package com.vtp.vipo.seller.config.validation.constraint;

import com.vtp.vipo.seller.config.validation.annotation.ValueOfEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<ValueOfEnum, String> {

    private Class<? extends Enum<?>> enumClass;

    @Override
    public void initialize(ValueOfEnum constraintAnnotation) {
        this.enumClass = constraintAnnotation.enumClass();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Giá trị null sẽ không bị validate (dùng @NotNull nếu cần kiểm tra null)
        }

        return Arrays.stream(enumClass.getEnumConstants())
                .anyMatch(e -> e.name().equals(value));
    }
}

