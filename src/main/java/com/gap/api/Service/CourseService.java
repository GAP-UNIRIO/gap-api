package com.gap.api.Service;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrgUnitRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.ICourseService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CourseService implements ICourseService {

    private final CourseRepository repository;
    private final UserRepository userRepository;
    private final OrgUnitRepository orgUnitRepository;


    @Transactional(readOnly = true)
    public List<CourseResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(CourseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        return CourseResponse.from(getOrThrow(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest courseRequest) {
        Course course = new Course();
        course.setName(courseRequest.name());
        setCDS(course, courseRequest.coordinatorId(), courseRequest.directorId(), courseRequest.secretaryId());
        setDepartment(course, courseRequest.departmentId());
        return CourseResponse.from(repository.save(course));
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest courseRequest) {
        Course course = getOrThrow(id);
        course.setName(courseRequest.name());
        setCDS(course, courseRequest.coordinatorId(), courseRequest.directorId(), courseRequest.secretaryId());
        setDepartment(course, courseRequest.departmentId());
        return CourseResponse.from(repository.save(course));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private void setCDS(Course course, Long coordinatorId, Long directorId, Long secretaryId) {
        User coordinator = findResponsible(coordinatorId, "Coordinator", "COORDINATOR");
        User director = findResponsible(directorId, "Director", "DIRECTOR");
        User secretary = findResponsible(secretaryId, "Secretary", "SECRETARY");
        course.setCoordinator(coordinator);
        course.setDirector(director);
        course.setSecretary(secretary);
    }

    private User findResponsible(Long id, String function, String authority) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, function + " not found: " + id));
        if (!user.getRoles().stream().filter(Objects::nonNull)
                .anyMatch(role -> authority.equals(role.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    function + " user " + id + " must have role " + authority);
        }
        return user;
    }

    private void setDepartment(Course course, Long departmentId) {
        OrgUnit department = orgUnitRepository.findById(departmentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found: " + departmentId));
        if (department.getType() != OrgUnit.OrgUnitType.DEPARTAMENTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Org unit must have type DEPARTAMENTO");
        }
        course.setDepartment(department);
    }

    private Course getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada: " + id));
    }
}
