package com.template.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password must be 8-64 chars and include upper, lower, digit and symbol.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int min() default 8;
    int max() default 64;
    boolean requireUpper() default true;
    boolean requireLower() default true;
    boolean requireDigit() default true;
    boolean requireSymbol() default true;
}