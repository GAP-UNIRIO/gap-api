package com.gap.api.Service;

import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.WorkflowStep;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowTemplateService implements IWorkflowTemplateService {

    private final WorkflowTemplateRepository templateRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public WorkflowTemplateResponse createTemplate(WorkflowTemplateRequest dto) {
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

        List<WorkflowStep> steps = dto.steps().stream().map(stepDto -> {
            WorkflowStep step = new WorkflowStep();
            step.setStepOrder(stepDto.stepOrder());
            step.setStageType(stepDto.stageType());
            step.setResponsibleUnitType(stepDto.responsibleUnitType());
            step.setTemplate(template);
            return step;
        }).toList();

        template.getSteps().addAll(steps);

        return WorkflowTemplateResponse.from(templateRepository.save(template));
    }

}
