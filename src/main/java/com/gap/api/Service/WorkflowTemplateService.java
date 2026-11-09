package com.gap.api.Service;

import com.gap.api.Model.DTO.WorkflowStepRequest;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.WorkflowStep;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateService implements IWorkflowTemplateService {

    private final WorkflowTemplateRepository templateRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public WorkflowTemplateResponse createTemplate(WorkflowTemplateRequest dto) {
        validateStepOrders(dto);
        if (dto.courseId() != null) {
            if (templateRepository.findByOrderTypeAndCourse_Id(dto.orderType(), dto.courseId()).isPresent()){
                throw new IllegalArgumentException("Workflow template already exists for this order type and course");
            }
        } else if (templateRepository.findByOrderTypeAndCourseIsNull(dto.orderType()).isPresent()) {
            throw  new IllegalArgumentException("Workflow template already exists for this order type and course");
        }

        WorkflowTemplate template = new WorkflowTemplate();
        template.setOrderType(dto.orderType());

        if (dto.courseId() != null) {
            Course course = courseRepository.findById(dto.courseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found"));
            template.setCourse(course);
        }

        template.getSteps().addAll(buildSteps(dto, template));

        return WorkflowTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public List<WorkflowTemplateResponse> findAll() {
        return templateRepository.findAll(Sort.by("id")).stream().map(WorkflowTemplateResponse::from).toList();
    }

    @Transactional
    public WorkflowTemplateResponse findById(Long id) {
        return WorkflowTemplateResponse.from(getOrThrow(id));
    }

    @Transactional
    public WorkflowTemplateResponse update(Long id, WorkflowTemplateRequest dto) {
        WorkflowTemplate template = getOrThrow(id);
        validateStepOrders(dto);
        findByOrderTypeAndCourse(dto.orderType(), dto.courseId())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Já existe um template para esse tipo de solicitação e curso: " + other.getId());
                });
        Course course = dto.courseId() == null ? null : courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: " + dto.courseId()));

        template.setOrderType(dto.orderType());
        template.setCourse(course);
        template.getSteps().clear();
        template.getSteps().addAll(buildSteps(dto, template));
        return WorkflowTemplateResponse.from(templateRepository.save(template));
    }

    @Transactional
    public void delete(Long id) {
        templateRepository.delete(getOrThrow(id));
    }

    private WorkflowTemplate getOrThrow(Long id) {
        return templateRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Template de workflow não encontrado: " + id));
    }

    private Optional<WorkflowTemplate> findByOrderTypeAndCourse(Order.OrderType orderType, Long courseId) {
        return courseId == null
                ? templateRepository.findByOrderTypeAndCourseIsNull(orderType)
                : templateRepository.findByOrderTypeAndCourse_Id(orderType, courseId);
    }

    private void validateStepOrders(WorkflowTemplateRequest dto) {
        if (dto.steps() == null) {
            return;
        }
        Set<Integer> orders = new HashSet<>();
        for (WorkflowStepRequest step : dto.steps()) {
            if (!orders.add(step.stepOrder())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "As etapas não podem repetir o stepOrder (" + step.stepOrder() + " aparece mais de uma vez).");
            }
        }
    }

    private List<WorkflowStep> buildSteps(WorkflowTemplateRequest dto, WorkflowTemplate template) {
        return dto.steps().stream().map(stepDto -> {
            WorkflowStep step = new WorkflowStep();
            step.setStepOrder(stepDto.stepOrder());
            step.setStageType(stepDto.stageType());
            step.setResponsibleUnitType(stepDto.responsibleUnitType());
            step.setStepName(stepDto.stepName());
            step.setStepDescription(stepDto.stepDescription());
            step.setTemplate(template);
            return step;
        }).toList();
    }
}
