package com.gap.api.Service;

import com.gap.api.Model.Entity.*;
import com.gap.api.Repository.ApprovalStageRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
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
    @DisplayName("@spec:AC-009 @spec:AC-101 prefere template específico do curso")
    void instantiateWorkflow_prefersCourseSpecificTemplate() {
        Order order = orderWithCourse(31L);
        WorkflowTemplate specific = template();
        WorkflowTemplate fallback = template();
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 31L))
                .thenReturn(Optional.of(specific));
        when(workflowTemplateRepository.findByOrderTypeAndCourseIsNull(order.getOrderType()))
                .thenReturn(Optional.of(fallback));

        workflowEngineService.instantiateWorkflowForOrder(order);

        verify(workflowTemplateRepository).findByOrderTypeAndCourse_Id(order.getOrderType(), 31L);
        verify(workflowTemplateRepository, never()).findByOrderTypeAndCourseIsNull(order.getOrderType());
        verify(approvalStageRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("@spec:AC-009 @spec:AC-102 usa template padrão quando não existe template de curso")
    void instantiateWorkflow_fallsBackToDefaultTemplate() {
        Order order = orderWithCourse(32L);
        WorkflowTemplate fallback = template(step(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                OrgUnit.OrgUnitType.COORDENACAO));
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 32L))
                .thenReturn(Optional.empty());
        when(workflowTemplateRepository.findByOrderTypeAndCourseIsNull(order.getOrderType()))
                .thenReturn(Optional.of(fallback));

        workflowEngineService.instantiateWorkflowForOrder(order);

        verify(workflowTemplateRepository).findByOrderTypeAndCourseIsNull(order.getOrderType());
        List<ApprovalStage> saved = capturedStages();
        assertEquals(1, saved.size());
        assertSame(order, saved.get(0).getOrder());
        assertSame(order.getUser().getCourse().getCoordenacao(), saved.get(0).getResponsibleUnit());
    }

    @Test
    @DisplayName("@spec:AC-103 instancia etapas ordenadas, pendentes e associadas ao pedido/unidade")
    void instantiateWorkflow_savesSortedPendingStagesWithResponsibleUnits() {
        Order order = orderWithCourse(33L);
        WorkflowTemplate template = template(
                step(2, ApprovalStage.ApprovalStageType.DIRECTOR, OrgUnit.OrgUnitType.DIRECAO),
                step(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO));
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 33L))
                .thenReturn(Optional.of(template));

        workflowEngineService.instantiateWorkflowForOrder(order);

        List<ApprovalStage> saved = capturedStages();
        assertEquals(2, saved.size());
        assertEquals(List.of(1, 2), saved.stream().map(ApprovalStage::getStepOrder).toList());
        assertEquals(List.of(ApprovalStage.ApprovalStageType.COORDINATOR, ApprovalStage.ApprovalStageType.DIRECTOR),
                saved.stream().map(ApprovalStage::getType).toList());
        assertTrue(saved.stream().allMatch(stage -> stage.getStatus() == ApprovalStage.ApprovalStageStatus.PENDING));
        assertTrue(saved.stream().allMatch(stage -> stage.getOrder() == order));
        assertSame(order.getUser().getCourse().getCoordenacao(), saved.get(0).getResponsibleUnit());
        assertSame(order.getUser().getCourse().getDirecao(), saved.get(1).getResponsibleUnit());
    }

    @Test
    @DisplayName("@spec:AC-110 resolve DEPARTAMENTO usa a unidade associada ao curso")
    void instantiateWorkflow_resolvesDepartment() {
        Order order = orderWithCourse(35L);
        OrgUnit department = unit(50L, OrgUnit.OrgUnitType.DEPARTAMENTO);
        order.getUser().getCourse().setDepartment(department);
        WorkflowTemplate template = template(step(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                OrgUnit.OrgUnitType.DEPARTAMENTO));
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 35L))
                .thenReturn(Optional.of(template));

        workflowEngineService.instantiateWorkflowForOrder(order);

        assertSame(department, capturedStages().get(0).getResponsibleUnit());
    }

    @Test
    @DisplayName("@spec:AC-112 rejeita etapa de departamento quando o curso legado não tem vínculo")
    void instantiateWorkflow_rejectsMissingDepartment() {
        Order order = orderWithCourse(36L);
        WorkflowTemplate template = template(step(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                OrgUnit.OrgUnitType.DEPARTAMENTO));
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 36L))
                .thenReturn(Optional.of(template));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workflowEngineService.instantiateWorkflowForOrder(order));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verifyNoInteractions(approvalStageRepository);
    }

    @Test
    @DisplayName("@spec:AC-009 @spec:AC-104 retorna 503 quando não existe template e não salva etapas")
    void instantiateWorkflow_missingTemplateDoesNotSaveStages() {
        Order order = orderWithCourse(34L);
        when(workflowTemplateRepository.findByOrderTypeAndCourse_Id(order.getOrderType(), 34L))
                .thenReturn(Optional.empty());
        when(workflowTemplateRepository.findByOrderTypeAndCourseIsNull(order.getOrderType()))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> workflowEngineService.instantiateWorkflowForOrder(order));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("No workflow template found"));
        verifyNoInteractions(approvalStageRepository);
    }

    private List<ApprovalStage> capturedStages() {
        ArgumentCaptor<Iterable> captor = ArgumentCaptor.forClass(Iterable.class);
        verify(approvalStageRepository).saveAll(captor.capture());
        List<ApprovalStage> stages = new ArrayList<>();
        for (Object stage : captor.getValue()) stages.add((ApprovalStage) stage);
        return stages;
    }

    private Order orderWithCourse(Long courseId) {
        Order order = new Order();
        order.setOrderType(Order.OrderType.INCLUSAO_DISCIPLINA);
        Course course = new Course();
        course.setId(courseId);
        course.setCoordenacao(unit(1L, OrgUnit.OrgUnitType.COORDENACAO));
        course.setDirecao(unit(2L, OrgUnit.OrgUnitType.DIRECAO));
        User user = new User();
        user.setCourse(course);
        order.setUser(user);
        return order;
    }

    private OrgUnit unit(Long id, OrgUnit.OrgUnitType type) {
        OrgUnit unit = new OrgUnit();
        unit.setId(id);
        unit.setType(type);
        unit.setName(type.name());
        return unit;
    }

    private WorkflowTemplate template(WorkflowStep... steps) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setSteps(List.of(steps));
        return template;
    }

    private WorkflowStep step(int order, ApprovalStage.ApprovalStageType stageType,
                              OrgUnit.OrgUnitType responsibleUnitType) {
        WorkflowStep step = new WorkflowStep();
        step.setStepOrder(order);
        step.setStageType(stageType);
        step.setResponsibleUnitType(responsibleUnitType);
        return step;
    }
}
