package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="orders")
public class Order {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private ZonedDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_solicitacao", nullable = false)
    private OrderType orderType;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Annex> annexes = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> extraData;// atributo para armazenar dados especficos das solicitacoes em json, permite flexibilidade

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ApprovalStage> approvalStages = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderHistory> orderHistories = new ArrayList<>();

    public enum OrderType {
        INCLUSAO_DISCIPLINA,
        EXCLUSAO_DISCIPLINA,
        APROVEITAMENTO_DE_DISCIPLINA,
        REVISAO_DE_PROVA,
        INTEGRALIZACAO_E_COLACAO_DE_GRAU,
        ATUALIZACAO_CADASTRAL,
        DECLARACAO_DE_MATRICULA,
        DECLARACAO_DE_CONTAGEM_DE_CREDITOS,
        EMISSAO_DE_HISTORICO_ESCOLAR,
        EMISSAO_DE_PROGRAMAS_DE_DISCIPLINAS,
        REGIME_EXCEPCIONAL_DE_APRENDIZAGEM,
        MIGRACAO_CURRICULAR,
        PRORROGACAO_PRAZO_INTEGRALIZACAO,
        CANCELAMENTO_MATRICULA,
        TRANCAMENTO_DE_MATRICULA
    }



}
