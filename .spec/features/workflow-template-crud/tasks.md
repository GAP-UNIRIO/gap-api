# Tasks: Cadastro completo de templates de workflow

> feature: workflow-template-crud

## T-048 — Consultar templates e resumir o curso na resposta [concluida]
- Refs: US-043, AC-158, AC-159, AC-160
- Arquivos: src/main/java/com/gap/api/Model/DTO/WorkflowTemplateResponse.java, src/main/java/com/gap/api/Service/Interface/IWorkflowTemplateService.java, src/main/java/com/gap/api/Service/WorkflowTemplateService.java, src/main/java/com/gap/api/Controller/WorkflowTemplateController.java
- Notas: `GET /workflow-template` e `GET /workflow-template/{id}`; etapas ordenadas por `stepOrder`; curso como resumo (ASM-018).

## T-049 — Editar e excluir templates [concluida]
- Refs: US-044, AC-160, AC-161, AC-162, AC-163, AC-164, US-045, AC-165
- Arquivos: src/main/java/com/gap/api/Service/Interface/IWorkflowTemplateService.java, src/main/java/com/gap/api/Service/WorkflowTemplateService.java, src/main/java/com/gap/api/Controller/WorkflowTemplateController.java
- Notas: `PUT` substitui tipo, curso e etapas; `DELETE` remove o template e as etapas (cascade). Validação de `stepOrder` repetido também no `POST`.

## T-050 — Testar os critérios e registrar no gate [concluida]
- Refs: US-043, AC-158, AC-159, AC-160, US-044, AC-161, AC-162, AC-163, AC-164, US-045, AC-165
- Arquivos: src/test/java/com/gap/api/Service/WorkflowTemplateServiceTest.java, src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java
- Notas: As duas classes já estão no `run-onp-tests.mjs`.
