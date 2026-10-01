package com.gap.api.Repository;

import com.gap.api.Model.Entity.AcademicCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AcademicCalendarRepository extends JpaRepository<AcademicCalendar, Long> {

    boolean existsBySemester(String semester);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update AcademicCalendar c set c.active = false where c.active = true and c.id <> :id")
    int deactivateAllExcept(@Param("id") Long id);
}
