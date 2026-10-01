package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowTemplateController.class)
@Import(SecurityConfig.class)
class WorkflowTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IWorkflowTemplateService workflowTemplateService;

    @InjectMocks
    private WorkflowTemplateController workflowTemplateController;

    @MockitoBean
    private CustomOidcUserService customOidcUserService;

    private WorkflowTemplateResponse response(Long id, Order.OrderType orderType) {
        return new WorkflowTemplateResponse(id, orderType, null);
    }

    @Test
    @DisplayName("createTemplate deve retornar 200 ao criar um template com sucesso")
    void createTemplate_Success() throws Exception {
        // Arrange
        WorkflowTemplateRequest requestDTO = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L, List.of());
        var response = response(1L, Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA);
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/workflow-template")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Workflow template created successfully"));
    }

    @Test
    @DisplayName("createTemplate deve retornar 400 ao ocorrer erro de validação")
    void createTemplate_ValidationError() throws Exception {
        // Arrange
        WorkflowTemplateRequest requestDTO = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, null, List.of());
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class))).thenThrow(new IllegalArgumentException("Invalid data"));

        // Act & Assert
        mockMvc.perform(post("/workflow-template")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Invalid data"));
    }
}
