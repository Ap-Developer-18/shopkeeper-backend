package com.shopkeeper.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class MobileNumberValidator implements ConstraintValidator<ValidMobileNumber, String> {

    // optional leading '+', then 10-15 digits
    private static final Pattern PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return PATTERN.matcher(value.trim()).matches();
    }
}
