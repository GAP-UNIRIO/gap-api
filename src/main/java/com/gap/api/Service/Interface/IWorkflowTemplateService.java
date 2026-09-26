package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.WorkflowTemplateRequestDTO;
import com.gap.api.Model.Entity.WorkflowTemplate;

public interface IWorkflowTemplateService {

    WorkflowTemplate createTemplate(WorkflowTemplateRequestDTO dto);



}
