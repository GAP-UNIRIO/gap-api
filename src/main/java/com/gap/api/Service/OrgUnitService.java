package com.gap.api.Service;

import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Model.Entities.OrgUnit;
import com.gap.api.Repository.OrgUnitRepository;
import com.gap.api.Service.Interface.IOrgUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrgUnitService implements IOrgUnitService {

    private final OrgUnitRepository repository;

    @Transactional(readOnly = true)
    public List<OrgUnitResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(OrgUnitResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrgUnitResponse findById(Long id) {
        return OrgUnitResponse.from(getOrThrow(id));
    }

    @Transactional
    public OrgUnitResponse create(OrgUnitRequest request) {
        OrgUnit unit = new OrgUnit();
        apply(unit, request);
        return OrgUnitResponse.from(repository.save(unit));
    }

    @Transactional
    public OrgUnitResponse update(Long id, OrgUnitRequest request) {
        OrgUnit unit = getOrThrow(id);
        apply(unit, request);
        return OrgUnitResponse.from(unit);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush(); // força a checagem de FK (approval_stages) aqui
    }

    private void apply(OrgUnit unit, OrgUnitRequest request) {
        unit.setType(request.type());
        unit.setName(request.name());
    }

    private OrgUnit getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidade organizacional não encontrada: " + id));
    }
}
