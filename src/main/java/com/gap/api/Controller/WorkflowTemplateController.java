package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.BaseResponseDTO;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("workflow-template")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final IWorkflowTemplateService WorkflowTemplateService;

    @PostMapping
    public ResponseEntity<BaseResponse<WorkflowTemplateResponse>> createTemplate(@RequestBody WorkflowTemplateRequest dto) {
        var entity = WorkflowTemplateService.createTemplate(dto);
        return ResponseEntity.ok(BaseResponse.success("Workflow template created successfully", entity));
    }

}
