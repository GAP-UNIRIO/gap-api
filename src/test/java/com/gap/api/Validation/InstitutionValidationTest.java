package com.gap.api.Validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class InstitutionValidationTest {
    private final CnpjValidator cnpjValidator = new CnpjValidator();
    private final SettingsValidator settingsValidator = new SettingsValidator();

    @Test
    @DisplayName("@spec:AC-127 rejeita CNPJ com formato ou dígitos verificadores inválidos")
    void cnpjValidator_rejectsInvalidValues() {
        assertThat(cnpjValidator.isValid("123", null)).isFalse();
        assertThat(cnpjValidator.isValid("04.252.011/0001-11", null)).isFalse();
        assertThat(cnpjValidator.isValid("04..252011000110", null)).isFalse();
    }

    @Test
    @DisplayName("@spec:AC-129 aceita configurações JSON opcionais dentro dos limites")
    void settingsValidator_acceptsJsonValues() {
        assertThat(settingsValidator.isValid(null, null)).isTrue();
        assertThat(settingsValidator.isValid(Map.of("features", List.of(true, 12, "texto"), "note", "ok"), null)).isTrue();
        assertThat(settingsValidator.isValid(Map.of("value", "x".repeat(16 * 1024 - 12)), null)).isTrue();
    }

    @Test
    @DisplayName("@spec:AC-130 rejeita configurações grandes, profundas ou com tipos não JSON")
    void settingsValidator_rejectsInvalidSettings() {
        assertThat(settingsValidator.isValid(Map.of("value", "x".repeat(17 * 1024)), null)).isFalse();
        Map<String, Object> nested = new HashMap<>();
        Map<String, Object> current = nested;
        for (int i = 0; i < 9; i++) {
            Map<String, Object> child = new HashMap<>();
            current.put("child", child);
            current = child;
        }
        assertThat(settingsValidator.isValid(nested, null)).isFalse();
        assertThat(settingsValidator.isValid(Map.of("value", new Object()), null)).isFalse();
    }
}
