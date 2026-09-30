package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Service.Interface.IOrgUnitService;
import com.gap.api.Service.OrgUnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/org-units")
@RequiredArgsConstructor
public class OrgUnitController {

    private final IOrgUnitService service;

    @GetMapping
    public ResponseEntity<BaseResponse<List<OrgUnitResponse>>> findAll() {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<OrgUnitResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<OrgUnitResponse>> create(@Valid @RequestBody OrgUnitRequest request) {
        OrgUnitResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success("Registro criado com sucesso.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<OrgUnitResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody OrgUnitRequest request) {
        return ResponseEntity.ok(BaseResponse.success("Registro atualizado com sucesso.", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(BaseResponse.<Void>success("Registro excluído com sucesso.", null));
    }
}
