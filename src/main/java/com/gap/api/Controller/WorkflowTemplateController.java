package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("workflow-template")
@RequiredArgsConstructor
public class WorkflowTemplateController {

    private final IWorkflowTemplateService WorkflowTemplateService;

    @PostMapping
    public ResponseEntity<BaseResponse<WorkflowTemplateResponse>> createTemplate(@Valid @RequestBody WorkflowTemplateRequest dto) {
        var entity = WorkflowTemplateService.createTemplate(dto);
        return ResponseEntity.ok(BaseResponse.success("Workflow template created successfully", entity));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<WorkflowTemplateResponse>>> findAll() {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", WorkflowTemplateService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<WorkflowTemplateResponse>> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", WorkflowTemplateService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<WorkflowTemplateResponse>> update(@PathVariable("id") Long id,
                                                                         @Valid @RequestBody WorkflowTemplateRequest dto) {
        return ResponseEntity.ok(BaseResponse.success("Registro atualizado com sucesso.", WorkflowTemplateService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable("id") Long id) {
        WorkflowTemplateService.delete(id);
        return ResponseEntity.ok(BaseResponse.<Void>success("Registro excluído com sucesso.", null));
    }
}
