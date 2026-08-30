package com.shopkeeper.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MobileNumberValidator.class)
@Documented
public @interface ValidMobileNumber {
    String message() default "Invalid mobile number. Must be 10-15 digits, optionally starting with '+'.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
