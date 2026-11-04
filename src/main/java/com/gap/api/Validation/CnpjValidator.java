package com.gap.api.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return true;
        String digits = normalize(value);
        if (!digits.matches("[0-9]{14}") || digits.chars().distinct().count() == 1) return false;
        return checkDigit(digits.substring(0, 12), new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}) == digits.charAt(12) - '0'
                && checkDigit(digits.substring(0, 13), new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}) == digits.charAt(13) - '0';
    }

    public static String normalize(String value) {
        String trimmed = value.trim();
        if (trimmed.matches("[0-9]{14}")) return trimmed;
        if (!trimmed.matches("[0-9]{2}\\.[0-9]{3}\\.[0-9]{3}/[0-9]{4}-[0-9]{2}")) return "";
        return trimmed.replace(".", "").replace("/", "").replace("-", "");
    }

    private int checkDigit(String digits, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) sum += (digits.charAt(i) - '0') * weights[i];
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
