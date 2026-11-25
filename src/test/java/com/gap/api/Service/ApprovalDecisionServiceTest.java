package com.gap.api.Service;

import com.gap.api.Model.DTO.ApprovalDecisionRequest;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.ApprovalStageRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IOrderStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalDecisionServiceTest {

    @Mock private ApprovalStageRepository approvalStageRepository;
    @Mock private UserRepository userRepository;
    @Mock private IOrderStatusService orderStatusService;
    @InjectMocks private ApprovalDecisionService service;

    private Order order;
    private User actor;
    private OrgUnit unit;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(12L);
        order.setStatus(Order.OrderStatus.ABERTA);
        Course course = new Course();
        course.setId(4L);
        order.setUser(new User());
        order.getUser().setCourse(course);

        unit = new OrgUnit();
        unit.setId(8L);
        actor = new User();
        actor.setId(23L);
        actor.setEmail("approver@example.test");
        actor.setOrgUnits(Set.of(unit));
        org.mockito.Mockito.lenient().when(userRepository.findByEmail(actor.getEmail())).thenReturn(Optional.of(actor));
    }

    @Test
    @DisplayName("@spec:AC-200 usuário da unidade aprova a etapa atual e libera a seguinte")
    void decide_approveCurrentStage_marksApprovedAndStartsAnalysis() {
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        ApprovalStage next = stage(2, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(current, next));
        when(orderStatusService.transition(order, Order.OrderStatus.EM_ANALISE, actor)).thenAnswer(call -> {
            order.setStatus(Order.OrderStatus.EM_ANALISE);
            return order;
        });

        service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                ApprovalDecisionRequest.Decision.APPROVED, null));

        assertEquals(ApprovalStage.ApprovalStageStatus.APPROVED, current.getStatus());
        assertSame(actor, current.getDecidedBy());
        assertEquals(Order.OrderStatus.EM_ANALISE, order.getStatus());
        assertEquals(ApprovalStage.ApprovalStageStatus.PENDING, next.getStatus());
        verify(approvalStageRepository).save(current);
    }

    @Test
    @DisplayName("@spec:AC-201 reprovação sem justificativa é recusada sem alterar a etapa")
    void decide_rejectWithoutJustification_returnsBadRequestWithoutChanges() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.REJECTED, "  ")));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(approvalStageRepository, userRepository, orderStatusService);
    }

    @Test
    @DisplayName("@spec:AC-201 decisão ou corpo ausente é rejeitado")
    void decide_missingRequestOrDecision_returnsBadRequest() {
        for (ApprovalDecisionRequest request : new ApprovalDecisionRequest[]{null,
                new ApprovalDecisionRequest(null, null)}) {
            ResponseStatusException error = assertThrows(ResponseStatusException.class,
                    () -> service.decide(12L, 31L, actor.getEmail(), request));
            assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        }
        verifyNoInteractions(approvalStageRepository, userRepository, orderStatusService);
    }

    @Test
    @DisplayName("@spec:AC-204 usuário autenticado inexistente retorna 404")
    void decide_unknownUser_returnsNotFound() {
        when(userRepository.findByEmail(actor.getEmail())).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(approvalStageRepository, orderStatusService);
    }

    @Test
    @DisplayName("@spec:AC-205 etapa inexistente na solicitação retorna 404")
    void decide_unknownStage_returnsNotFound() {
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verify(approvalStageRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-205 etapa aprovada anteriormente não pode ser decidida novamente")
    void decide_noPendingStages_returnsUnprocessable() {
        ApprovalStage alreadyApproved = stage(1, ApprovalStage.ApprovalStageStatus.APPROVED);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(alreadyApproved));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(alreadyApproved));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(422, error.getStatusCode().value());
        verify(approvalStageRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-205 etapa não pode mudar solicitação já encerrada")
    void decide_finalOrder_returnsUnprocessable() {
        order.setStatus(Order.OrderStatus.CONCLUIDA);
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(current));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(422, error.getStatusCode().value());
        verify(approvalStageRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-202 reprovação justificada registra o motivo e indefere a solicitação")
    void decide_rejectWithJustification_recordsReasonAndIndeferesOrder() {
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(current));
        when(orderStatusService.transition(order, Order.OrderStatus.INDEFERIDA, actor)).thenAnswer(call -> {
            order.setStatus(Order.OrderStatus.INDEFERIDA);
            return order;
        });

        service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                ApprovalDecisionRequest.Decision.REJECTED, "Documento incompleto"));

        assertEquals(ApprovalStage.ApprovalStageStatus.REJECTED, current.getStatus());
        assertEquals("Documento incompleto", current.getRejectionJustification());
        assertSame(actor, current.getDecidedBy());
        assertEquals(Order.OrderStatus.INDEFERIDA, order.getStatus());
        verify(approvalStageRepository).save(current);
    }

    @Test
    @DisplayName("@spec:AC-203 aprovação da última etapa mantém a solicitação em análise")
    void decide_approveLastStage_doesNotCompleteOrder() {
        order.setStatus(Order.OrderStatus.EM_ANALISE);
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(current));

        service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                ApprovalDecisionRequest.Decision.APPROVED, null));

        assertEquals(Order.OrderStatus.EM_ANALISE, order.getStatus());
        verify(orderStatusService, never()).transition(any(), any(), any());
    }

    @Test
    @DisplayName("@spec:AC-204 usuário fora da unidade não pode decidir a etapa")
    void decide_userOutsideResponsibleUnit_returnsForbidden() {
        actor.setOrgUnits(Set.of());
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatusCode());
        assertEquals(ApprovalStage.ApprovalStageStatus.PENDING, current.getStatus());
        verify(approvalStageRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-204 etapa sem unidade responsável não pode ser decidida")
    void decide_stageWithoutResponsibleUnit_returnsConflict() {
        ApprovalStage current = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        current.setResponsibleUnit(null);
        when(approvalStageRepository.findByIdAndOrder_Id(31L, 12L)).thenReturn(Optional.of(current));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 31L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(approvalStageRepository, never()).save(any());
    }

    @Test
    @DisplayName("@spec:AC-205 etapa posterior não pode ser decidida antes da atual")
    void decide_laterStageBeforeCurrent_returnsUnprocessable() {
        ApprovalStage first = stage(1, ApprovalStage.ApprovalStageStatus.PENDING);
        ApprovalStage later = stage(2, ApprovalStage.ApprovalStageStatus.PENDING);
        when(approvalStageRepository.findByIdAndOrder_Id(32L, 12L)).thenReturn(Optional.of(later));
        when(approvalStageRepository.findAllByOrder_IdOrderByStepOrderAsc(12L)).thenReturn(List.of(first, later));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.decide(12L, 32L, actor.getEmail(), new ApprovalDecisionRequest(
                        ApprovalDecisionRequest.Decision.APPROVED, null)));

        assertEquals(422, error.getStatusCode().value());
        assertEquals(ApprovalStage.ApprovalStageStatus.PENDING, later.getStatus());
        verify(approvalStageRepository, never()).save(any());
    }

    private ApprovalStage stage(int orderNumber, ApprovalStage.ApprovalStageStatus status) {
        ApprovalStage stage = new ApprovalStage();
        stage.setId(30L + orderNumber);
        stage.setOrder(order);
        stage.setStepOrder(orderNumber);
        stage.setStatus(status);
        stage.setResponsibleUnit(unit);
        return stage;
    }
}
