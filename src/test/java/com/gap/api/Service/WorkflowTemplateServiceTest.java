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
import org.springframework.data.domain.Sort;
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

    // ---------- consulta, edição e exclusão (workflow-template-crud) ----------

    @Test
    @DisplayName("@spec:AC-158 findAll lista os templates com etapas ordenadas e curso resumido")
    void findAll_returnsTemplatesWithSortedSteps() {
        Course course = course(1L);
        WorkflowTemplate template = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, course,
                step(2, "Direção"), step(1, "Coordenação"));
        when(templateRepository.findAll(Sort.by("id"))).thenReturn(List.of(template));

        List<WorkflowTemplateResponse> result = workflowTemplateService.findAll();

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).id());
        assertEquals(new WorkflowTemplateResponse.CourseSummary(1L, "Sistemas de Informação"), result.get(0).course());
        assertEquals(List.of(1, 2), result.get(0).steps().stream().map(WorkflowTemplateResponse.WorkflowStepSummary::stepOrder).toList());
    }

    @Test
    @DisplayName("@spec:AC-159 findById retorna o template com etapas ordenadas")
    void findById_returnsTemplate() {
        WorkflowTemplate template = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null,
                step(2, "Direção"), step(1, "Coordenação"));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(template));

        WorkflowTemplateResponse response = workflowTemplateService.findById(5L);

        assertEquals(Order.OrderType.TRANCAMENTO_DE_MATRICULA, response.orderType());
        assertNull(response.course());
        assertEquals("Coordenação", response.steps().get(0).stepName());
    }

    @Test
    @DisplayName("@spec:AC-160 consultar, editar ou excluir template inexistente lança 404")
    void missingTemplate_throwsNotFound() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertStatus(assertThrows(ResponseStatusException.class, () -> workflowTemplateService.findById(99L)), HttpStatus.NOT_FOUND);
        assertStatus(assertThrows(ResponseStatusException.class,
                () -> workflowTemplateService.update(99L, request(Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, stepRequest(1)))), HttpStatus.NOT_FOUND);
        assertStatus(assertThrows(ResponseStatusException.class, () -> workflowTemplateService.delete(99L)), HttpStatus.NOT_FOUND);
        verify(templateRepository, never()).save(any());
        verify(templateRepository, never()).delete(any());
    }

    @Test
    @DisplayName("@spec:AC-161 update substitui tipo, curso e etapas")
    void update_replacesTypeCourseAndSteps() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        Course course = course(1L);
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(templateRepository.findByOrderTypeAndCourse_Id(Order.OrderType.REVISAO_DE_PROVA, 1L)).thenReturn(Optional.empty());
        when(templateRepository.save(existing)).thenReturn(existing);

        WorkflowTemplateResponse response = workflowTemplateService.update(5L,
                request(Order.OrderType.REVISAO_DE_PROVA, 1L, stepRequest(1), stepRequest(2)));

        assertEquals(Order.OrderType.REVISAO_DE_PROVA, existing.getOrderType());
        assertSame(course, existing.getCourse());
        assertEquals(2, existing.getSteps().size());
        assertTrue(existing.getSteps().stream().allMatch(s -> s.getTemplate() == existing));
        assertEquals(1L, response.course().id());
        assertEquals(List.of(1, 2), response.steps().stream().map(WorkflowTemplateResponse.WorkflowStepSummary::stepOrder).toList());
    }

    @Test
    @DisplayName("@spec:AC-161 update mantendo tipo e curso não conflita com o próprio template")
    void update_sameTypeAndCourse_doesNotConflictWithItself() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(templateRepository.findByOrderTypeAndCourseIsNull(Order.OrderType.TRANCAMENTO_DE_MATRICULA)).thenReturn(Optional.of(existing));
        when(templateRepository.save(existing)).thenReturn(existing);

        workflowTemplateService.update(5L, request(Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, stepRequest(1)));

        verify(templateRepository).save(existing);
    }

    @Test
    @DisplayName("@spec:AC-162 update recusa duplicar outro template com 409")
    void update_duplicateOfAnotherTemplate_throwsConflict() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        WorkflowTemplate other = template(9L, Order.OrderType.REVISAO_DE_PROVA, null);
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(templateRepository.findByOrderTypeAndCourseIsNull(Order.OrderType.REVISAO_DE_PROVA)).thenReturn(Optional.of(other));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> workflowTemplateService.update(5L, request(Order.OrderType.REVISAO_DE_PROVA, null, stepRequest(1))));

        assertStatus(error, HttpStatus.CONFLICT);
        assertEquals(Order.OrderType.TRANCAMENTO_DE_MATRICULA, existing.getOrderType());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-163 update com curso inexistente lança 404")
    void update_courseNotFound_throwsNotFound() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(templateRepository.findByOrderTypeAndCourse_Id(Order.OrderType.TRANCAMENTO_DE_MATRICULA, 77L)).thenReturn(Optional.empty());
        when(courseRepository.findById(77L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> workflowTemplateService.update(5L, request(Order.OrderType.TRANCAMENTO_DE_MATRICULA, 77L, stepRequest(1))));

        assertStatus(error, HttpStatus.NOT_FOUND);
        assertNull(existing.getCourse());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-164 criar ou editar com stepOrder repetido lança 400")
    void duplicateStepOrder_throwsBadRequest() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));
        WorkflowTemplateRequest duplicated = request(Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, stepRequest(1), stepRequest(1));

        assertStatus(assertThrows(ResponseStatusException.class, () -> workflowTemplateService.update(5L, duplicated)), HttpStatus.BAD_REQUEST);
        assertStatus(assertThrows(ResponseStatusException.class, () -> workflowTemplateService.createTemplate(duplicated)), HttpStatus.BAD_REQUEST);
        assertEquals(1, existing.getSteps().size());
        verify(templateRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-165 delete remove o template")
    void delete_removesTemplate() {
        WorkflowTemplate existing = template(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA, null, step(1, "Antiga"));
        when(templateRepository.findById(5L)).thenReturn(Optional.of(existing));

        workflowTemplateService.delete(5L);

        verify(templateRepository).delete(existing);
    }

    private void assertStatus(ResponseStatusException error, HttpStatus expected) {
        assertEquals(expected.value(), error.getStatusCode().value());
    }

    private Course course(Long id) {
        Course course = new Course();
        course.setId(id);
        course.setName("Sistemas de Informação");
        return course;
    }

    private WorkflowStep step(int order, String name) {
        WorkflowStep step = new WorkflowStep();
        step.setStepOrder(order);
        step.setStageType(ApprovalStage.ApprovalStageType.COORDINATOR);
        step.setResponsibleUnitType(OrgUnit.OrgUnitType.COORDENACAO);
        step.setStepName(name);
        step.setStepDescription("Descrição de " + name);
        return step;
    }

    private WorkflowTemplate template(Long id, Order.OrderType type, Course course, WorkflowStep... steps) {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setId(id);
        template.setOrderType(type);
        template.setCourse(course);
        for (WorkflowStep step : steps) {
            step.setTemplate(template);
            template.getSteps().add(step);
        }
        return template;
    }

    private WorkflowStepRequest stepRequest(int order) {
        return new WorkflowStepRequest(order, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                "Etapa " + order, "Descrição da etapa " + order);
    }

    private WorkflowTemplateRequest request(Order.OrderType type, Long courseId, WorkflowStepRequest... steps) {
        return new WorkflowTemplateRequest(type, courseId, List.of(steps));
    }
}
