package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;

import java.util.List;

public interface IInstitutionService {

    List<InstitutionResponse> findAll();

    InstitutionResponse findById(Long id);

    InstitutionResponse create(InstitutionRequest request);

    InstitutionResponse update(Long id, InstitutionRequest request);

    void delete(Long id);


}
