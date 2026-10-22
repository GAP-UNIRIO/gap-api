# Tarefas: CRUD de calendário acadêmico

> feature: academic-calendar-crud

## T-007 — Especificar o contrato do CRUD existente [concluida]
- Refs: US-008, AC-015, AC-016, AC-017, US-009, AC-018, AC-019, AC-020, AC-021, AC-022, AC-023, US-010, AC-024, AC-025, AC-026, AC-027, AC-028, US-011, AC-029, AC-030
- Arquivos: `.spec/features/academic-calendar-crud/spec.md`, `src/main/java/com/gap/api/Controller/AcademicCalendarController.java`, `src/main/java/com/gap/api/Service/AcademicCalendarService.java`, `src/main/java/com/gap/api/Model/DTO/AcademicCalendarRequest.java`, `src/main/java/com/gap/api/Model/Entity/AcademicCalendar.java`
- Notas: Especificar o comportamento existente, sem introduzir validação de semestre futuro ou correspondência entre semestre e datas.

## T-008 — Mapear os testes atuais aos critérios cobertos [concluida]
- Refs: US-008, AC-016, US-009, AC-018
- Arquivos: `src/test/java/com/gap/api/Controller/AcademicCalendarControllerTest.java`, `src/test/java/com/gap/api/Service/AcademicCalendarServiceTest.java`
- Notas: Anotar casos existentes com `@spec:AC-xxx`; manter escopo separado da inclusão de novos cenários.

## T-009 — Completar testes de contrato HTTP e validação [concluida]
- Refs: US-008, AC-015, AC-017, US-009, AC-019, AC-020, AC-021, AC-023, US-010, AC-024, AC-027, AC-028, US-011, AC-029, AC-030
- Arquivos: `src/test/java/com/gap/api/Controller/AcademicCalendarControllerTest.java`, `src/test/java/com/gap/api/Service/AcademicCalendarServiceTest.java`
- Notas: Cobrir endpoints HTTP, campos obrigatórios e erros de domínio observados; não alterar regras funcionais.

## T-010 — Verificar a feature e atualizar o inventário rastreado [concluida]
- Refs: US-008, AC-015, AC-016, AC-017, US-009, AC-018, AC-019, AC-020, AC-021, AC-022, AC-023, US-010, AC-024, AC-025, AC-026, AC-027, AC-028, US-011, AC-029, AC-030
- Arquivos: `.spec/features/academic-calendar-crud/spec.md`, `.spec/features/academic-calendar-crud/tasks.md`, `.spec/features/codebase-baseline/tasks.md`, `.spec/verification/academic-calendar-crud.json`
- Notas: Rodar `verify` e `audit`; atualizar o inventário para mapear arquivos novos ou alterados conforme o mecanismo do projeto.
