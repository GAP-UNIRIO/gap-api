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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    // ---------- findById ----------

    @Test
    @DisplayName("GET /academic-calendars/{id} deve retornar calendário específico")
    void findById() throws Exception {
        AcademicCalendarResponse calendarResponse = response(1L, "2026.1", true);

        when(academicCalendarService.findById(1L)).thenReturn(calendarResponse);

        mockMvc.perform(get("/academic-calendars/1").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.semester").value("2026.1"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    // ---------- create ----------

    @Test
    @DisplayName("POST /academic-calendars deve criar calendário")
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
}