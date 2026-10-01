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
    @DisplayName("createTemplate deve criar um template com sucesso")
    void createTemplate_Success() {
        // Arrange
        WorkflowStepRequest step1 = new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO);
        WorkflowStepRequest step2 = new WorkflowStepRequest(2, ApprovalStage.ApprovalStageType.DIRECTOR, OrgUnit.OrgUnitType.DIRECAO);
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, List.of(step1, step2));

        Course course = new Course();
        course.setId(1L);
        when(courseRepository.findById(request.courseId())).thenReturn(Optional.of(course));
        when(templateRepository.findByOrderTypeAndCourse_Id(request.orderType(), request.courseId())).thenReturn(Optional.empty());

        WorkflowTemplate template = new WorkflowTemplate();


        template.setOrderType(request.orderType());
        template.setCourse(course);
        when(templateRepository.save(any(WorkflowTemplate.class))).thenReturn(template);

        // Act
        WorkflowTemplateResponse response = workflowTemplateService.createTemplate(request);

        // Assert
        assertNotNull(response);
        assertEquals(request.orderType(), response.orderType());
        verify(templateRepository, times(1)).save(any(WorkflowTemplate.class));
    }

    @Test
    @DisplayName("createTemplate deve lançar exceção quando o curso não for encontrado")
    void createTemplate_CourseNotFound() {
        // Arrange
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, null);
        when(courseRepository.findById(request.courseId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workflowTemplateService.createTemplate(request));
        assertEquals("Course not found", exception.getMessage());
    }

    @Test
    @DisplayName("createTemplate deve lançar exceção quando o template já existir")
    void createTemplate_TemplateAlreadyExists() {
        // Arrange
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, null);
        when(templateRepository.findByOrderTypeAndCourse_Id(request.orderType(), request.courseId())).thenReturn(Optional.of(new WorkflowTemplate()));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> workflowTemplateService.createTemplate(request));
        assertEquals("Workflow template already exists for this order type and course", exception.getMessage());
    }
}
