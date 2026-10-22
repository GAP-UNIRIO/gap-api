package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;

public record CourseResponse(
        Long id,
        String name,
        ResponsibleSummary director,
        ResponsibleSummary coordinator,
        ResponsibleSummary secretary,
        OrgUnitSummary department,
        OrgUnitSummary reitoria,
        OrgUnitSummary coordenacao,
        OrgUnitSummary direcao,
        OrgUnitSummary secretaria,
        OrgUnitSummary escola
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                ResponsibleSummary.from(course.getDirector()),
                ResponsibleSummary.from(course.getCoordinator()),
                ResponsibleSummary.from(course.getSecretary()),
                OrgUnitSummary.from(course.getDepartment()),
                OrgUnitSummary.from(course.getReitoria()),
                OrgUnitSummary.from(course.getCoordenacao()),
                OrgUnitSummary.from(course.getDirecao()),
                OrgUnitSummary.from(course.getSecretaria()),
                OrgUnitSummary.from(course.getEscola())
        );
    }

    public record ResponsibleSummary(Long id, String name, String email) {
        private static ResponsibleSummary from(User user) {
            return user == null ? null : new ResponsibleSummary(user.getId(), user.getName(), user.getEmail());
        }
    }

    public record OrgUnitSummary(Long id, String name) {
        private static OrgUnitSummary from(com.gap.api.Model.Entity.OrgUnit unit) {
            return unit == null ? null : new OrgUnitSummary(unit.getId(), unit.getName());
        }
    }
}
