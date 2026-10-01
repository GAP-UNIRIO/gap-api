package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.ICourseService;
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

@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ICourseService service;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    private CourseResponse response(Long id, String name) {
        return new CourseResponse(id, name, null, null, null);
    }

    private Course course(Long id, String name) {
        return new Course(id, name, null, null, null, null);
    }

    private CourseRequest request(String name) {
        return new CourseRequest(name, 10L, 20L, 30L);
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("findAll deve retornar 200 e a lista de cursos")
    void findAll_returnsMappedList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                response(1L, "Sistemas de Informação"),
                response(2L, "Engenharia")
        ));

        mockMvc.perform(get("/courses").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].name").value("Sistemas de Informação"))
                .andExpect(jsonPath("$.data[1].name").value("Engenharia"));
    }

    @Test
    @DisplayName("findById deve retornar 200 e o curso quando existe")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(course(1L, "Sistemas de Informação")); // Supondo ajuste no service para retornar DTO

        mockMvc.perform(get("/courses/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Sistemas de Informação"));
    }

    // ---------- create ----------

    @Test
    @DisplayName("create deve retornar 201 e o curso criado")
    void create_success() throws Exception {
        when(service.create(any(CourseRequest.class))).thenReturn(response(1L, "Sistemas de Informação"));

        mockMvc.perform(post("/courses")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Sistemas de Informação"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    // ---------- update ----------

    @Test
    @DisplayName("update deve retornar 200 ao alterar o curso")
    void update_success() throws Exception {
        when(service.update(eq(1L), any(CourseRequest.class))).thenReturn(response(1L, "Nome novo"));

        mockMvc.perform(put("/courses/1")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Nome novo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Nome novo"));
    }

    // ---------- delete ----------

    @Test
    @DisplayName("delete deve retornar 200 ao remover")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/courses/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }
}