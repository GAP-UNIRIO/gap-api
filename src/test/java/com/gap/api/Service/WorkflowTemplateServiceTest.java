package com.gap.api.Service;

import com.gap.api.Model.DTO.WorkflowStepRequest;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.*;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkflowTemplateServiceTest {

    @Mock
    private WorkflowTemplateRepository templateRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private WorkflowTemplateService workflowTemplateService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("@spec:AC-095 @spec:AC-099 createTemplate associa curso e mapeia cada etapa")
    void createTemplate_Success() {
        // Arrange
        WorkflowStepRequest step1 = new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                "Coordenação", "Conferência inicial");
        WorkflowStepRequest step2 = new WorkflowStepRequest(2, ApprovalStage.ApprovalStageType.DIRECTOR, OrgUnit.OrgUnitType.DIRECAO,
                "Direção", "Aprovação final");
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, List.of(step1, step2));

        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
        when(templateRepository.findByOrderTypeAndCourse_Id(request.orderType(), request.courseId())).thenReturn(Optional.empty());

        WorkflowTemplate template = new WorkflowTemplate();


        template.setOrderType(request.orderType());
        template.setCourse(course);
        when(templateRepository.save(any(WorkflowTemplate.class))).thenAnswer(invocation -> {
            WorkflowTemplate savedTemplate = invocation.getArgument(0);
            savedTemplate.setId(10L);
            return savedTemplate;
        });

        // Act
        WorkflowTemplateResponse response = workflowTemplateService.createTemplate(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.orderType(), response.orderType());
        ArgumentCaptor<WorkflowTemplate> captor = ArgumentCaptor.forClass(WorkflowTemplate.class);
        verify(templateRepository, times(1)).save(captor.capture());
        WorkflowTemplate saved = captor.getValue();
        assertEquals(course, saved.getCourse());
        assertEquals(2, saved.getSteps().size());
        assertEquals("Coordenação", response.steps().get(0).stepName());
        assertEquals("Conferência inicial", response.steps().get(0).stepDescription());
        assertEquals(List.of(1, 2), saved.getSteps().stream().map(WorkflowStep::getStepOrder).toList());
        assertEquals(ApprovalStage.ApprovalStageType.COORDINATOR, saved.getSteps().get(0).getStageType());
        assertEquals(OrgUnit.OrgUnitType.COORDENACAO, saved.getSteps().get(0).getResponsibleUnitType());
        assertEquals("Coordenação", saved.getSteps().get(0).getStepName());
        assertEquals("Conferência inicial", saved.getSteps().get(0).getStepDescription());
        assertTrue(saved.getSteps().stream().allMatch(step -> step.getTemplate() == saved));
    }

    @Test
    @DisplayName("@spec:AC-096 createTemplate deve lançar erro quando o curso não for encontrado")
    void createTemplate_CourseNotFound() {
        // Arrange
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, null);
        when(courseRepository.findById(request.courseId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workflowTemplateService.createTemplate(request));
        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-097 createTemplate deve rejeitar template específico duplicado")
    void createTemplate_TemplateAlreadyExists() {
        // Arrange
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, null);
        when(templateRepository.findByOrderTypeAndCourse_Id(request.orderType(), request.courseId())).thenReturn(Optional.of(new WorkflowTemplate()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workflowTemplateService.createTemplate(request));
        assertEquals("Workflow template already exists for this order type and course", exception.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-097 createTemplate deve rejeitar template específico duplicado com etapas")
    void createTemplate_ShouldThrowException_WhenTemplateAlreadyExistsForCourse() {
        // Arrange
        WorkflowStepRequest step1 = new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                "Coordenação", "Conferência inicial");
        WorkflowStepRequest step2 = new WorkflowStepRequest(2, ApprovalStage.ApprovalStageType.DIRECTOR, OrgUnit.OrgUnitType.DIRECAO,
                "Direção", "Aprovação final");
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, List.of(step1, step2));
        WorkflowTemplate existingTemplate = new WorkflowTemplate();
        when(templateRepository.findByOrderTypeAndCourse_Id(request.orderType(), request.courseId()))
                .thenReturn(Optional.of(existingTemplate));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workflowTemplateService.createTemplate(request));
        assertEquals("Workflow template already exists for this order type and course", exception.getMessage());
    }

    @Test
    @DisplayName("@spec:AC-094 @spec:AC-098 createTemplate aceita template padrão com etapas e rejeita padrão duplicado")
    void createTemplate_defaultAndDuplicate() {
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(
                Order.OrderType.REVISAO_DE_PROVA, null,
                List.of(new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                        OrgUnit.OrgUnitType.COORDENACAO, "Coordenação", "Conferência")));
        WorkflowTemplate saved = new WorkflowTemplate();
        saved.setOrderType(request.orderType());
        when(templateRepository.findByOrderTypeAndCourseIsNull(request.orderType())).thenReturn(Optional.empty());
        when(templateRepository.save(any(WorkflowTemplate.class))).thenAnswer(invocation -> {
            WorkflowTemplate entity = invocation.getArgument(0);
            entity.setId(22L);
            return entity;
        });

        WorkflowTemplateResponse response = workflowTemplateService.createTemplate(request);

        assertEquals(22L, response.id());
        assertNull(response.course());
        ArgumentCaptor<WorkflowTemplate> captor = ArgumentCaptor.forClass(WorkflowTemplate.class);
        verify(templateRepository).save(captor.capture());
        assertEquals(1, captor.getValue().getSteps().size());

        when(templateRepository.findByOrderTypeAndCourseIsNull(request.orderType()))
                .thenReturn(Optional.of(saved));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> workflowTemplateService.createTemplate(request));
        assertEquals("Workflow template already exists for this order type and course", exception.getMessage());
    }
}
