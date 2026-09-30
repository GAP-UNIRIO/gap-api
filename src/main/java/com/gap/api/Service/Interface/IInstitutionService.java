package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Model.Entities.Institution;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IInstitutionService {

    List<InstitutionResponse> findAll();

    InstitutionResponse findById(Long id);

    InstitutionResponse create(InstitutionRequest request);

    InstitutionResponse update(Long id, InstitutionRequest request);

    void delete(Long id);


}
