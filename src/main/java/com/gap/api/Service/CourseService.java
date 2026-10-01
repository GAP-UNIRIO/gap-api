package com.gap.api.Service;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.ICourseService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository repository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(CourseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Course findById(Long id) {
        return getOrThrow(id);
    }

    @Transactional
    public CourseResponse create(CourseRequest courseRequest) {
        Course course = new Course();
        course.setName(courseRequest.name());
        setCDS(course, courseRequest.coordinatorId(), courseRequest.directorId(), courseRequest.secretaryId());
        return CourseResponse.from(repository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest courseRequest) {
        Course course = getOrThrow(id);
        course.setName(courseRequest.name());
        setCDS(course, courseRequest.coordinatorId(), courseRequest.directorId(), courseRequest.secretaryId());
        return CourseResponse.from(repository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private void setCDS(Course course, Long coordinatorId, Long directorId, Long secretaryId) {
        User coordinator = userRepository.getReferenceById(coordinatorId);
        course.setCoordinator(coordinator);
        User director = userRepository.getReferenceById(directorId);
        course.setDirector(director);
        User secretary = userRepository.getReferenceById(secretaryId);
        course.setSecretary(secretary);
    }

    private Course getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada: " + id));
    }
}
