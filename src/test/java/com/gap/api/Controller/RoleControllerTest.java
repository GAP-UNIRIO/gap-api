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
    @DisplayName("@spec:AC-080 findAll retorna papéis ordenados")
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
    @DisplayName("@spec:AC-081 findAll sem papéis retorna lista vazia")
    void findAll_empty() throws Exception {
        when(service.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/roles").with(oidcLogin()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@spec:AC-082 findById retorna o papel quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(response(1L, "Admin"));

        mockMvc.perform(get("/roles/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.authority").value("Admin"));
    }

    @Test
    @DisplayName("@spec:AC-083 findById inexistente retorna 404")
    void findById_notFound() throws Exception {
        when(service.findById(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(get("/roles/99").with(oidcLogin())).andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-084 create retorna 201 e o papel criado")
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

    @Test
    @DisplayName("@spec:AC-085 create rejeita autoridade ausente, branca ou acima do limite")
    void create_invalidAuthority() throws Exception {
        String[] bodies = {
                "{}",
                "{\"authority\":\" \"}",
                "{\"authority\":\"" + "x".repeat(256) + "\"}"
        };
        for (String body : bodies) {
            mockMvc.perform(post("/roles").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-085 update rejeita autoridade branca")
    void update_blankAuthority() throws Exception {
        mockMvc.perform(put("/roles/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"authority\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-086 create com authority duplicada retorna 409")
    void create_duplicateAuthority() throws Exception {
        when(service.create(any(RoleRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mockMvc.perform(post("/roles").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ROLE_ADMIN"))))
                .andExpect(status().isConflict());
    }

    // ---------- update ----------

    @Test
    @DisplayName("@spec:AC-087 update retorna 200 ao alterar o papel")
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

    @Test
    @DisplayName("@spec:AC-088 update com authority de outro papel retorna 409")
    void update_duplicateAuthority() throws Exception {
        when(service.update(eq(1L), any(RoleRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT));
        mockMvc.perform(put("/roles/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ROLE_TAKEN"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("@spec:AC-089 update inexistente retorna 404")
    void update_notFound() throws Exception {
        when(service.update(eq(99L), any(RoleRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        mockMvc.perform(put("/roles/99").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("ROLE_NEW"))))
                .andExpect(status().isNotFound());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("@spec:AC-090 delete retorna 200 ao remover o papel")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/roles/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("@spec:AC-091 delete inexistente retorna 404")
    void delete_notFound() throws Exception {
        org.mockito.Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(service).delete(99L);
        mockMvc.perform(delete("/roles/99").with(oidcLogin()).with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-092 delete papel associado a usuário retorna 409")
    void delete_assignedRoleConflict() throws Exception {
        org.mockito.Mockito.doThrow(new org.springframework.dao.DataIntegrityViolationException("foreign key"))
                .when(service).delete(1L);
        mockMvc.perform(delete("/roles/1").with(oidcLogin()).with(csrf())).andExpect(status().isConflict());
    }
}
