package com.gap.api.Model.DTO;

import com.gap.api.Model.Entities.AcademicCalendar;

import java.time.LocalDate;
import java.util.Map;

public record AcademicCalendarResponse(
        Long id,
        String semester,
        LocalDate startDate,
        LocalDate endDate,
        Map<String, LocalDate> deadlines,
        Map<String, Integer> academicLimits,
        boolean active
) {

    public static AcademicCalendarResponse from(AcademicCalendar c) {
        return new AcademicCalendarResponse(
                c.getId(),
                c.getSemester(),
                c.getStartDate(),
                c.getEndDate(),
                c.getDeadlines(),
                c.getAcademicLimits(),
                c.isActive());
    }
}
