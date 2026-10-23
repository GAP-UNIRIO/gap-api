package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "org_units", uniqueConstraints = @UniqueConstraint(name = "uk_org_units_name", columnNames = "name"))
public class
OrgUnit {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgUnitType type;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @OneToMany(mappedBy = "responsibleUnit", fetch = FetchType.LAZY)
    private List<ApprovalStage> approvalStages;

    public enum OrgUnitType {
        REITORIA,
        COORDENACAO,
        DIRECAO,
        SECRETARIA,
        DEPARTAMENTO,
        ESCOLA
    }

}
