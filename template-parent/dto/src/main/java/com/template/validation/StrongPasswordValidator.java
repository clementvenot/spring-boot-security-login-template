package com.template.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int min, max;
    private boolean requireUpper, requireLower, requireDigit, requireSymbol;

    @Override
    public void initialize(StrongPassword ann) {
        this.min = ann.min();
        this.max = ann.max();
        this.requireUpper = ann.requireUpper();
        this.requireLower = ann.requireLower();
        this.requireDigit = ann.requireDigit();
        this.requireSymbol = ann.requireSymbol();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null) return false;

        int len = value.length();
        if (len < min || len > max) return false;

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (int i = 0; i < value.length();) {
            final int cp = value.codePointAt(i);

            if (Character.isUpperCase(cp)) hasUpper = true;
            if (Character.isLowerCase(cp)) hasLower = true;
            if (Character.isDigit(cp))     hasDigit = true;

            if (!Character.isLetterOrDigit(cp)) {
                switch (Character.getType(cp)) {
                    case Character.MATH_SYMBOL:                 // Sm
                    case Character.CURRENCY_SYMBOL:             // Sc
                    case Character.MODIFIER_SYMBOL:             // Sk
                    case Character.OTHER_SYMBOL:                // So
                    case Character.CONNECTOR_PUNCTUATION:       // Pc
                    case Character.DASH_PUNCTUATION:            // Pd
                    case Character.START_PUNCTUATION:           // Ps
                    case Character.END_PUNCTUATION:             // Pe
                    case Character.INITIAL_QUOTE_PUNCTUATION:   // Pi
                    case Character.FINAL_QUOTE_PUNCTUATION:     // Pf
                    case Character.OTHER_PUNCTUATION:           // Po
                        hasSymbol = true;
                        break;
                    default:
                }
            }

            i += Character.charCount(cp); 
        }

        if (requireUpper  && !hasUpper)  return false;
        if (requireLower  && !hasLower)  return false;
        if (requireDigit  && !hasDigit)  return false;
        if (requireSymbol && !hasSymbol) return false;

        return true;
    }
}
