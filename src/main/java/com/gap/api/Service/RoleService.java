package com.gap.api.Service;

import com.gap.api.Model.DTO.RoleRequest;
import com.gap.api.Model.DTO.RoleResponse;
import com.gap.api.Model.Entities.Role;
import com.gap.api.Repository.RoleRepository;
import com.gap.api.Service.Interface.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository repository;

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(RoleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleResponse.from(getOrThrow(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request) {
        if (repository.existsByAuthority(request.authority())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um papel com essa authority.");
        }
        Role role = new Role();
        role.setAuthority(request.authority());
        return RoleResponse.from(repository.save(role));
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest request) {
        Role role = getOrThrow(id);
        if (!role.getAuthority().equals(request.authority())
                && repository.existsByAuthority(request.authority())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um papel com essa authority.");
        }
        role.setAuthority(request.authority());
        return RoleResponse.from(role);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush(); // força a checagem de FK aqui, para o handler devolver 409
    }

    private Role getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Papel não encontrado: " + id));
    }
}
