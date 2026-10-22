package com.gap.api.Integration;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrgUnitRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.ICourseService;
import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class CourseServiceIntegrationTest {

    @Autowired
    private ICourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgUnitRepository orgUnitRepository;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @DisplayName("@spec:AC-036 banco rejeita nome duplicado ao criar curso")
    void create_duplicateName_isRejectedByDatabase() {
        Fixture fixture = fixture("duplicate-create");
        try {
            courseService.create(fixture.request("Curso " + fixture.suffix()));

            assertThrows(DataIntegrityViolationException.class,
                    () -> courseService.create(fixture.request("Curso " + fixture.suffix())));
        } finally {
            cleanup(fixture);
        }
    }

    @Test
    @DisplayName("@spec:AC-037 banco rejeita nome ausente")
    void create_missingName_isRejectedByDatabase() {
        Fixture fixture = fixture("missing-name");
        try {
            assertThrows(DataIntegrityViolationException.class,
                    () -> courseService.create(fixture.request(null)));
        } finally {
            cleanup(fixture);
        }
    }

    @Test
    @DisplayName("@spec:AC-040 banco rejeita nome duplicado na atualização")
    void update_duplicateName_isRejectedByDatabase() {
        Fixture fixture = fixture("duplicate-update");
        try {
            String firstName = "Curso A " + fixture.suffix();
            String secondName = "Curso B " + fixture.suffix();
            var first = courseService.create(fixture.request(firstName));
            var second = courseService.create(fixture.request(secondName));

            assertThrows(DataIntegrityViolationException.class,
                    () -> courseService.update(first.id(), fixture.request(secondName)));
        } finally {
            cleanup(fixture);
        }
    }

    @Test
    @DisplayName("@spec:AC-043 banco impede excluir curso ainda referenciado por usuário")
    void delete_courseReferencedByUser_isRejectedByDatabase() {
        Fixture fixture = fixture("referenced-delete");
        try {
            var course = courseService.create(fixture.request("Curso " + fixture.suffix()));
            User student = fixture.users().get(0);
            student.setCourse(courseRepository.findById(course.id()).orElseThrow());
            userRepository.saveAndFlush(student);

            assertThrows(DataIntegrityViolationException.class, () -> courseService.delete(course.id()));
        } finally {
            cleanup(fixture);
        }
    }

    private Fixture fixture(String label) {
        String suffix = label + "-" + UUID.randomUUID();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setEmail("course-" + suffix + "-" + i + "@example.test");
            user.setName("Course test user " + i);
            users.add(userRepository.saveAndFlush(user));
        }
        OrgUnit department = new OrgUnit();
        department.setName("Course department " + suffix);
        department.setType(OrgUnit.OrgUnitType.DEPARTAMENTO);
        department = orgUnitRepository.saveAndFlush(department);
        return new Fixture(suffix, users, department);
    }

    private void cleanup(Fixture fixture) {
        for (User user : fixture.users()) {
            User persisted = userRepository.findById(user.getId()).orElse(null);
            if (persisted != null) {
                persisted.setCourse(null);
                userRepository.saveAndFlush(persisted);
            }
        }
        courseRepository.deleteAll(courseRepository.findAll().stream()
                .filter(course -> course.getName() != null && course.getName().contains(fixture.suffix()))
                .toList());
        courseRepository.flush();
        userRepository.deleteAll(fixture.users());
        userRepository.flush();
        orgUnitRepository.delete(fixture.department());
        orgUnitRepository.flush();
    }

    private record Fixture(String suffix, List<User> users, OrgUnit department) {
        CourseRequest request(String name) {
            return new CourseRequest(name, users.get(0).getId(), users.get(1).getId(), users.get(2).getId(), department.getId());
        }
    }
}
