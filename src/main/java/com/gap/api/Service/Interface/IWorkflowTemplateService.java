package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;

public interface IWorkflowTemplateService {

    WorkflowTemplateResponse createTemplate(WorkflowTemplateRequest dto);



}
