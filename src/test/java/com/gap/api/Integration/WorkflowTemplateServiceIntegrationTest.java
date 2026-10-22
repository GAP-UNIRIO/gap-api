package com.gap.api.Integration;

import com.gap.api.Model.DTO.WorkflowStepRequest;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import com.gap.api.Service.Interface.IStorageService;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkflowTemplateServiceIntegrationTest {

    @Autowired
    private IWorkflowTemplateService workflowTemplateService;

    @Autowired
    private WorkflowTemplateRepository templateRepository;

    @Autowired
    private CourseRepository courseRepository;

    @MockitoBean
    private IStorageService storageService;

    @Test
    @DisplayName("@spec:AC-099 persiste nome e descrição da etapa no template")
    void createTemplate_persistsStepNameAndDescription() {
        Course course = new Course();
        course.setName("Template workflow test " + UUID.randomUUID());
        course = courseRepository.saveAndFlush(course);

        var request = new WorkflowTemplateRequest(Order.OrderType.REVISAO_DE_PROVA, course.getId(), List.of(
                new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                        OrgUnit.OrgUnitType.COORDENACAO, "Coordenação", "Revisar solicitação")));

        var response = workflowTemplateService.createTemplate(request);
        templateRepository.flush();
        var persisted = templateRepository.findById(response.id()).orElseThrow();

        assertThat(persisted.getSteps()).singleElement().satisfies(step -> {
            assertThat(step.getStepName()).isEqualTo("Coordenação");
            assertThat(step.getStepDescription()).isEqualTo("Revisar solicitação");
        });

        templateRepository.delete(persisted);
        templateRepository.flush();
        courseRepository.delete(course);
        courseRepository.flush();
    }
}
