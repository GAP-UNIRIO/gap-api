package com.gap.api.Controller;

import com.gap.api.Model.DTO.BaseResponse;
import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Service.Interface.ICourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final ICourseService service;

    @PostMapping
    public ResponseEntity<BaseResponse<CourseResponse>> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location)
                .body(BaseResponse.success("Registro criado com sucesso.", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<CourseResponse>> update(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(BaseResponse.success("Registro atualizado com sucesso.", service.update(id, request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<CourseResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findById(id)));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CourseResponse>>> findAll() {
        return ResponseEntity.ok(BaseResponse.success("Consulta realizada com sucesso.", service.findAll()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(BaseResponse.success("Registro excluído com sucesso.", null));
    }

}
