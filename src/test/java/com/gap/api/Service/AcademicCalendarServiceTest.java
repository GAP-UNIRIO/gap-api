package com.gap.api.Service;

import com.gap.api.Model.DTO.AcademicCalendarRequest;
import com.gap.api.Model.DTO.AcademicCalendarResponse;
import com.gap.api.Model.Entity.AcademicCalendar;
import com.gap.api.Repository.AcademicCalendarRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicCalendarServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 2, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 30);
    private static final Map<String, LocalDate> DEADLINES = Map.of("matricula", LocalDate.of(2026, 1, 20));
    private static final Map<String, Integer> LIMITS = Map.of("maxCreditos", 24);

    @Mock
    private AcademicCalendarRepository repository;

    @InjectMocks
    private AcademicCalendarService service;

    private AcademicCalendar calendar(Long id, String semester, boolean active) {
        AcademicCalendar c = new AcademicCalendar();
        c.setId(id);
        c.setSemester(semester);
        c.setStartDate(START);
        c.setEndDate(END);
        c.setDeadlines(DEADLINES);
        c.setAcademicLimits(LIMITS);
        c.setActive(active);
        return c;
    }

    private AcademicCalendarRequest request(String semester, LocalDate start, LocalDate end, Boolean active) {
        return new AcademicCalendarRequest(semester, start, end, DEADLINES, LIMITS, active);
    }

    private void assertStatus(Throwable ex, HttpStatus expected) {
        assertThat(ex).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(expected));
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve retornar os calendários ordenados por id")
    void findAll_returnsMappedList() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of(
                calendar(1L, "2025.2", false),
                calendar(2L, "2026.1", true)));

        List<AcademicCalendarResponse> result = service.findAll();

        assertThat(result).extracting(AcademicCalendarResponse::semester)
                .containsExactly("2025.2", "2026.1");
        assertThat(result).extracting(AcademicCalendarResponse::active)
                .containsExactly(false, true);
    }

    @Test
    @DisplayName("findAll deve retornar lista vazia quando não há calendários")
    void findAll_empty() {
        when(repository.findAll(Sort.by("id"))).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    @DisplayName("findById deve retornar o calendário quando existe")
    void findById_found() {
        when(repository.findById(1L)).thenReturn(Optional.of(calendar(1L, "2026.1", true)));

        AcademicCalendarResponse result = service.findById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.semester()).isEqualTo("2026.1");
        assertThat(result.startDate()).isEqualTo(START);
        assertThat(result.endDate()).isEqualTo(END);
        assertThat(result.deadlines()).isEqualTo(DEADLINES);
        assertThat(result.academicLimits()).isEqualTo(LIMITS);
        assertThat(result.active()).isTrue();
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
    @DisplayName("create com active null deve salvar inativo e não desativar os demais")
    void create_defaultsToInactive() {
        when(repository.existsBySemester("2026.1")).thenReturn(false);
        when(repository.save(any(AcademicCalendar.class))).thenAnswer(inv -> {
            AcademicCalendar c = inv.getArgument(0);
            c.setId(1L);
            return c;
        });

        AcademicCalendarResponse result = service.create(request("2026.1", START, END, null));

        ArgumentCaptor<AcademicCalendar> captor = ArgumentCaptor.forClass(AcademicCalendar.class);
        verify(repository).save(captor.capture());
        AcademicCalendar saved = captor.getValue();
        assertThat(saved.getSemester()).isEqualTo("2026.1");
        assertThat(saved.getStartDate()).isEqualTo(START);
        assertThat(saved.getEndDate()).isEqualTo(END);
        assertThat(saved.getDeadlines()).isEqualTo(DEADLINES);
        assertThat(saved.getAcademicLimits()).isEqualTo(LIMITS);
        assertThat(saved.isActive()).isFalse();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.active()).isFalse();
        verify(repository, never()).deactivateAllExcept(anyLong());
    }

    @Test
    @DisplayName("create com active=true deve desativar todos os outros calendários")
    void create_active_deactivatesOthers() {
        when(repository.existsBySemester("2026.1")).thenReturn(false);
        when(repository.save(any(AcademicCalendar.class))).thenAnswer(inv -> {
            AcademicCalendar c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });

        AcademicCalendarResponse result = service.create(request("2026.1", START, END, true));

        assertThat(result.active()).isTrue();
        InOrder order = inOrder(repository);
        order.verify(repository).save(any(AcademicCalendar.class));
        order.verify(repository).deactivateAllExcept(3L);
    }

    @Test
    @DisplayName("create com active=false explícito não deve desativar os demais")
    void create_explicitInactive() {
        when(repository.existsBySemester("2026.1")).thenReturn(false);
        when(repository.save(any(AcademicCalendar.class))).thenAnswer(inv -> inv.getArgument(0));

        AcademicCalendarResponse result = service.create(request("2026.1", START, END, false));

        assertThat(result.active()).isFalse();
        verify(repository, never()).deactivateAllExcept(anyLong());
    }

    @Test
    @DisplayName("create deve aceitar data de término igual à data de início")
    void create_sameStartAndEnd_isValid() {
        when(repository.existsBySemester("2026.1")).thenReturn(false);
        when(repository.save(any(AcademicCalendar.class))).thenAnswer(inv -> inv.getArgument(0));

        AcademicCalendarResponse result = service.create(request("2026.1", START, START, null));

        assertThat(result.startDate()).isEqualTo(START);
        assertThat(result.endDate()).isEqualTo(START);
    }

    @Test
    @DisplayName("create deve lançar 400 quando o término é anterior ao início, sem tocar o repositório")
    void create_invalidDates() {
        AcademicCalendarRequest invalid = request("2026.1", END, START, null);

        assertThatThrownBy(() -> service.create(invalid))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("create deve lançar 409 quando o semestre já existe")
    void create_semesterConflict() {
        when(repository.existsBySemester("2026.1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("2026.1", START, END, null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        verify(repository, never()).save(any());
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve alterar os campos do calendário")
    void update_success() {
        AcademicCalendar existing = calendar(1L, "2025.2", false);
        LocalDate newStart = LocalDate.of(2026, 8, 1);
        LocalDate newEnd = LocalDate.of(2026, 12, 15);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsBySemester("2026.2")).thenReturn(false);

        AcademicCalendarResponse result = service.update(1L, request("2026.2", newStart, newEnd, null));

        assertThat(existing.getSemester()).isEqualTo("2026.2");
        assertThat(existing.getStartDate()).isEqualTo(newStart);
        assertThat(existing.getEndDate()).isEqualTo(newEnd);
        assertThat(result.semester()).isEqualTo("2026.2");
        verify(repository, never()).deactivateAllExcept(anyLong());
    }

    @Test
    @DisplayName("update com o mesmo semestre não deve checar duplicidade")
    void update_sameSemester_skipsExistsCheck() {
        AcademicCalendar existing = calendar(1L, "2026.1", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.update(1L, request("2026.1", START, END, null));

        verify(repository, never()).existsBySemester(anyString());
    }

    @Test
    @DisplayName("update com active=true deve ativar e desativar os demais")
    void update_activate_deactivatesOthers() {
        AcademicCalendar existing = calendar(1L, "2026.1", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        AcademicCalendarResponse result = service.update(1L, request("2026.1", START, END, true));

        assertThat(existing.isActive()).isTrue();
        assertThat(result.active()).isTrue();
        verify(repository).deactivateAllExcept(1L);
    }

    @Test
    @DisplayName("update com active=false deve desativar sem mexer nos demais")
    void update_deactivate() {
        AcademicCalendar existing = calendar(1L, "2026.1", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        AcademicCalendarResponse result = service.update(1L, request("2026.1", START, END, false));

        assertThat(existing.isActive()).isFalse();
        assertThat(result.active()).isFalse();
        verify(repository, never()).deactivateAllExcept(anyLong());
    }

    @Test
    @DisplayName("update com active null num calendário ativo deve mantê-lo ativo e desativar os demais")
    void update_nullActive_keepsActiveAndDeactivatesOthers() {
        AcademicCalendar existing = calendar(1L, "2026.1", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        AcademicCalendarResponse result = service.update(1L, request("2026.1", START, END, null));

        assertThat(result.active()).isTrue();
        verify(repository).deactivateAllExcept(1L);
    }

    @Test
    @DisplayName("update com active null num calendário inativo deve mantê-lo inativo")
    void update_nullActive_keepsInactive() {
        AcademicCalendar existing = calendar(1L, "2026.1", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        AcademicCalendarResponse result = service.update(1L, request("2026.1", START, END, null));

        assertThat(result.active()).isFalse();
        verify(repository, never()).deactivateAllExcept(anyLong());
    }

    @Test
    @DisplayName("update deve lançar 404 quando o calendário não existe")
    void update_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request("2026.1", START, END, null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("update deve lançar 400 quando o término é anterior ao início")
    void update_invalidDates() {
        AcademicCalendar existing = calendar(1L, "2026.1", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(1L, request("2026.1", END, START, null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.BAD_REQUEST));

        assertThat(existing.getStartDate()).isEqualTo(START);
        assertThat(existing.getEndDate()).isEqualTo(END);
    }

    @Test
    @DisplayName("update deve lançar 409 quando o novo semestre pertence a outro calendário")
    void update_semesterConflict() {
        AcademicCalendar existing = calendar(1L, "2025.2", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.existsBySemester("2026.1")).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("2026.1", START, END, null)))
                .satisfies(ex -> assertStatus(ex, HttpStatus.CONFLICT));

        assertThat(existing.getSemester()).isEqualTo("2025.2");
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve remover e dar flush, nessa ordem")
    void delete_success() {
        AcademicCalendar existing = calendar(1L, "2026.1", false);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        InOrder order = inOrder(repository);
        order.verify(repository).delete(existing);
        order.verify(repository).flush();
    }

    @Test
    @DisplayName("delete deve lançar 404 e não remover quando o calendário não existe")
    void delete_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .satisfies(ex -> assertStatus(ex, HttpStatus.NOT_FOUND));

        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
