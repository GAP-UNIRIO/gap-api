package com.gap.api.Repository;

import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {
    
    Optional<WorkflowTemplate> findByOrderTypeAndCourse_Id(Order.OrderType orderType, Long CourseId);

    Optional<WorkflowTemplate> findByOrderTypeAndCourseIsNull(Order.OrderType orderType);
}
