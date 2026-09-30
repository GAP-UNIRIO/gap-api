package com.gap.api.Service;

import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Model.Entities.Institution;
import com.gap.api.Model.Entities.Institution.AuthProviderType;
import com.gap.api.Model.Entities.Institution.SignatureProviderType;
import com.gap.api.Repository.InstitutionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstitutionServiceTest {

    private static final Map<String, Object> SETTINGS = Map.of("color", "#003366");

    @Mock
    private InstitutionRepository repository;

    @InjectMocks
    private InstitutionService service;

    private Institution institution(Long id, String name, String code, boolean active) {
        Institution i = new Institution();
        i.setId(id);
        i.setName(name);
        i.setCode(code);
        i.setAuthProviderType(AuthProviderType.LOCAL);
        i.setSignatureProvider(SignatureProviderType.NONE);
        i.setSettings(SETTINGS);
        i.setActive(active);
        return i;
    }

    private InstitutionRequest request(String name, String code, Boolean active) {
        return new InstitutionRequest(name, code, AuthProviderType.OAUTH2_CUSTOM,
                SignatureProviderType.GOV_BR, SETTINGS, active);
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve retornar as instituições ordenadas por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                institution(1L, "Inst A", "111", true),
                institution(2L, "Inst B", "222", false)));

        List<InstitutionResponse> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InstitutionResponse::code).containsExactly("111", "222");
        assertThat(result).extracting(InstitutionResponse::active).containsExactly(true, false);
    }

    @Test
    @DisplayName("findAll deve retornar lista vazia quando não há instituições")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById deve retornar a instituição quando existe")
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(institution(1L, "Inst A", "111", true)));

        InstitutionResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Inst A");
        assertThat(result.code()).isEqualTo("111");
    }

    @Test
    @DisplayName("findById deve lançar 404 quando não existe")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- create ----------

    @Test
    @DisplayName("create deve copiar os campos do request e ficar ativa quando active é null")
    void create_defaultsToActive() {
        when(repository.existsByCode("123")).thenReturn(false);
        when(repository.save(any(Institution.class))).thenAnswer(inv -> {
            Institution i = inv.getArgument(0);
            i.setId(7L);
            return i;
        });

        InstitutionResponse result = service.create(request("Nova", "123", null));

        ArgumentCaptor<Institution> captor = ArgumentCaptor.forClass(Institution.class);
        verify(repository).save(captor.capture());
        Institution saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Nova");
        assertThat(saved.getCode()).isEqualTo("123");
        assertThat(saved.getAuthProviderType()).isEqualTo(AuthProviderType.OAUTH2_CUSTOM);
        assertThat(saved.getSignatureProvider()).isEqualTo(SignatureProviderType.GOV_BR);
        assertThat(saved.getSettings()).isEqualTo(SETTINGS);
        assertThat(saved.isActive()).isTrue();
        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.active()).isTrue();
    }

    @Test
    @DisplayName("create deve respeitar active=false")
    void create_respectsInactive() {
        when(repository.existsByCode("123")).thenReturn(false);
        when(repository.save(any(Institution.class))).thenAnswer(inv -> inv.getArgument(0));

        InstitutionResponse result = service.create(request("Nova", "123", false));

        assertThat(result.active()).isFalse();
    }

    @Test
    @DisplayName("create deve lançar 409 quando o código já existe")
    void create_conflict() {
        when(repository.existsByCode("123")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("Nova", "123", null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        verify(repository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve alterar os campos e mudar o código quando ele está livre")
    void update_success() {
        Institution existing = institution(1L, "Antiga", "111", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByCode("999")).thenReturn(false);

        InstitutionResponse result = service.update(1L, request("Renomeada", "999", false));

        assertThat(existing.getName()).isEqualTo("Renomeada");
        assertThat(existing.getCode()).isEqualTo("999");
        assertThat(existing.getAuthProviderType()).isEqualTo(AuthProviderType.OAUTH2_CUSTOM);
        assertThat(existing.getSignatureProvider()).isEqualTo(SignatureProviderType.GOV_BR);
        assertThat(existing.isActive()).isFalse();
        assertThat(result.code()).isEqualTo("999");
    }

    @Test
    @DisplayName("update com active null deve manter o valor atual")
    void update_nullActive_keepsCurrent() {
        Institution existing = institution(1L, "Antiga", "111", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        InstitutionResponse result = service.update(1L, request("Renomeada", "111", null));

        assertThat(existing.isActive()).isFalse();
        assertThat(result.active()).isFalse();
    }

    @Test
    @DisplayName("update com o mesmo código não deve checar duplicidade")
    void update_sameCode_skipsExistsCheck() {
        Institution existing = institution(1L, "Antiga", "111", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.update(1L, request("Renomeada", "111", null));

        verify(repository, never()).existsByCode(any());
    }

    @Test
    @DisplayName("update deve lançar 409 quando o novo código pertence a outra instituição")
    void update_conflict() {
        Institution existing = institution(1L, "Antiga", "111", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByCode("999")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("Renomeada", "999", null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        assertThat(existing.getCode()).isEqualTo("111");
        assertThat(existing.getName()).isEqualTo("Antiga");
    }

    @Test
    @DisplayName("update deve lançar 404 quando a instituição não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("X", "1", null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve remover e dar flush, nessa ordem")
    void delete_success() {
        Institution existing = institution(1L, "Inst", "111", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("delete deve lançar 404 e não remover quando a instituição não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
