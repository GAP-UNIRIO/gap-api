package com.gap.api.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status = OrderStatus.ABERTA;

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

    /**
     * Ciclo de vida da solicitação. As transições permitidas ficam no próprio enum;
     * status sem transição de saída são finais.
     */
    public enum OrderStatus {
        ABERTA,
        EM_ANALISE,
        EM_DILIGENCIA,
        DEFERIDA,
        INDEFERIDA,
        CANCELADA,
        CONCLUIDA;

        private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

        static {
            TRANSITIONS.put(ABERTA, EnumSet.of(EM_ANALISE, CANCELADA));
            TRANSITIONS.put(EM_ANALISE, EnumSet.of(EM_DILIGENCIA, DEFERIDA, INDEFERIDA));
            TRANSITIONS.put(EM_DILIGENCIA, EnumSet.of(EM_ANALISE, CANCELADA));
            TRANSITIONS.put(DEFERIDA, EnumSet.of(CONCLUIDA));
            TRANSITIONS.put(INDEFERIDA, EnumSet.noneOf(OrderStatus.class));
            TRANSITIONS.put(CANCELADA, EnumSet.noneOf(OrderStatus.class));
            TRANSITIONS.put(CONCLUIDA, EnumSet.noneOf(OrderStatus.class));
        }

        public boolean canTransitionTo(OrderStatus target) {
            return TRANSITIONS.get(this).contains(target);
        }

        public boolean isFinal() {
            return TRANSITIONS.get(this).isEmpty();
        }
    }



}
