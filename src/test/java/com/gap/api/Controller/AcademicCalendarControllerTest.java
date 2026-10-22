package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.AcademicCalendarRequest;
import com.gap.api.Model.DTO.AcademicCalendarResponse;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IAcademicCalendarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.nullValue;

@WebMvcTest(AcademicCalendarController.class)
@Import(SecurityConfig.class)
class AcademicCalendarControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IAcademicCalendarService academicCalendarService;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    // Métodos auxiliares
    private AcademicCalendarResponse response(Long id, String semester, boolean active) {
        return new AcademicCalendarResponse(
                id, semester, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30),
                Map.of(), Map.of(), active
        );
    }

    private AcademicCalendarRequest request(String semester, boolean active) {
        return new AcademicCalendarRequest(
                semester, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30),
                null, null, active
        );
    }

    private void stubNotFound(Long id) {
        when(academicCalendarService.findById(id)).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Calendário acadêmico não encontrado: " + id));
    }

    // ---------- findById ----------

    @Test
    @DisplayName("@spec:AC-016 GET /academic-calendars/{id} retorna calendário específico")
    void findById() throws Exception {
        AcademicCalendarResponse calendarResponse = response(1L, "2026.1", true);

        when(academicCalendarService.findById(1L)).thenReturn(calendarResponse);

        mockMvc.perform(get("/academic-calendars/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value("2026.1"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    @DisplayName("@spec:AC-015 GET /academic-calendars lista calendários")
    void findAll() throws Exception {
        when(academicCalendarService.findAll()).thenReturn(java.util.List.of(
                response(1L, "2025.2", false), response(2L, "2026.1", true)));

        mockMvc.perform(get("/academic-calendars").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].semester").value("2026.1"));
    }

    @Test
    @DisplayName("@spec:AC-017 GET de calendário inexistente retorna 404")
    void findById_notFound() throws Exception {
        stubNotFound(99L);

        mockMvc.perform(get("/academic-calendars/99").with(oidcLogin()))
                .andExpect(status().isNotFound());
    }

    // ---------- create ----------

    @Test
    @DisplayName("@spec:AC-018 POST /academic-calendars cria calendário válido")
    void create() throws Exception {
        AcademicCalendarRequest calendarRequest = request("2026.1", true);
        AcademicCalendarResponse calendarResponse = response(1L, "2026.1", true);

        when(academicCalendarService.create(any(AcademicCalendarRequest.class))).thenReturn(calendarResponse);

        mockMvc.perform(post("/academic-calendars")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(calendarRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value("2026.1"));
    }

    @Test
    @DisplayName("@spec:AC-019 POST rejeita semestre em branco")
    void create_blankSemester_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":\" \",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @DisplayName("@spec:AC-019 POST rejeita semestre ausente")
    void create_missingSemester_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"startDate\":\"2026-02-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-019 POST rejeita semestre acima de dez caracteres")
    void create_longSemester_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":\"12345678901\",\"startDate\":\"2026-02-01\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-019 POST rejeita data inicial ausente")
    void create_missingStartDate_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":\"2026.1\",\"endDate\":\"2026-06-30\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-019 POST rejeita data final ausente")
    void create_missingEndDate_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"semester\":\"2026.1\",\"startDate\":\"2026-02-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-024 PUT atualiza calendário")
    void update() throws Exception {
        var updateRequest = request("2026.2", false);
        var updated = response(1L, "2026.2", false);
        when(academicCalendarService.update(1L, updateRequest)).thenReturn(updated);

        mockMvc.perform(put("/academic-calendars/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value("2026.2"));
    }

    @Test
    @DisplayName("@spec:AC-028 PUT de calendário inexistente retorna 404")
    void update_notFound() throws Exception {
        var updateRequest = request("2026.2", false);
        when(academicCalendarService.update(99L, updateRequest)).thenThrow(
                new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Calendário acadêmico não encontrado: 99"));

        mockMvc.perform(put("/academic-calendars/99").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-029 DELETE remove calendário existente")
    void delete() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/academic-calendars/1").with(oidcLogin()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value(nullValue()));
    }

    @Test
    @DisplayName("@spec:AC-030 DELETE de calendário inexistente retorna 404")
    void delete_notFound() throws Exception {
        org.mockito.Mockito.doThrow(new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Calendário acadêmico não encontrado: 99"))
                .when(academicCalendarService).delete(99L);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/academic-calendars/99").with(oidcLogin()).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
