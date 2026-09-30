package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.RoleRequest;
import com.gap.api.Model.DTO.RoleResponse;
import com.gap.api.Service.Interface.IRoleService;
import com.gap.api.Service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final IRoleService service;

    @GetMapping
    public ResponseEntity<BaseResponse<List<RoleResponse>>> findAll() {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<RoleResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
        RoleResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success("Registro criado com sucesso.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<RoleResponse>> update(@PathVariable Long id,
                                                             @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(BaseResponse.success("Registro atualizado com sucesso.", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(BaseResponse.<Void>success("Registro excluído com sucesso.", null));
    }
}
