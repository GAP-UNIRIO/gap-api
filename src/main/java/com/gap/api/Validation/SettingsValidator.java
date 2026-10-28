package com.gap.api.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public class SettingsValidator implements ConstraintValidator<ValidSettings, Map<String, Object>> {
    private static final int MAX_BYTES = 16 * 1024;
    private static final int MAX_DEPTH = 8;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean isValid(Map<String, Object> value, ConstraintValidatorContext context) {
        if (value == null) return true;
        try {
            return objectMapper.writeValueAsBytes(value).length <= MAX_BYTES && isJsonValue(value, 1);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private boolean isJsonValue(Object value, int depth) {
        if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) {
            return value == null || !(value instanceof Number n) || isFiniteNumber(n);
        }
        if (depth > MAX_DEPTH) return false;
        if (value instanceof Map<?, ?> map) {
            return map.entrySet().stream().allMatch(e -> e.getKey() instanceof String && isJsonValue(e.getValue(), depth + 1));
        }
        if (value instanceof List<?> list) return list.stream().allMatch(item -> isJsonValue(item, depth + 1));
        return false;
    }

    private boolean isFiniteNumber(Number number) {
        if (number instanceof Double d) return Double.isFinite(d);
        if (number instanceof Float f) return Float.isFinite(f);
        return number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long
                || number instanceof BigInteger || number instanceof BigDecimal || number instanceof Double || number instanceof Float;
    }
}
