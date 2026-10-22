package com.gap.api.Integration;

import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrgUnitRepository;
import com.gap.api.Service.Interface.IOrgUnitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.gap.api.Service.Interface.IStorageService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class OrgUnitServiceIntegrationTest {

    @Autowired
    private IOrgUnitService orgUnitService;

    @Autowired
    private OrgUnitRepository orgUnitRepository;

    @Autowired
    private CourseRepository courseRepository;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @DisplayName("@spec:AC-074 banco rejeita tipo ou nome nulo")
    void create_nullRequiredField_isRejectedByDatabase() {
        assertThrows(DataIntegrityViolationException.class,
                () -> orgUnitService.create(new OrgUnitRequest(OrgUnit.OrgUnitType.ESCOLA, null)));
        assertThrows(DataIntegrityViolationException.class,
                () -> orgUnitService.create(new OrgUnitRequest(null, "Unidade")));
    }

    @Test
    @DisplayName("@spec:AC-079 banco impede excluir unidade referenciada por curso")
    void delete_unitReferencedByCourse_isRejectedByDatabase() {
        String suffix = UUID.randomUUID().toString();
        OrgUnit unit = new OrgUnit();
        unit.setType(OrgUnit.OrgUnitType.REITORIA);
        unit.setName("Reitoria " + suffix);
        unit = orgUnitRepository.saveAndFlush(unit);

        Course course = new Course();
        course.setName("Curso para teste de unidade " + suffix);
        course.setReitoria(unit);
        course = courseRepository.saveAndFlush(course);

        Long unitId = unit.getId();
        Long courseId = course.getId();
        try {
            assertThrows(DataIntegrityViolationException.class, () -> orgUnitService.delete(unitId));
        } finally {
            courseRepository.deleteById(courseId);
            courseRepository.flush();
            orgUnitRepository.deleteById(unitId);
            orgUnitRepository.flush();
        }
    }
}
