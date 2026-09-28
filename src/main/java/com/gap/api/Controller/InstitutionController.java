package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Service.InstitutionService;
import com.gap.api.Service.Interface.IInstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final IInstitutionService service;

    @GetMapping
    public ResponseEntity<BaseResponse<List<InstitutionResponse>>> findAll() {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<InstitutionResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<InstitutionResponse>> create(@Valid @RequestBody InstitutionRequest request) {
        InstitutionResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success("Registro criado com sucesso.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<InstitutionResponse>> update(@PathVariable Long id,
                                                                    @Valid @RequestBody InstitutionRequest request) {
        return ResponseEntity.ok(BaseResponse.success("Registro atualizado com sucesso.", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(BaseResponse.<Void>success("Registro excluído com sucesso.", null));
    }
}
