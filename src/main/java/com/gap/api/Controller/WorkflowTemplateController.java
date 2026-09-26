package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponseDTO;
import com.gap.api.Model.DTO.WorkflowTemplateRequestDTO;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController("workflow-template")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    IWorkflowTemplateService WorkflowTemplateService;

    @PostMapping
    public ResponseEntity<BaseResponseDTO> createTemplate(@RequestBody WorkflowTemplateRequestDTO dto) {
        var entity = WorkflowTemplateService.createTemplate(dto);
        return ResponseEntity.ok(new BaseResponseDTO("200", "Workflow template created successfully", entity));
    }

}
