package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowStepRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Model.Entity.Course;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
        return new WorkflowTemplateResponse(id, orderType, null, List.of());
    }

    @Test
    @DisplayName("@spec:AC-093 @spec:AC-095 @spec:AC-106 createTemplate retorna sucesso, curso e etapas completas")
    void createTemplate_Success() throws Exception {
        // Arrange
        WorkflowTemplateRequest requestDTO = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 1L,
                List.of(new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                        "Coordenação", "Análise pela coordenação")));
        Course course = new Course();
        course.setId(1L);
        course.setName("Sistemas de Informação");
        var response = new WorkflowTemplateResponse(1L, Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, course,
                List.of(new WorkflowTemplateResponse.WorkflowStepSummary(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                        OrgUnit.OrgUnitType.COORDENACAO, "Coordenação", "Análise pela coordenação")));
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/workflow-template")
                        .with(oidcLogin())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Workflow template created successfully"))
                .andExpect(jsonPath("$.data.course.id").value(1L))
                .andExpect(jsonPath("$.data.steps[0].stepName").value("Coordenação"))
                .andExpect(jsonPath("$.data.steps[0].stepDescription").value("Análise pela coordenação"));
    }

    @Test
    @DisplayName("@spec:AC-094 createTemplate padrão retorna course null")
    void createDefaultTemplate_returnsNullCourse() throws Exception {
        WorkflowTemplateRequest request = new WorkflowTemplateRequest(
                Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, null,
                List.of(new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                        "Coordenação", "Análise pela coordenação")));
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class)))
                .thenReturn(response(2L, request.orderType()));
        mockMvc.perform(post("/workflow-template").with(oidcLogin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.course").doesNotExist());
    }

    @Test
    @DisplayName("@spec:AC-096 createTemplate de curso inexistente retorna 400")
    void createTemplate_ValidationError() throws Exception {
        // Arrange
        WorkflowTemplateRequest requestDTO = new WorkflowTemplateRequest(Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, 99L,
                List.of(new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                        "Coordenação", "Análise pela coordenação")));
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

    @Test
    @DisplayName("@spec:AC-097 @spec:AC-098 duplicidade de template retorna 400")
    void createTemplate_duplicateSpecificAndDefault_returnsBadRequest() throws Exception {
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class)))
                .thenThrow(new IllegalArgumentException("Workflow template already exists"));
        String[] requests = {
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"courseId\":1,\"steps\":[]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"courseId\":null,\"steps\":[]}"
        };
        for (String request : requests) {
            mockMvc.perform(post("/workflow-template").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("error"));
        }
    }

    @Test
    @DisplayName("@spec:AC-100 createTemplate rejeita lista de etapas vazia ou omitida")
    void createTemplate_requiresAtLeastOneStep() throws Exception {
        String[] requests = {
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"courseId\":null,\"steps\":[]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"courseId\":null}"
        };
        for (String request : requests) {
            mockMvc.perform(post("/workflow-template").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("@spec:AC-105 createTemplate rejeita nome ou descrição de etapa inválidos")
    void createTemplate_requiresStepNameAndDescription() throws Exception {
        String[] requests = {
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"steps\":[{\"stepOrder\":1,\"stageType\":\"COORDINATOR\",\"responsibleUnitType\":\"COORDENACAO\"}]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"steps\":[{\"stepOrder\":1,\"stageType\":\"COORDINATOR\",\"responsibleUnitType\":\"COORDENACAO\",\"stepName\":\" \",\"stepDescription\":\"Descrição\"}]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"steps\":[{\"stepOrder\":1,\"stageType\":\"COORDINATOR\",\"responsibleUnitType\":\"COORDENACAO\",\"stepName\":\"Coordenação\",\"stepDescription\":\" \"}]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"steps\":[{\"stepOrder\":1,\"stageType\":\"COORDINATOR\",\"responsibleUnitType\":\"COORDENACAO\",\"stepName\":\"" + "x".repeat(256) + "\",\"stepDescription\":\"Descrição\"}]}",
                "{\"orderType\":\"APROVEITAMENTO_DE_DISCIPLINA\",\"steps\":[{\"stepOrder\":1,\"stageType\":\"COORDINATOR\",\"responsibleUnitType\":\"COORDENACAO\",\"stepName\":\"Coordenação\",\"stepDescription\":\"" + "x".repeat(256) + "\"}]}"
        };
        for (String request : requests) {
            mockMvc.perform(post("/workflow-template").with(oidcLogin()).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest());
        }
    }
}
