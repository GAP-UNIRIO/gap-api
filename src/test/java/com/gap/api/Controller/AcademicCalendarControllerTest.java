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

    private static final int YEAR = LocalDate.now().getYear() + 1;
    private static final String FIRST_SEMESTER = YEAR + ".1";
    private static final String SECOND_SEMESTER = YEAR + ".2";
    private static final LocalDate START = LocalDate.of(YEAR, 2, 1);
    private static final LocalDate END = LocalDate.of(YEAR, 6, 30);
    private static final Map<String, LocalDate> DEADLINES = Map.of("matricula", LocalDate.of(YEAR, 3, 1));

    private LocalDate startFor(String semester) {
        return semester.endsWith(".2") ? LocalDate.of(YEAR, 8, 1) : START;
    }

    private LocalDate endFor(String semester) {
        return semester.endsWith(".2") ? LocalDate.of(YEAR, 12, 15) : END;
    }

    private Map<String, LocalDate> deadlinesFor(String semester) {
        return Map.of("matricula", semester.endsWith(".2")
                ? LocalDate.of(YEAR, 9, 1) : DEADLINES.get("matricula"));
    }

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
                id, semester, startFor(semester), endFor(semester),
                deadlinesFor(semester), Map.of("maxCreditos", 24), active
        );
    }

    private AcademicCalendarRequest request(String semester, boolean active) {
        return new AcademicCalendarRequest(
                semester, startFor(semester), endFor(semester),
                deadlinesFor(semester), Map.of("maxCreditos", 24), active
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
        AcademicCalendarRequest calendarRequest = request(FIRST_SEMESTER, true);
        AcademicCalendarResponse calendarResponse = response(1L, FIRST_SEMESTER, true);

        when(academicCalendarService.create(any(AcademicCalendarRequest.class))).thenReturn(calendarResponse);

        mockMvc.perform(post("/academic-calendars")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(calendarRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value(FIRST_SEMESTER));
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
    @DisplayName("@spec:AC-113 POST rejeita formato de semestre inválido")
    void create_invalidSemesterFormat_returnsBadRequest() throws Exception {
        var invalidRequest = request(YEAR + ".3", false);
        String body = objectMapper.writeValueAsString(invalidRequest);

        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/academic-calendars/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("@spec:AC-116 POST rejeita mapa de prazos ausente ou vazio")
    void create_missingOrEmptyDeadlines_returnsBadRequest() throws Exception {
        for (String deadlines : new String[]{"null", "{}"}) {
            String body = "{\"semester\":\"" + FIRST_SEMESTER + "\",\"startDate\":\"" + START
                    + "\",\"endDate\":\"" + END + "\",\"deadlines\":" + deadlines
                    + ",\"academicLimits\":{\"maxCreditos\":24}}";
            mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-116 POST rejeita prazo nulo e chave em branco")
    void create_invalidDeadlineEntry_returnsBadRequest() throws Exception {
        for (String deadlines : new String[]{"{\"matricula\":null}", "{\" \":\"" + START + "\"}"}) {
            String body = "{\"semester\":\"" + FIRST_SEMESTER + "\",\"startDate\":\"" + START
                    + "\",\"endDate\":\"" + END + "\",\"deadlines\":" + deadlines
                    + ",\"academicLimits\":{\"maxCreditos\":24}}";
            mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-117 POST rejeita limites ausentes, vazios ou negativos")
    void create_invalidAcademicLimits_returnsBadRequest() throws Exception {
        for (String limits : new String[]{"null", "{}", "{\"maxCreditos\":-1}",
                "{\"maxCreditos\":null}", "{\" \":1}"}) {
            String body = "{\"semester\":\"" + FIRST_SEMESTER + "\",\"startDate\":\"" + START
                    + "\",\"endDate\":\"" + END + "\",\"deadlines\":{\"matricula\":\""
                    + DEADLINES.get("matricula") + "\"},\"academicLimits\":" + limits + "}";
            mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-117 POST aceita chave desconhecida e limite zero")
    void create_unknownAcademicLimitWithZero_isAccepted() throws Exception {
        var validRequest = new AcademicCalendarRequest(FIRST_SEMESTER, START, END, DEADLINES,
                Map.of("futureLimit", 0), false);
        when(academicCalendarService.create(any(AcademicCalendarRequest.class)))
                .thenReturn(response(1L, FIRST_SEMESTER, false));

        mockMvc.perform(post("/academic-calendars").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("@spec:AC-024 PUT atualiza calendário")
    void update() throws Exception {
        var updateRequest = request(SECOND_SEMESTER, false);
        var updated = response(1L, SECOND_SEMESTER, false);
        when(academicCalendarService.update(1L, updateRequest)).thenReturn(updated);

        mockMvc.perform(put("/academic-calendars/1").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value(SECOND_SEMESTER));
    }

    @Test
    @DisplayName("@spec:AC-028 PUT de calendário inexistente retorna 404")
    void update_notFound() throws Exception {
        var updateRequest = request(SECOND_SEMESTER, false);
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
