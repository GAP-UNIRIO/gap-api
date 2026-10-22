package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.CourseRequest;
import com.gap.api.Model.DTO.CourseResponse;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.User;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.ICourseService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.aMapWithSize;

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
        return new CourseResponse(id, name, null, null, null,
                new CourseResponse.OrgUnitSummary(40L, "Departamento"));
    }

    private Course course(Long id, String name) {
        return new Course(id, name, null, null, null, null, null, null, null, null, null, null);
    }

    private CourseRequest request(String name) {
        return new CourseRequest(name, 10L, 20L, 30L, 40L);
    }

    // ---------- findAll / findById ----------

    @Test
    @DisplayName("@spec:AC-031 findAll retorna 200 e cursos ordenados")
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
    @DisplayName("@spec:AC-032 findAll retorna lista vazia quando não há cursos")
    void findAll_empty() throws Exception {
        when(service.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/courses").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("@spec:AC-033 findById retorna 200 para curso existente")
    void findById_found() throws Exception {
        when(service.findById(1L)).thenReturn(CourseResponse.from(course(1L, "Sistemas de Informação"))); // Supondo ajuste no service para retornar DTO

        mockMvc.perform(get("/courses/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.name").value("Sistemas de Informação"));
    }

    @Test
    @DisplayName("@spec:AC-044 findById retorna somente resumo dos responsáveis")
    void findById_returnsResponsibleSummaries() throws Exception {
        Course course = course(1L, "Sistemas de Informação");
        User director = user(10L, "diretor@example.test", "Diretora", course);
        User coordinator = user(20L, "coordenador@example.test", "Coordenador", course);
        User secretary = user(30L, "secretaria@example.test", "Secretária", course);
        course.setDirector(director);
        course.setCoordinator(coordinator);
        course.setSecretary(secretary);
        when(service.findById(1L)).thenReturn(CourseResponse.from(course));

        mockMvc.perform(get("/courses/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.director.id").value(10))
                .andExpect(jsonPath("$.data.director.name").value("Diretora"))
                .andExpect(jsonPath("$.data.director.email").value("diretor@example.test"))
                .andExpect(jsonPath("$.data.director", aMapWithSize(3)))
                .andExpect(jsonPath("$.data.coordinator", aMapWithSize(3)))
                .andExpect(jsonPath("$.data.secretary", aMapWithSize(3)));
    }

    private User user(Long id, String email, String name, Course course) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setCourse(course);
        return user;
    }

    @Test
    @DisplayName("@spec:AC-034 findById retorna 404 para curso inexistente")
    void findById_notFound() throws Exception {
        when(service.findById(99L)).thenThrow(new org.springframework.web.server.ResponseStatusException(
                HttpStatus.NOT_FOUND, "Curso não encontrado: 99"));

        mockMvc.perform(get("/courses/99").with(oidcLogin()))
                .andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-035 @spec:AC-108 create retorna 201 com resumo do departamento")
    void create_success() throws Exception {
        when(service.create(any(CourseRequest.class))).thenReturn(response(1L, "Sistemas de Informação"));

        mockMvc.perform(post("/courses")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Sistemas de Informação"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.department.id").value(40L))
                .andExpect(jsonPath("$.data.department.name").value("Departamento"));
    }

    @Test
    @DisplayName("@spec:AC-036 create retorna 409 para nome duplicado")
    void create_duplicateName_returnsConflict() throws Exception {
        when(service.create(any(CourseRequest.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/courses").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Engenharia"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("@spec:AC-037 create rejeita nome ausente com 400")
    void create_missingName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/courses").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"directorId\":10,\"coordinatorId\":20,\"secretaryId\":30,\"departmentId\":40}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-118 create e update rejeitam nome vazio ou composto por espaços")
    void write_blankName_returnsBadRequest() throws Exception {
        String request = "{\"name\":\"   \",\"directorId\":10,\"coordinatorId\":20,\"secretaryId\":30,\"departmentId\":40}";
        mockMvc.perform(post("/courses").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/courses/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-107 create rejeita curso sem departmentId")
    void create_missingDepartment_returnsBadRequest() throws Exception {
        String request = "{\"name\":\"Curso\",\"directorId\":10,\"coordinatorId\":20,\"secretaryId\":30}";
        mockMvc.perform(post("/courses").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/courses/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
    }

    // ---------- update ----------

    @Test
    @DisplayName("@spec:AC-038 update retorna 200 ao alterar o curso")
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

    @Test
    @DisplayName("@spec:AC-039 update retorna 404 para curso inexistente")
    void update_notFound() throws Exception {
        when(service.update(eq(99L), any(CourseRequest.class))).thenThrow(
                new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: 99"));

        mockMvc.perform(put("/courses/99").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Nome novo"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-040 update retorna 409 para nome duplicado")
    void update_duplicateName_returnsConflict() throws Exception {
        when(service.update(eq(1L), any(CourseRequest.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(put("/courses/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Engenharia"))))
                .andExpect(status().isConflict());
    }

    // ---------- delete ----------

    @Test
    @DisplayName("@spec:AC-041 delete retorna 200 ao remover")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/courses/1")
                        .with(oidcLogin())
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(service).delete(1L);
    }

    @Test
    @DisplayName("@spec:AC-042 delete retorna 404 para curso inexistente")
    void delete_notFound() throws Exception {
        doThrow(new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: 99"))
                .when(service).delete(99L);

        mockMvc.perform(delete("/courses/99").with(oidcLogin()).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-043 delete retorna 409 quando o curso possui referências")
    void delete_referenced_returnsConflict() throws Exception {
        doThrow(new DataIntegrityViolationException("referenced"))
                .when(service).delete(1L);

        mockMvc.perform(delete("/courses/1").with(oidcLogin()).with(csrf()))
                .andExpect(status().isConflict());
    }
}
