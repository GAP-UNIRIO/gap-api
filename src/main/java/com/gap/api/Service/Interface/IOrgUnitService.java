package com.gap.api.Service.Interface;

import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Model.Entities.OrgUnit;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrgUnitService {

    List<OrgUnitResponse> findAll();

    OrgUnitResponse findById(Long id);

    OrgUnitResponse create(OrgUnitRequest request);

    OrgUnitResponse update(Long id, OrgUnitRequest request);

    void delete(Long id);

}
