package com.gap.api.Service;

import com.gap.api.Model.DTO.AcademicCalendarRequest;
import com.gap.api.Model.DTO.AcademicCalendarResponse;
import com.gap.api.Model.Entity.AcademicCalendar;
import com.gap.api.Repository.AcademicCalendarRepository;
import com.gap.api.Service.Interface.IAcademicCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AcademicCalendarService implements IAcademicCalendarService {

    private final AcademicCalendarRepository repository;

    @Transactional(readOnly = true)
    public List<AcademicCalendarResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(AcademicCalendarResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AcademicCalendarResponse findById(Long id) {
        return AcademicCalendarResponse.from(getOrThrow(id));
    }

    @Transactional
    public AcademicCalendarResponse create(AcademicCalendarRequest request) {
        validateDates(request);
        if (repository.existsBySemester(request.semester())) {
            throw semesterConflict();
        }
        AcademicCalendar calendar = new AcademicCalendar();
        apply(calendar, request);
        calendar.setActive(Boolean.TRUE.equals(request.active()));

        AcademicCalendar saved = repository.save(calendar);
        if (saved.isActive()) {
            repository.deactivateAllExcept(saved.getId());
        }
        return AcademicCalendarResponse.from(saved);
    }

    @Transactional
    public AcademicCalendarResponse update(Long id, AcademicCalendarRequest request) {
        AcademicCalendar calendar = getOrThrow(id);
        validateDates(request);
        if (!calendar.getSemester().equals(request.semester())
                && repository.existsBySemester(request.semester())) {
            throw semesterConflict();
        }
        apply(calendar, request);
        if (request.active() != null) {
            calendar.setActive(request.active());
        }
        if (calendar.isActive()) {
            repository.deactivateAllExcept(calendar.getId());
        }
        return AcademicCalendarResponse.from(calendar);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private void apply(AcademicCalendar calendar, AcademicCalendarRequest request) {
        calendar.setSemester(request.semester());
        calendar.setStartDate(request.startDate());
        calendar.setEndDate(request.endDate());
        calendar.setDeadlines(request.deadlines());
        calendar.setAcademicLimits(request.academicLimits());
    }

    private void validateDates(AcademicCalendarRequest request) {
        LocalDate today = LocalDate.now();
        if (!request.startDate().isAfter(today) || !request.endDate().isAfter(today)) {
            throw invalidCalendar("As datas de início e término devem ser futuras.");
        }
        if (request.endDate().isBefore(request.startDate())) {
            throw invalidCalendar("A data de término não pode ser anterior à data de início.");
        }

        int year = Integer.parseInt(request.semester().substring(0, 4));
        boolean firstSemester = request.semester().endsWith(".1");
        if (!firstSemester && year == 9999) {
            throw invalidCalendar("O semestre 9999.2 não pode ter término no ano seguinte.");
        }
        LocalDate earliestStart = firstSemester
                ? LocalDate.of(year, 1, 1)
                : LocalDate.of(year, 5, 1);
        LocalDate latestStart = firstSemester
                ? LocalDate.of(year, 7, 31)
                : LocalDate.of(year, 12, 31);
        LocalDate latestEnd = firstSemester
                ? LocalDate.of(year, 9, 30)
                : LocalDate.of(year + 1, 3, 31);

        if (request.startDate().isBefore(earliestStart)
                || request.startDate().isAfter(latestStart)
                || request.endDate().isAfter(latestEnd)) {
            throw invalidCalendar("As datas não correspondem à janela permitida para o semestre.");
        }

        boolean deadlineOutsidePeriod = request.deadlines().values().stream()
                .anyMatch(deadline -> deadline.isBefore(request.startDate()) || deadline.isAfter(request.endDate()));
        if (deadlineOutsidePeriod) {
            throw invalidCalendar("Todos os prazos devem estar dentro do período do calendário.");
        }
    }

    private ResponseStatusException invalidCalendar(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException semesterConflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um calendário para esse semestre.");
    }

    private AcademicCalendar getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Calendário acadêmico não encontrado: " + id));
    }
}
