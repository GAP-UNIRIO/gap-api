package com.gap.api.Repository;

import com.gap.api.Model.Entity.ApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApprovalStageRepository extends JpaRepository<ApprovalStage, Long> {


}
