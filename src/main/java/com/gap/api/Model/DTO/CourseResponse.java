package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;

public record CourseResponse(
        Long id,
        String name,
        User director,
        User coordinator,
        User secretary
) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDirector(),
                course.getCoordinator(),
                course.getSecretary()
        );
    }
}
