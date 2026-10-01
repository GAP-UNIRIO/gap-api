package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;

import java.util.List;

public interface ICourseService {

    CourseResponse create(CourseRequest courseRequest);
    CourseResponse update(Long id, CourseRequest courseRequest);

    CourseResponse findById(Long id);

    List<CourseResponse> findAll();

    void delete(Long id);
}
