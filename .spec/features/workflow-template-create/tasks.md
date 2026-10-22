# Tarefas: criação e uso de templates de workflow

> feature: workflow-template-create

## T-028 — Mapear o contrato atual e lacunas de schema [concluida]
- Refs: US-030, AC-093, AC-094, AC-095, AC-096, AC-097, AC-098, AC-099, AC-100, AC-105, AC-106, US-031, AC-101, AC-102, AC-103, AC-104, AC-110, AC-112
- Arquivos: `.spec/features/workflow-template-create/spec.md`, `src/main/java/com/gap/api/Controller/WorkflowTemplateController.java`, `src/main/java/com/gap/api/Service/WorkflowTemplateService.java`, `src/main/java/com/gap/api/Service/WorkflowEngineService.java`, `src/main/java/com/gap/api/Model/DTO/WorkflowTemplateRequest.java`, `src/main/java/com/gap/api/Model/DTO/WorkflowStepRequest.java`, `src/main/java/com/gap/api/Model/Entity/WorkflowStep.java`, `src/main/resources/db/migration/V1__initial_schema.sql`
- Notas: Contrato documentado; perguntas Q-007 a Q-010 respondidas e mapeadas nos critérios de aceite.

## T-029 — Anotar e completar testes do contrato definido [concluida]
- Refs: US-030, AC-093, AC-094, AC-095, AC-096, AC-097, AC-098, AC-099, AC-100, AC-105, AC-106, US-031, AC-101, AC-102, AC-103, AC-104, AC-110, AC-112
- Arquivos: `src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java`, `src/test/java/com/gap/api/Service/WorkflowTemplateServiceTest.java`, `src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java`
- Notas: Cobrir criação padrão/específica, conflitos, mapeamento/resposta de etapas e resolução de DEPARTAMENTO no motor; Q-007 a Q-010 respondidas.

## T-030 — Verificar feature e auditoria CI [concluida]
- Refs: US-030, AC-093, AC-094, AC-095, AC-096, AC-097, AC-098, AC-099, AC-100, AC-105, AC-106, US-031, AC-101, AC-102, AC-103, AC-104, AC-110, AC-112
- Arquivos: `.spec/features/workflow-template-create/spec.md`, `.spec/features/workflow-template-create/tasks.md`, `.spec/verification/workflow-template-create.json`
- Notas: Rodar verify e auditoria após fechar as perguntas abertas e renovar provas obsoletas do runner compartilhado.
