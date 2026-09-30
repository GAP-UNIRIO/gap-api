package com.gap.api.Repository;

import com.gap.api.Model.Entities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    boolean existsByCode(String code);
}
