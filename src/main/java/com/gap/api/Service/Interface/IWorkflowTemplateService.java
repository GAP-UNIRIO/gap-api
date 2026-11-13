package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;

import java.util.List;

public interface IWorkflowTemplateService {

    WorkflowTemplateResponse createTemplate(WorkflowTemplateRequest dto);

    List<WorkflowTemplateResponse> findAll();

    WorkflowTemplateResponse findById(Long id);

    WorkflowTemplateResponse update(Long id, WorkflowTemplateRequest dto);

    void delete(Long id);
}
