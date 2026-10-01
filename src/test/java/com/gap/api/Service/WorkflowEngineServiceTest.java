package com.gap.api.Service;

import com.gap.api.Model.Entity.*;
import com.gap.api.Repository.ApprovalStageRepository;
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

class WorkflowEngineServiceTest {

    @Mock
    private ApprovalStageRepository approvalStageRepository;

    @Mock
    private WorkflowTemplateRepository workflowTemplateRepository;

    @InjectMocks
    private WorkflowEngineService workflowEngineService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("instantiateWorkflowForOrder deve criar estágios de aprovação com sucesso")
    void instantiateWorkflowForOrder_Success() {
        // Arrange
        Order order = new Order();
        order.setOrderType(Order.OrderType.INCLUSAO_DISCIPLINA);
        User user = new User();
        Course course = new Course();
        course.setId(1L);
        user.setCourse(course);
        order.setUser(user);

        WorkflowTemplate template = new WorkflowTemplate();
        WorkflowStep step1 = new WorkflowStep();
        step1.setStepOrder(1);
        step1.setStageType(ApprovalStage.ApprovalStageType.COORDINATOR);
        step1.setResponsibleUnitType(OrgUnit.OrgUnitType.COORDENACAO);
        WorkflowStep step2 = new WorkflowStep();
        step2.setStepOrder(2);
        step2.setStageType(ApprovalStage.ApprovalStageType.DIRECTOR);
        step2.setResponsibleUnitType(OrgUnit.OrgUnitType.DIRECAO);
        template.setSteps(List.of(step1, step2));

        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), course.getId())).thenReturn(Optional.of(template));

        // Act
        workflowEngineService.instantiateWorkflowForOrder(order);

        // Assert
        verify(approvalStageRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("instantiateWorkflowForOrder deve lançar exceção quando o template não for encontrado")
    void instantiateWorkflowForOrder_TemplateNotFound() {
        // Arrange
        Order order = new Order();
        order.setOrderType(Order.OrderType.INCLUSAO_DISCIPLINA);
        User user = new User();
        Course course = new Course();
        course.setId(1L);
        user.setCourse(course);
        order.setUser(user);

        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), course.getId())).thenReturn(Optional.empty());
        when(workflowTemplateRepository.findByOrderTypeAndCourseIsNull(order.getOrderType())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> workflowEngineService.instantiateWorkflowForOrder(order));
        assertEquals("No workflow template found for order type: INCLUSAO_DISCIPLINA", exception.getMessage());
    }
}
