package com.gap.api.Service;

import com.gap.api.Model.DTO.RoleRequest;
import com.gap.api.Model.DTO.RoleResponse;
import com.gap.api.Model.Entities.Role;
import com.gap.api.Repository.RoleRepository;
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
class RoleServiceTest {

    @Mock
    private RoleRepository repository;

    @InjectMocks
    private RoleService service;

    private Role role(Long id, String authority) {
        return new Role(id, authority);
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findAll ----------

    @Test
    @DisplayName("findAll deve retornar os papéis ordenados por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id")))
                .thenReturn(List.of(role(1L, "ROLE_ADMIN"), role(2L, "ROLE_USER")));

        List<RoleResponse> result = service.findAll();

        assertThat(result).containsExactly(
                new RoleResponse(1L, "ROLE_ADMIN"),
                new RoleResponse(2L, "ROLE_USER"));
    }

    @Test
    @DisplayName("findAll deve retornar lista vazia quando não há papéis")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    // ---------- findById ----------

    @Test
    @DisplayName("findById deve retornar o papel quando existe")
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(role(1L, "ROLE_ADMIN")));

        RoleResponse result = service.findById(1L);

        assertThat(result).isEqualTo(new RoleResponse(1L, "ROLE_ADMIN"));
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
    @DisplayName("create deve salvar e retornar o novo papel")
    void create_success() {
        when(repository.existsByAuthority("ROLE_ADMIN")).thenReturn(false);
        when(repository.save(any(Role.class))).thenAnswer(inv -> {
            Role r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        RoleResponse result = service.create(new RoleRequest("ROLE_ADMIN"));

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(result).isEqualTo(new RoleResponse(10L, "ROLE_ADMIN"));
    }

    @Test
    @DisplayName("create deve lançar 409 quando a authority já existe")
    void create_conflict() {
        when(repository.existsByAuthority("ROLE_ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new RoleRequest("ROLE_ADMIN")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        verify(repository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve alterar a authority quando ela é nova")
    void update_success() {
        Role existing = role(1L, "ROLE_OLD");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByAuthority("ROLE_NEW")).thenReturn(false);

        RoleResponse result = service.update(1L, new RoleRequest("ROLE_NEW"));

        assertThat(existing.getAuthority()).isEqualTo("ROLE_NEW");
        assertThat(result).isEqualTo(new RoleResponse(1L, "ROLE_NEW"));
    }

    @Test
    @DisplayName("update com a mesma authority não deve checar duplicidade")
    void update_sameAuthority_skipsExistsCheck() {
        Role existing = role(1L, "ROLE_ADMIN");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        RoleResponse result = service.update(1L, new RoleRequest("ROLE_ADMIN"));

        assertThat(result).isEqualTo(new RoleResponse(1L, "ROLE_ADMIN"));
        verify(repository, never()).existsByAuthority(any());
    }

    @Test
    @DisplayName("update deve lançar 409 quando a nova authority já pertence a outro papel")
    void update_conflict() {
        Role existing = role(1L, "ROLE_OLD");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsByAuthority("ROLE_TAKEN")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new RoleRequest("ROLE_TAKEN")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        assertThat(existing.getAuthority()).isEqualTo("ROLE_OLD");
    }

    @Test
    @DisplayName("update deve lançar 404 quando o papel não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new RoleRequest("ROLE_X")))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve remover e dar flush, nessa ordem")
    void delete_success() {
        Role existing = role(1L, "ROLE_ADMIN");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("delete deve lançar 404 e não remover quando o papel não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
