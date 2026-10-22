package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.AnnexRequest;
import com.gap.api.Model.DTO.AnnexResponse;
import com.gap.api.Model.DTO.AnnexUploadRequest;
import com.gap.api.Model.DTO.AnnexUploadResponse;
import com.gap.api.Model.DTO.OrderRequest;
import com.gap.api.Model.DTO.OrderResponse;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IOrderService orderService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    @Test
    @DisplayName("@spec:AC-010 retorna autorização para upload assinado")
    void upload_returnsSignedPostFields() throws Exception {
        var response = new AnnexUploadResponse("https://uploads-test.s3.us-east-1.amazonaws.com/",
                "temporary/opaque-key", Map.of("key", "temporary/opaque-key", "policy", "signed-policy"));
        when(orderService.generateAnnexUploadUrl(any(AnnexUploadRequest.class))).thenReturn(response);

        mockMvc.perform(post("/orders/annex-upload").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnnexUploadRequest("record.pdf", "application/pdf"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.uploadUrl").value(response.uploadUrl()))
                .andExpect(jsonPath("$.data.fileKey").value(response.fileKey()))
                .andExpect(jsonPath("$.data.fields.policy").value("signed-policy"));

        verify(orderService).generateAnnexUploadUrl(any(AnnexUploadRequest.class));
    }

    @Test
    @DisplayName("@spec:AC-011 rejeita metadados inválidos de upload")
    void upload_rejectsInvalidMetadata() throws Exception {
        mockMvc.perform(post("/orders/annex-upload").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnnexUploadRequest(" ", ""))))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).generateAnnexUploadUrl(any(AnnexUploadRequest.class));
    }

    @Test
    @DisplayName("@spec:AC-012 retorna 503 quando o armazenamento está indisponível")
    void upload_returns503WhenStorageUnavailable() throws Exception {
        when(orderService.generateAnnexUploadUrl(any(AnnexUploadRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Storage unavailable"));

        mockMvc.perform(post("/orders/annex-upload").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnnexUploadRequest("record.pdf", "application/pdf"))))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("@spec:AC-004 cria o pedido do usuário autenticado")
    void create_returns201WithPersistedOrder() throws Exception {
        OrderRequest request = request(List.of(new AnnexRequest("record.pdf", "application/pdf", "temporary/key")));
        OrderResponse response = new OrderResponse(23L, 7L, Order.OrderType.INCLUSAO_DISCIPLINA,
                ZonedDateTime.parse("2026-01-02T03:04:05Z"),
                List.of(new AnnexResponse(31L, "record.pdf", "application/pdf", "orders/23/31/record.pdf")));
        when(orderService.create(any(OrderRequest.class), eq("order-create@example.test"))).thenReturn(response);

        mockMvc.perform(post("/orders").with(authenticatedOidc()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.id").value(23))
                .andExpect(jsonPath("$.data.userId").value(7))
                .andExpect(jsonPath("$.data.annexes[0].storageKey").value("orders/23/31/record.pdf"));

        verify(orderService).create(any(OrderRequest.class), eq("order-create@example.test"));
    }

    @Test
    @DisplayName("@spec:AC-005 rejeita corpo inválido")
    void create_rejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/orders").with(authenticatedOidc()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest(null, null, null, null))))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).create(any(OrderRequest.class), any(String.class));
    }

    @Test
    @DisplayName("@spec:AC-006 retorna 404 se o usuário autenticado não estiver cadastrado")
    void create_returns404WhenUserDoesNotExist() throws Exception {
        when(orderService.create(any(OrderRequest.class), eq("order-create@example.test")))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/orders").with(authenticatedOidc()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(List.of()))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-008 rejeita chave temporária inválida")
    void create_returns400ForInvalidTemporaryKey() throws Exception {
        OrderRequest request = request(List.of(new AnnexRequest("record.pdf", "application/pdf", "temporary/tampered")));
        when(orderService.create(any(OrderRequest.class), eq("order-create@example.test")))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid temporary file key"));

        mockMvc.perform(post("/orders").with(authenticatedOidc()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-013 retorna 403 quando o pedido informa outro usuário")
    void create_returns403WhenRequestUserDiffersFromAuthenticatedUser() throws Exception {
        when(orderService.create(any(OrderRequest.class), eq("order-create@example.test")))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create an order for another user"));

        mockMvc.perform(post("/orders").with(authenticatedOidc()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(List.of()))))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor authenticatedOidc() {
        return oidcLogin().idToken(token -> token.claim("email", "order-create@example.test"));
    }

    private OrderRequest request(List<AnnexRequest> annexes) {
        return new OrderRequest(7L, Order.OrderType.INCLUSAO_DISCIPLINA, Map.of("reason", "test"), annexes);
    }
}

