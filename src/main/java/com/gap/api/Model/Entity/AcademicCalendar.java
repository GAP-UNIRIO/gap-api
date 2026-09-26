package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "academic_calendars")
public class AcademicCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "semester", nullable = false, length = 10, unique = true)
    private String semester; // Ex: "2026.1"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "deadlines")
    private Map<String, LocalDate> deadlines;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "academic_limits")
    private Map<String, Integer> academicLimits;

    // usar transactional para garantir que apenas um calendário esteja ativo por vez
    // usar indice unico parcial para garantir que apenas um calendário esteja ativo por vez
    @Column(name = "is_active", nullable = false)
    private boolean active = false;
}