package com.gap.api.Service;

import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Model.Entities.OrgUnit;
import com.gap.api.Model.Entities.OrgUnit.OrgUnitType;
import com.gap.api.Repository.OrgUnitRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgUnitServiceTest {

    @Mock
    private OrgUnitRepository repository;

    @InjectMocks
    private OrgUnitService service;

    private OrgUnit unit(Long id, OrgUnitType type, String name) {
        OrgUnit u = new OrgUnit();
        u.setId(id);
        u.setType(type);
        u.setName(name);
        return u;
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    @Test
    @DisplayName("findAll deve retornar as unidades ordenadas por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                unit(1L, OrgUnitType.REITORIA, "Reitoria"),
                unit(2L, OrgUnitType.ESCOLA, "Escola de TI")));

        List<OrgUnitResponse> result = service.findAll();

        assertThat(result).containsExactly(
                new OrgUnitResponse(1L, OrgUnitType.REITORIA, "Reitoria"),
                new OrgUnitResponse(2L, OrgUnitType.ESCOLA, "Escola de TI"));
    }

    @Test
    @DisplayName("findAll deve retornar lista vazia quando não há unidades")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById deve retornar a unidade quando existe")
    void findById_found() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(unit(1L, OrgUnitType.COORDENACAO, "Coordenação")));

        OrgUnitResponse result = service.findById(1L);

        assertThat(result).isEqualTo(new OrgUnitResponse(1L, OrgUnitType.COORDENACAO, "Coordenação"));
    }

    @Test
    @DisplayName("findById deve lançar 404 quando não existe")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("create deve salvar a unidade com tipo e nome do request")
    void create_success() {
        when(repository.save(any(OrgUnit.class))).thenAnswer(inv -> {
            OrgUnit u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        OrgUnitResponse result = service.create(new OrgUnitRequest(OrgUnitType.SECRETARIA, "Secretaria"));

        ArgumentCaptor<OrgUnit> captor = ArgumentCaptor.forClass(OrgUnit.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(OrgUnitType.SECRETARIA);
        assertThat(captor.getValue().getName()).isEqualTo("Secretaria");
        assertThat(result).isEqualTo(new OrgUnitResponse(5L, OrgUnitType.SECRETARIA, "Secretaria"));
    }

    @Test
    @DisplayName("update deve alterar tipo e nome da unidade existente")
    void update_success() {
        OrgUnit existing = unit(1L, OrgUnitType.ESCOLA, "Antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        OrgUnitResponse result = service.update(1L, new OrgUnitRequest(OrgUnitType.DEPARTAMENTO, "Novo"));

        assertThat(existing.getType()).isEqualTo(OrgUnitType.DEPARTAMENTO);
        assertThat(existing.getName()).isEqualTo("Novo");
        assertThat(result).isEqualTo(new OrgUnitResponse(1L, OrgUnitType.DEPARTAMENTO, "Novo"));
    }

    @Test
    @DisplayName("update deve lançar 404 quando a unidade não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new OrgUnitRequest(OrgUnitType.ESCOLA, "X")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("delete deve remover e dar flush, nessa ordem")
    void delete_success() {
        OrgUnit existing = unit(1L, OrgUnitType.ESCOLA, "Escola");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("delete deve lançar 404 e não remover quando a unidade não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
