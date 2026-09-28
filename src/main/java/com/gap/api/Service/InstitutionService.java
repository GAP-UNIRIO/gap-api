package com.gap.api.Service;

import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Model.Entities.Institution;
import com.gap.api.Repository.InstitutionRepository;
import com.gap.api.Service.Interface.IInstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstitutionService implements IInstitutionService {

    private final InstitutionRepository repository;

    @Transactional(readOnly = true)
    public List<InstitutionResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream().map(InstitutionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public InstitutionResponse findById(Long id) {
        return InstitutionResponse.from(getOrThrow(id));
    }

    @Transactional
    public InstitutionResponse create(InstitutionRequest request) {
        if (repository.existsByCode(request.code())) {
            throw codeConflict();
        }
        Institution institution = new Institution();
        apply(institution, request);
        institution.setActive(request.active() == null || request.active());
        return InstitutionResponse.from(repository.save(institution));
    }

    @Transactional
    public InstitutionResponse update(Long id, InstitutionRequest request) {
        Institution institution = getOrThrow(id);
        if (!institution.getCode().equals(request.code()) && repository.existsByCode(request.code())) {
            throw codeConflict();
        }
        apply(institution, request);
        if (request.active() != null) {
            institution.setActive(request.active());
        }
        return InstitutionResponse.from(institution);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private void apply(Institution institution, InstitutionRequest request) {
        institution.setName(request.name());
        institution.setCode(request.code());
        institution.setAuthProviderType(request.authProviderType());
        institution.setSignatureProvider(request.signatureProvider());
        institution.setSettings(request.settings());
    }

    private ResponseStatusException codeConflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma instituição com esse código (CNPJ).");
    }

    private Institution getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada: " + id));
    }
}
