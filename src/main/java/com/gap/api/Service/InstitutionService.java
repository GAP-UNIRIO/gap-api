package com.gap.api.Service;

import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Model.Entity.Institution;
import com.gap.api.Repository.InstitutionRepository;
import com.gap.api.Service.Interface.IInstitutionService;
import com.gap.api.Validation.CnpjValidator;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.sql.SQLException;

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
        String code = CnpjValidator.normalize(request.code());
        if (repository.existsByCode(code)) {
            throw codeConflict();
        }
        Institution institution = new Institution();
        apply(institution, request, code);
        institution.setActive(request.active() == null || request.active());
        try {
            return InstitutionResponse.from(repository.saveAndFlush(institution));
        } catch (DataIntegrityViolationException ex) {
            if (isCodeUniqueViolation(ex)) throw codeConflict();
            throw ex;
        }
    }

    @Transactional
    public InstitutionResponse update(Long id, InstitutionRequest request) {
        Institution institution = getOrThrow(id);
        String code = CnpjValidator.normalize(request.code());
        if (!institution.getCode().equals(code) && repository.existsByCode(code)) {
            throw codeConflict();
        }
        apply(institution, request, code);
        if (request.active() != null) {
            institution.setActive(request.active());
        }
        try {
            repository.saveAndFlush(institution);
            return InstitutionResponse.from(institution);
        } catch (DataIntegrityViolationException ex) {
            if (isCodeUniqueViolation(ex)) throw codeConflict();
            throw ex;
        }
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
        repository.flush();
    }

    private void apply(Institution institution, InstitutionRequest request, String code) {
        institution.setName(request.name());
        institution.setCode(code);
        institution.setAuthProviderType(request.authProviderType());
        institution.setSignatureProvider(request.signatureProvider());
        institution.setSettings(request.settings());
    }

    private boolean isCodeUniqueViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                if (name != null && name.toLowerCase().contains("code")) return true;
            }
            if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) {
                String message = sql.getMessage();
                return message == null || message.toLowerCase().contains("code");
            }
        }
        return false;
    }

    private ResponseStatusException codeConflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma instituição com esse código (CNPJ).");
    }

    private Institution getOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Instituição não encontrada: " + id));
    }
}
