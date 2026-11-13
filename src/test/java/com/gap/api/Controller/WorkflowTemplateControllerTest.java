package com.gap.api.Controller;

import com.gap.api.Config.SecurityConfig;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Model.DTO.WorkflowTemplateRequest;
import com.gap.api.Model.DTO.WorkflowStepRequest;
import com.gap.api.Model.DTO.WorkflowTemplateResponse;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Service.CustomOidcUserService;
import com.gap.api.Service.Interface.IWorkflowTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkflowTemplateController.class)
@Import(SecurityConfig.class)
class WorkflowTemplateControllerTest {

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void allowCompletedUserForControllerTests() {
        when(userRepository.isCadasterCompleted(anyString())).thenReturn(true);
    }

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
        var course = new WorkflowTemplateResponse.CourseSummary(1L, "Sistemas de Informação");
        var response = new WorkflowTemplateResponse(1L, Order.OrderType.APROVEITAMENTO_DE_DISCIPLINA, course,
                List.of(new WorkflowTemplateResponse.WorkflowStepSummary(1, ApprovalStage.ApprovalStageType.COORDINATOR,
                        OrgUnit.OrgUnitType.COORDENACAO, "Coordenação", "Análise pela coordenação")));
        when(workflowTemplateService.createTemplate(any(WorkflowTemplateRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/workflow-template")
                        .with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
        mockMvc.perform(post("/workflow-template").with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf())
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
                        .with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
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
            mockMvc.perform(post("/workflow-template").with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf())
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
            mockMvc.perform(post("/workflow-template").with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf())
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
            mockMvc.perform(post("/workflow-template").with(oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content(request))
                    .andExpect(status().isBadRequest());
        }
    }

    // ---------- consulta, edição e exclusão (workflow-template-crud) ----------

    @Test
    @DisplayName("@spec:AC-158 GET lista os templates")
    void findAll_returns200() throws Exception {
        when(workflowTemplateService.findAll()).thenReturn(List.of(response(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA)));

        mockMvc.perform(get("/workflow-template").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].id").value(5))
                .andExpect(jsonPath("$.data[0].orderType").value("TRANCAMENTO_DE_MATRICULA"));
    }

    @Test
    @DisplayName("@spec:AC-159 GET por id retorna o template")
    void findById_returns200() throws Exception {
        when(workflowTemplateService.findById(5L)).thenReturn(response(5L, Order.OrderType.TRANCAMENTO_DE_MATRICULA));

        mockMvc.perform(get("/workflow-template/5").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(5));
    }

    @Test
    @DisplayName("@spec:AC-160 GET de template inexistente retorna 404")
    void findById_returns404() throws Exception {
        when(workflowTemplateService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Template de workflow não encontrado: 99"));

        mockMvc.perform(get("/workflow-template/99").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Template de workflow não encontrado: 99"));
    }

    @Test
    @DisplayName("@spec:AC-161 PUT atualiza o template")
    void update_returns200() throws Exception {
        when(workflowTemplateService.update(eq(5L), any(WorkflowTemplateRequest.class)))
                .thenReturn(response(5L, Order.OrderType.REVISAO_DE_PROVA));

        mockMvc.perform(put("/workflow-template/5").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.orderType").value("REVISAO_DE_PROVA"));
    }

    @Test
    @DisplayName("@spec:AC-162 PUT que duplica outro template retorna 409")
    void update_returns409() throws Exception {
        when(workflowTemplateService.update(eq(5L), any(WorkflowTemplateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um template para esse tipo de solicitação e curso."));

        mockMvc.perform(put("/workflow-template/5").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("@spec:AC-163 PUT com curso inexistente retorna 404")
    void update_courseNotFound_returns404() throws Exception {
        when(workflowTemplateService.update(eq(5L), any(WorkflowTemplateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado: 77"));

        mockMvc.perform(put("/workflow-template/5").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("@spec:AC-164 PUT com stepOrder repetido retorna 400")
    void update_duplicateStepOrder_returns400() throws Exception {
        when(workflowTemplateService.update(eq(5L), any(WorkflowTemplateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "As etapas não podem repetir o stepOrder."));

        mockMvc.perform(put("/workflow-template/5").with(admin()).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("As etapas não podem repetir o stepOrder."));
    }

    @Test
    @DisplayName("@spec:AC-165 DELETE remove o template")
    void delete_returns200() throws Exception {
        mockMvc.perform(delete("/workflow-template/5").with(admin()).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(workflowTemplateService).delete(5L);
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor admin() {
        return oidcLogin().idToken(token -> token.claim("email", "complete@example.test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private WorkflowTemplateRequest validRequest() {
        return new WorkflowTemplateRequest(Order.OrderType.REVISAO_DE_PROVA, null,
                List.of(new WorkflowStepRequest(1, ApprovalStage.ApprovalStageType.COORDINATOR, OrgUnit.OrgUnitType.COORDENACAO,
                        "Coordenação", "Análise pela coordenação")));
    }
}
