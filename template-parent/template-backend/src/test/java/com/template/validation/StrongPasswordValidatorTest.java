package com.template.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the @StrongPassword constraint (min = 8, max = 64).
 * Requirements in StrongPasswordValidator:
 *   - Upper/lower case in Unicode: \\p{Lu} and \\p{Ll}
 *   - Symbol class: [\\p{Punct}\\p{S}]
 *   - Use matcher.find() (not String.matches)
 */
class StrongPasswordValidatorTest {

    private static Validator validator;

    // Minimal DTO to carry the constraint with min=8
    private record PwdDTO(
            @NotBlank
            @StrongPassword(min = 8, max = 64)
            String password
    ) {}

    @BeforeAll
    static void initValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // --- Valid cases ---
    @Test
    void valid_examples_should_pass() {
        List<String> valid = List.of(
                "Cl@278608769",        // length 12, upper, lower, digits, symbol
                "Abcdef1!",           // exact 8
                "Str0ng_P@ss",
                "MIXED123#ok",
                "Passw0rd€Strong",     // Unicode symbol
                "C0mpl3x!tyIsFun",
                "AAaa11!!",            // exact 8 minimalist
                "Longggg123@@@",
                "Éléphant9?",          // Unicode uppercase É
                "Aa1!Aa1!Aa1!",
                "Cho@uihm%289/0070"    // your example
        );

        for (String pwd : valid) {
            var violations = validator.validate(new PwdDTO(pwd));
            assertTrue(violations.isEmpty(), () ->
                    "Should be valid: '" + pwd + "' -> " + violations);
        }
    }

    // --- Invalid cases (excluding null/blank) ---
    @Test
    void invalid_examples_should_fail() {
        List<String> invalid = List.of(
                "Abcdefg1",            // no symbol
                "abcdefg1!",           // no uppercase
                "ABCDEFG!",            // no digit nor lowercase
                "Abcdefgh!",           // no digit
                "A1!bcde",             // 7 -> too short
                "aaaaaaa1!",           // no uppercase
                "AAAAAAA1!",           // no lowercase
                "NoSymbol123",         // no symbol
                "SymbolsOnly!!!!",     // no digit
                "VeryLongPasswordWithAllKindsOfChars123!VeryLongPasswordWithAllKindsOfChars123!" // >64
        );

        for (String pwd : invalid) {
            var violations = validator.validate(new PwdDTO(pwd));
            assertFalse(violations.isEmpty(), () ->
                    "Should be invalid: '" + pwd + "'");
        }
    }

    // --- Null/blank handled separately (because List.of does not accept null) ---
    @Test
    void null_password_should_fail() {
        var violations = validator.validate(new PwdDTO(null));
        assertFalse(violations.isEmpty());
    }

    @Test
    void blank_password_should_fail() {
        var violations = validator.validate(new PwdDTO("   "));
        assertFalse(violations.isEmpty());
    }

    // --- Length boundary tests ---
    @Nested
    class LengthBoundaries {

        @Test
        void min_length_8_should_pass_when_all_requirements_met() {
            var violations = validator.validate(new PwdDTO("Aa1!aaaa")); // 8
            assertTrue(violations.isEmpty(), violations.toString());
        }

        @Test
        void length_7_should_fail() {
            var violations = validator.validate(new PwdDTO("Aa1!aaa")); // 7
            assertFalse(violations.isEmpty());
        }

        @Test
        void max_length_64_should_pass() {
            String base = "Aa1!";
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 60) sb.append("a"); // 4 + 60 = 64
            String pwd = base + sb;
            assertEquals(64, pwd.length());

            var violations = validator.validate(new PwdDTO(pwd));
            assertTrue(violations.isEmpty(), violations.toString());
        }

        @Test
        void length_65_should_fail() {
            String base = "Aa1!";
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 61) sb.append("a"); // 4 + 61 = 65
            String pwd = base + sb;
            assertEquals(65, pwd.length());

            var violations = validator.validate(new PwdDTO(pwd));
            assertFalse(violations.isEmpty());
        }
    }
}