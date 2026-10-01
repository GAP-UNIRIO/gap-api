package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflow_steps")
public class WorkflowStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private WorkflowTemplate template;

    @Column(name = "step_order", nullable = false)
    private int stepOrder; // A ordem de execução (1, 2, 3...)

    @Enumerated(EnumType.STRING)
    @Column(name = "stage_type", nullable = false)
    private ApprovalStage.ApprovalStageType stageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "responsible_unit_type", nullable = false)
    private OrgUnit.OrgUnitType responsibleUnitType;
}