# Tarefas: integridade de templates de workflow

> feature: workflow-template-integrity

## T-051 — Validar campos obrigatórios e a ordem das etapas [concluida]
- Refs: US-046, AC-166, AC-167, AC-168
- Arquivos: src/main/java/com/gap/api/Model/DTO/WorkflowTemplateRequest.java, src/main/java/com/gap/api/Model/DTO/WorkflowStepRequest.java, src/main/java/com/gap/api/Service/WorkflowTemplateService.java, src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java, src/test/java/com/gap/api/Service/WorkflowTemplateServiceTest.java
- Notas: Cobrir criação e edição, incluindo que estado existente não muda quando a validação falha.

## T-052 — Garantir unicidade do escopo contra concorrência [concluida]
- Refs: US-046, AC-169, AC-170
- Arquivos: src/main/java/com/gap/api/Service/WorkflowTemplateService.java, src/main/resources/db/migration/V6__workflow_template_integrity.sql, test/flyway-migration.test.js, src/test/java/com/gap/api/Service/WorkflowTemplateServiceTest.java, src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java
- Notas: O `POST` mantém HTTP 400 e conflito concorrente em `PUT` retorna 409. A migration remove as etapas dos templates excedentes, apaga os templates de maior `id` e depois cria a proteção de unicidade.
