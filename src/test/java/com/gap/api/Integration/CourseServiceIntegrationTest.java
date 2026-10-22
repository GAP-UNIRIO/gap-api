package com.gap.api.Integration;

import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.Role;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrgUnitRepository;
import com.gap.api.Repository.RoleRepository;
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
import static org.assertj.core.api.Assertions.assertThat;

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

    @Autowired
    private RoleRepository roleRepository;

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
    @DisplayName("chamada direta ao serviço preserva a restrição NOT NULL do banco")
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
    @DisplayName("@spec:AC-120 create e update persistem e devolvem os cinco resumos de unidades")
    void organizationalUnits_arePersistedAndReturned() {
        Fixture fixture = fixture("organizational-units");
        try {
            var created = courseService.create(fixture.request("Curso " + fixture.suffix()));
            assertThat(created.reitoria().id()).isEqualTo(fixture.reitoria().getId());
            assertThat(created.coordenacao().id()).isEqualTo(fixture.coordenacao().getId());
            assertThat(created.direcao().id()).isEqualTo(fixture.direcao().getId());
            assertThat(created.secretaria().id()).isEqualTo(fixture.secretaria().getId());
            assertThat(created.escola().id()).isEqualTo(fixture.escola().getId());

            var updated = courseService.update(created.id(), fixture.request("Curso atualizado " + fixture.suffix()));
            assertThat(updated.reitoria().id()).isEqualTo(fixture.reitoria().getId());
            assertThat(updated.coordenacao().id()).isEqualTo(fixture.coordenacao().getId());
            assertThat(updated.direcao().id()).isEqualTo(fixture.direcao().getId());
            assertThat(updated.secretaria().id()).isEqualTo(fixture.secretaria().getId());
            assertThat(updated.escola().id()).isEqualTo(fixture.escola().getId());
        } finally {
            cleanup(fixture);
        }
    }

    @Test
    @DisplayName("@spec:AC-121 cada tipo incompatível retorna 400 sem salvar")
    void incompatibleOrganizationalUnitType_isRejectedWithoutSaving() {
        Fixture fixture = fixture("incompatible-unit");
        try {
            for (int i = 0; i < fixture.units().size(); i++) {
                CourseRequest request = fixture.requestWithUnitId(i, fixture.department().getId());
                var exception = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                        () -> courseService.create(request));
                assertThat(exception.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).contains(String.valueOf(fixture.department().getId()));
                assertThat(courseRepository.findAll().stream().noneMatch(course -> course.getName() != null && course.getName().contains(fixture.suffix()))).isTrue();
            }
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
        List<String> authorities = List.of("DIRECTOR", "COORDINATOR", "SECRETARY");
        for (int i = 0; i < 3; i++) {
            User user = new User();
            user.setEmail("course-" + suffix + "-" + i + "@example.test");
            user.setName("Course test user " + i);
            user.addRole(role(authorities.get(i)));
            users.add(userRepository.saveAndFlush(user));
        }
        OrgUnit department = new OrgUnit();
        department.setName("Course department " + suffix);
        department.setType(OrgUnit.OrgUnitType.DEPARTAMENTO);
        department = orgUnitRepository.saveAndFlush(department);
        List<OrgUnit> units = new ArrayList<>();
        for (OrgUnit.OrgUnitType type : List.of(OrgUnit.OrgUnitType.REITORIA, OrgUnit.OrgUnitType.COORDENACAO,
                OrgUnit.OrgUnitType.DIRECAO, OrgUnit.OrgUnitType.SECRETARIA, OrgUnit.OrgUnitType.ESCOLA)) {
            OrgUnit unit = new OrgUnit();
            unit.setName(type + " " + suffix);
            unit.setType(type);
            units.add(orgUnitRepository.saveAndFlush(unit));
        }
        return new Fixture(suffix, users, department, units);
    }

    private Role role(String authority) {
        return roleRepository.findAll().stream()
                .filter(role -> authority.equals(role.getAuthority()))
                .findFirst()
                .orElseGet(() -> roleRepository.saveAndFlush(new Role(null, authority)));
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
        fixture.units().forEach(orgUnitRepository::delete);
        orgUnitRepository.flush();
    }

    private record Fixture(String suffix, List<User> users, OrgUnit department, List<OrgUnit> units) {
        OrgUnit reitoria() { return units.get(0); }
        OrgUnit coordenacao() { return units.get(1); }
        OrgUnit direcao() { return units.get(2); }
        OrgUnit secretaria() { return units.get(3); }
        OrgUnit escola() { return units.get(4); }
        CourseRequest request(String name) {
            return new CourseRequest(name, users.get(0).getId(), users.get(1).getId(), users.get(2).getId(), department.getId(),
                    reitoria().getId(), coordenacao().getId(), direcao().getId(), secretaria().getId(), escola().getId());
        }
        CourseRequest requestWithUnitId(int index, Long departmentId) {
            Long[] ids = { reitoria().getId(), coordenacao().getId(), direcao().getId(), secretaria().getId(), escola().getId() };
            ids[index] = departmentId;
            return new CourseRequest("Curso " + suffix + " invalid-" + index, users.get(0).getId(), users.get(1).getId(), users.get(2).getId(), departmentId,
                    ids[0], ids[1], ids[2], ids[3], ids[4]);
        }
    }
}
