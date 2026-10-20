package com.gap.api.Service;

import com.gap.api.Model.DTO.AnnexRequest;
import com.gap.api.Model.DTO.AnnexUploadRequest;
import com.gap.api.Model.DTO.OrderRequest;
import com.gap.api.Model.Entity.Annex;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.OrderRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private IStorageService storageService;
    @Mock private WorkflowEngineService workflowEngineService;
    @InjectMocks private OrderService orderService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(7L);
        user.setName("Test User");
        user.setEmail("order-create@example.test");

    }

    @Test
    void generateAnnexUploadUrl_returnsStorageAuthorization() {
        var authorization = new IStorageService.UploadUrlResponse("https://s3.test", "temporary/key",
                Map.of("key", "temporary/key", "policy", "signed"));
        when(storageService.generateUploadUrl("proof.pdf", "application/pdf")).thenReturn(authorization);

        var response = orderService.generateAnnexUploadUrl(new AnnexUploadRequest("proof.pdf", "application/pdf"));

        assertEquals("https://s3.test", response.uploadUrl());
        assertEquals("temporary/key", response.fileKey());
        assertEquals("signed", response.fields().get("policy"));
    }

    @Test
    void create_savesOrderWithoutAnnexes() {
        givenUserAndOrderSave();
        var response = orderService.create(request(List.of()));

        assertEquals(23L, response.id());
        assertEquals(7L, response.userId());
        assertTrue(response.annexes().isEmpty());
        verify(storageService, never()).moveToOrder(anyString(), anyLong(), anyString());
        verify(workflowEngineService).instantiateWorkflowForOrder(any(Order.class));
    }

    @Test
    void create_movesAndPersistsSingleAnnexAtFinalKey() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf"))
                .thenReturn("orders/23/opaque/proof.pdf");

        var response = orderService.create(request(List.of(annex("proof.pdf", "temporary/proof"))));

        assertEquals(1, response.annexes().size());
        assertEquals("orders/23/opaque/proof.pdf", response.annexes().get(0).storageKey());
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, atLeastOnce()).save(orderCaptor.capture());
        Order persistedOrder = orderCaptor.getAllValues().get(orderCaptor.getValue().getAnnexes().isEmpty() ? 0 : orderCaptor.getAllValues().size() - 1);
        assertEquals("orders/23/opaque/proof.pdf", persistedOrder.getAnnexes().get(0).getUrl());
        assertSame(persistedOrder, persistedOrder.getAnnexes().get(0).getOrder());
    }

    @Test
    void create_movesAndPersistsEveryAnnex() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued(anyString())).thenReturn(true);
        when(storageService.moveToOrder("temporary/a", 23L, "a.pdf")).thenReturn("orders/23/a/a.pdf");
        when(storageService.moveToOrder("temporary/b", 23L, "b.pdf")).thenReturn("orders/23/b/b.pdf");

        var response = orderService.create(request(List.of(
                annex("a.pdf", "temporary/a"), annex("b.pdf", "temporary/b"))));

        assertEquals(List.of("orders/23/a/a.pdf", "orders/23/b/b.pdf"),
                response.annexes().stream().map(item -> item.storageKey()).toList());
        verify(storageService).moveToOrder("temporary/a", 23L, "a.pdf");
        verify(storageService).moveToOrder("temporary/b", 23L, "b.pdf");
    }

    @Test
    void create_checksUserAndKeysBeforeMovingFiles() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/a")).thenReturn(true);
        when(storageService.isTemporaryKeyIssued("temporary/b")).thenReturn(true);
        when(storageService.moveToOrder(anyString(), eq(23L), anyString()))
                .thenReturn("orders/23/a/file.pdf", "orders/23/b/file.pdf");

        orderService.create(request(List.of(annex("a.pdf", "temporary/a"), annex("b.pdf", "temporary/b"))));

        InOrder sequence = inOrder(userRepository, storageService);
        sequence.verify(userRepository).findById(7L);
        sequence.verify(storageService).isTemporaryKeyIssued("temporary/a");
        sequence.verify(storageService).isTemporaryKeyIssued("temporary/b");
        sequence.verify(storageService).moveToOrder("temporary/a", 23L, "a.pdf");
    }

    @Test
    void create_missingUserDoesNotCallStorageOrRepositories() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof")))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verifyNoInteractions(orderRepository, storageService, workflowEngineService);
    }

    @Test
    void create_invalidKeyDoesNotMoveOrPersist() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(storageService.isTemporaryKeyIssued("temporary/tampered")).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/tampered")))));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(orderRepository, never()).save(any(Order.class));
        verify(storageService, never()).moveToOrder(anyString(), anyLong(), anyString());
    }

    @Test
    void create_firstCopyFailureRollsBackAndLeavesNoFinalObject() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        RuntimeException failure = new RuntimeException("S3 copy failed");
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf")).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof")))));

        assertSame(failure, thrown);
        verify(storageService, never()).deleteObject(anyString());
    }

    @Test
    void create_laterCopyFailureDeletesEarlierFinalObject() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued(anyString())).thenReturn(true);
        when(storageService.moveToOrder("temporary/a", 23L, "a.pdf")).thenReturn("orders/23/a/a.pdf");
        when(storageService.moveToOrder("temporary/b", 23L, "b.pdf")).thenThrow(new RuntimeException("copy failed"));

        assertThrows(RuntimeException.class, () -> orderService.create(request(List.of(
                annex("a.pdf", "temporary/a"), annex("b.pdf", "temporary/b")))));

        verify(storageService).deleteObject("orders/23/a/a.pdf");
    }

    @Test
    void create_temporaryCleanupFailureDoesNotPersistOrder() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        RuntimeException failure = new RuntimeException("temporary cleanup failed");
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf")).thenThrow(failure);

        assertSame(failure, assertThrows(RuntimeException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof"))))));
        verify(workflowEngineService, never()).instantiateWorkflowForOrder(any(Order.class));
    }

    @Test
    void create_missingWorkflowDeletesFinalObjectsAndRollsBack() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf"))
                .thenReturn("orders/23/opaque/proof.pdf");
        RuntimeException missingWorkflow = new RuntimeException("No workflow template found");
        doThrow(missingWorkflow).when(workflowEngineService).instantiateWorkflowForOrder(any(Order.class));

        assertSame(missingWorkflow, assertThrows(RuntimeException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof"))))));
        verify(storageService).deleteObject("orders/23/opaque/proof.pdf");
    }

    @Test
    void create_orderSaveFailureDoesNotMoveFiles() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("database unavailable"));

        assertThrows(RuntimeException.class, () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof")))));

        verify(storageService, never()).moveToOrder(anyString(), anyLong(), anyString());
    }

    @Test
    void create_annexCascadeSaveFailureDeletesFinalObjects() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf"))
                .thenReturn("orders/23/opaque/proof.pdf");
        AtomicInteger saves = new AtomicInteger();
        doAnswer(invocation -> {
            if (saves.incrementAndGet() == 2) throw new RuntimeException("annex persistence failed");
            Order order = invocation.getArgument(0);
            order.setId(23L);
            return order;
        }).when(orderRepository).save(any(Order.class));

        assertThrows(RuntimeException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof")))));

        verify(storageService).deleteObject("orders/23/opaque/proof.pdf");
        verify(workflowEngineService, never()).instantiateWorkflowForOrder(any(Order.class));
    }

    @Test
    void create_stageSaveFailureDeletesFinalObjectsAndRollsBack() {
        givenUserAndOrderSave();
        when(storageService.isTemporaryKeyIssued("temporary/proof")).thenReturn(true);
        when(storageService.moveToOrder("temporary/proof", 23L, "proof.pdf"))
                .thenReturn("orders/23/opaque/proof.pdf");
        doThrow(new RuntimeException("stage persistence failed"))
                .when(workflowEngineService).instantiateWorkflowForOrder(any(Order.class));

        assertThrows(RuntimeException.class,
                () -> orderService.create(request(List.of(annex("proof.pdf", "temporary/proof")))));

        verify(storageService).deleteObject("orders/23/opaque/proof.pdf");
    }

    private void givenUserAndOrderSave() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) order.setId(23L);
            return order;
        });
    }

    private OrderRequest request(List<AnnexRequest> annexes) {
        return new OrderRequest(7L, Order.OrderType.INCLUSAO_DISCIPLINA, Map.of("reason", "test"), annexes);
    }

    private AnnexRequest annex(String name, String key) {
        return new AnnexRequest(name, "application/pdf", key);
    }
}
