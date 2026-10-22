package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IOrgUnitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
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

@WebMvcTest(OrgUnitController.class)
@Import(SecurityConfig.class)
class OrgUnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IOrgUnitService service;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    // Métodos auxiliares
    private OrgUnitResponse response(Long id, String name, OrgUnit.OrgUnitType type) {
        return new OrgUnitResponse(id, type, name);
    }

    private OrgUnitRequest request(OrgUnit.OrgUnitType type, String name) {
        return new OrgUnitRequest(type, name);
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("@spec:AC-068 findAll retorna lista de unidades organizacionais")
    void findAll_returnsMappedList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                response(1L, "CCET", OrgUnit.OrgUnitType.COORDENACAO)
        ));

        mockMvc.perform(get("/org-units").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("CCET"))
                .andExpect(jsonPath("$.data[0].type").value("COORDENACAO"));
    }

    @Test
    @DisplayName("@spec:AC-069 findAll retorna lista vazia")
    void findAll_empty() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/org-units").with(oidcLogin()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@spec:AC-070 findById retorna 200 e a unidade quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, "CCET", OrgUnit.OrgUnitType.COORDENACAO));

        mockMvc.perform(get("/org-units/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("CCET"))
                .andExpect(jsonPath("$.data.type").value("COORDENACAO"));
    }

    @Test
    @DisplayName("@spec:AC-071 findById inexistente retorna 404")
    void findById_notFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/org-units/99").with(oidcLogin())).andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-072 create retorna 201 e a unidade criada")
    void create_success() throws Exception {
        when(service.create(any(OrgUnitRequest.class))).thenReturn(response(1L, "CCET", OrgUnit.OrgUnitType.COORDENACAO));

        mockMvc.perform(post("/org-units")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(OrgUnit.OrgUnitType.COORDENACAO, "CCET"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("CCET"))
                .andExpect(jsonPath("$.data.type").value("COORDENACAO"));
    }

    @Test
    @DisplayName("@spec:AC-073 create rejeita tipo desconhecido")
    void create_invalidType() throws Exception {
        mockMvc.perform(post("/org-units").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"UNKNOWN\",\"name\":\"Unidade\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-074 create com tipo ou nome nulo retorna 409 por restrição do banco")
    void create_nullRequiredField_returnsConflict() throws Exception {
        org.mockito.Mockito.when(service.create(any(OrgUnitRequest.class)))
                .thenThrow(new DataIntegrityViolationException("not null"));
        mockMvc.perform(post("/org-units").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ESCOLA\",\"name\":null}"))
                .andExpect(status().isConflict());
    }

    // ---------- update ----------

    @Test
    @DisplayName("@spec:AC-075 update retorna 200 ao alterar a unidade")
    void update_success() throws Exception {
        when(service.update(eq(1L), any(OrgUnitRequest.class))).thenReturn(response(1L, "CCET Atualizado", OrgUnit.OrgUnitType.DIRECAO));

        mockMvc.perform(put("/org-units/1")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(OrgUnit.OrgUnitType.DIRECAO,"CCET Atualizado"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("CCET Atualizado"))
                .andExpect(jsonPath("$.data.type").value("DIRECAO"));
    }

    @Test
    @DisplayName("@spec:AC-076 update inexistente retorna 404")
    void update_notFound() throws Exception {
        when(service.update(eq(99L), any(OrgUnitRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/org-units/99").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request(OrgUnit.OrgUnitType.ESCOLA, "X"))))
                .andExpect(status().isNotFound());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("@spec:AC-077 delete retorna 200 ao remover a unidade")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/org-units/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("@spec:AC-078 delete inexistente retorna 404")
    void delete_notFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).delete(99L);
        mockMvc.perform(delete("/org-units/99").with(oidcLogin()).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-079 delete com referência retorna 409")
    void delete_referencedConflict() throws Exception {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("foreign key"))
                .when(service).delete(1L);
        mockMvc.perform(delete("/org-units/1").with(oidcLogin()).with(csrf())).andExpect(status().isConflict());
    }
}
