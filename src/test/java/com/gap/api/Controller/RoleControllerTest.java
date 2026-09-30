package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.RoleRequest;
import com.gap.api.Model.DTO.RoleResponse;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IRoleService;
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

@WebMvcTest(RoleController.class)
@Import(SecurityConfig.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IRoleService service;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    // Métodos auxiliares
    private RoleResponse response(Long id, String authority) {
        return new RoleResponse(id, authority);
    }

    private RoleRequest request(String authority) {
        return new RoleRequest(authority);
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve retornar 200 e a lista de papéis (roles)")
    void findAll_returnsMappedList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                response(1L, "Student")
        ));

        mockMvc.perform(get("/roles").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].authority").value("Student"));
    }

    @Test
    @DisplayName("findById deve retornar 200 e o papel quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, "Admin"));

        mockMvc.perform(get("/roles/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.authority").value("Admin"));
    }

    // ---------- create ----------

    @Test
    @DisplayName("create deve retornar 201 e o papel criado")
    void create_success() throws Exception {
        when(service.create(any(RoleRequest.class))).thenReturn(response(1L, "Professor"));

        mockMvc.perform(post("/roles")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Professor"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve retornar 200 ao alterar o papel")
    void update_success() throws Exception {
        when(service.update(eq(1L), any(RoleRequest.class))).thenReturn(response(1L, "Coordenador"));

        mockMvc.perform(put("/roles/1")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Coordenador"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.authority").value("Coordenador"));
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve retornar 200 ao remover o papel")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/roles/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }
}