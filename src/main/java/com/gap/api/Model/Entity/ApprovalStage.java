package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.ZonedDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="approval_stages")
public class ApprovalStage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false)
    private ApprovalStageType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStageStatus status;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User whoApproved;

    @ManyToOne
    @JoinColumn(name = "org_unit_id")
    private OrgUnit responsibleUnit;



    public enum ApprovalStageType {
        COORDINATOR,
        DIRECTOR,
        SECRETARY
    }

    public enum ApprovalStageStatus {
        PENDING,
        APPROVED,
        REJECTED
    }

}
