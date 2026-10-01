package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.OrgUnitRequest;
import com.gap.api.Model.DTO.OrgUnitResponse;
import com.gap.api.Model.Entities.OrgUnit;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IOrgUnitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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
    @DisplayName("findAll deve retornar 200 e a lista de unidades organizacionais")
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
    @DisplayName("findById deve retornar 200 e a unidade quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, "CCET", OrgUnit.OrgUnitType.COORDENACAO));

        mockMvc.perform(get("/org-units/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("CCET"))
                .andExpect(jsonPath("$.data.type").value("COORDENACAO"));
    }

    // ---------- create ----------

    @Test
    @DisplayName("create deve retornar 201 e a unidade criada")
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

    // ---------- update ----------

    @Test
    @DisplayName("update deve retornar 200 ao alterar a unidade")
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

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve retornar 200 ao remover a unidade")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/org-units/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }
}