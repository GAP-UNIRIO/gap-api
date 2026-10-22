package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.InstitutionRequest;
import com.gap.api.Model.DTO.InstitutionResponse;
import com.gap.api.Model.Entity.Institution;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IInstitutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InstitutionController.class)
@Import(SecurityConfig.class)
class InstitutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IInstitutionService service;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    // Métodos auxiliares
    private InstitutionResponse response(Long id, String name) {
        return new InstitutionResponse(id, name, "UNIRIO", null, null, null, true, null);
    }

    private InstitutionRequest request(String name) {
        return new InstitutionRequest(name, "UNIRIO", Institution.AuthProviderType.LOCAL, Institution.SignatureProviderType.NONE, null, true);
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("@spec:AC-045 findAll deve retornar 200 e a lista de instituições")
    void findAll_returnsMappedList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                response(1L, "Universidade Federal do Estado do Rio de Janeiro")
        ));

        mockMvc.perform(get("/institutions").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("Universidade Federal do Estado do Rio de Janeiro"));
    }

    @Test
    @DisplayName("@spec:AC-046 findAll sem registros retorna lista vazia")
    void findAll_empty() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/institutions").with(oidcLogin()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@spec:AC-047 findById deve retornar 200 e a instituição quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, "UNIRIO"));

        mockMvc.perform(get("/institutions/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("UNIRIO"));
    }

    @Test
    @DisplayName("@spec:AC-048 findById inexistente retorna 404")
    void findById_notFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/institutions/99").with(oidcLogin())).andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-049 create deve retornar 201 e a instituição criada")
    void create_success() throws Exception {
        when(service.create(any(InstitutionRequest.class))).thenReturn(response(1L, "UNIRIO"));

        mockMvc.perform(post("/institutions")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("UNIRIO"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    @DisplayName("@spec:AC-050 create rejeita campos obrigatórios ausentes e limites excedidos")
    void create_invalidFields() throws Exception {
        String[] invalid = {
                "{\"name\":\" \",\"code\":\"x\",\"authProviderType\":\"LOCAL\",\"signatureProvider\":\"NONE\"}",
                "{\"name\":\"" + "n".repeat(256) + "\",\"code\":\"x\",\"authProviderType\":\"LOCAL\",\"signatureProvider\":\"NONE\"}",
                "{\"name\":\"n\",\"code\":\"" + "c".repeat(51) + "\",\"authProviderType\":\"LOCAL\",\"signatureProvider\":\"NONE\"}",
                "{\"name\":\"n\",\"code\":\"x\",\"signatureProvider\":\"NONE\"}",
                "{\"name\":\"n\",\"code\":\"x\",\"authProviderType\":\"LOCAL\"}"
        };
        for (String body : invalid) {
            mockMvc.perform(post("/institutions").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-051 create rejeita valores de provedores desconhecidos")
    void create_invalidProvider() throws Exception {
        String body = "{\"name\":\"N\",\"code\":\"x\",\"authProviderType\":\"UNKNOWN\",\"signatureProvider\":\"NONE\"}";
        mockMvc.perform(post("/institutions").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-052 create código duplicado retorna 409")
    void create_duplicateCode() throws Exception {
        when(service.create(any(InstitutionRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mockMvc.perform(post("/institutions").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request("N"))))
                .andExpect(status().isConflict());
    }

    // ---------- update ----------

    @Test
    @DisplayName("@spec:AC-054 update deve retornar 200 ao alterar a instituição")
    void update_success() throws Exception {
        when(service.update(eq(1L), any(InstitutionRequest.class))).thenReturn(response(1L, "UNIRIO Atualizada"));

        mockMvc.perform(put("/institutions/1")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("UNIRIO Atualizada"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("UNIRIO Atualizada"));
    }

    @Test
    @DisplayName("@spec:AC-057 update código duplicado retorna 409")
    void update_duplicateCode() throws Exception {
        when(service.update(eq(1L), any(InstitutionRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mockMvc.perform(put("/institutions/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request("N"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("@spec:AC-058 update inexistente retorna 404")
    void update_notFound() throws Exception {
        when(service.update(eq(99L), any(InstitutionRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/institutions/99").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request("N"))))
                .andExpect(status().isNotFound());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("@spec:AC-059 delete deve retornar 200 ao remover a instituição")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/institutions/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("@spec:AC-060 delete inexistente retorna 404")
    void delete_notFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).delete(99L);
        mockMvc.perform(delete("/institutions/99").with(oidcLogin()).with(csrf())).andExpect(status().isNotFound());
    }
}
