package com.gap.api.Repository;

import com.gap.api.Model.Entity.ApprovalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalStageRepository extends JpaRepository<ApprovalStage, Long> {
    Optional<ApprovalStage> findByIdAndOrder_Id(Long id, Long orderId);
    List<ApprovalStage> findAllByOrder_IdOrderByStepOrderAsc(Long orderId);
}
