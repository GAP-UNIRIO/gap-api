package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.AcademicCalendarRequest;
import com.gap.api.Model.DTO.AcademicCalendarResponse;
import com.gap.api.Model.Entities.AcademicCalendar;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IAcademicCalendarService {

    List<AcademicCalendarResponse> findAll();

    AcademicCalendarResponse findById(Long id);

    AcademicCalendarResponse create(AcademicCalendarRequest request);

    AcademicCalendarResponse update(Long id, AcademicCalendarRequest request);

    void delete(Long id);

}
