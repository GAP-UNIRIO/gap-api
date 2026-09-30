package com.gap.api.Repository;

import com.gap.api.Model.Entities.OrgUnit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrgUnitRepository extends JpaRepository<OrgUnit, Long> {
}
