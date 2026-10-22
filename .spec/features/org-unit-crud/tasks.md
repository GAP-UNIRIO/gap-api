# Tarefas: CRUD de unidades organizacionais

> feature: org-unit-crud

## T-022 — Documentar contrato e relações existentes [concluida]
- Refs: US-022, AC-068, AC-069, AC-070, AC-071, US-023, AC-072, AC-073, AC-074, US-024, AC-075, AC-076, US-025, AC-077, AC-078, AC-079
- Arquivos: `.spec/features/org-unit-crud/spec.md`, `src/main/java/com/gap/api/Controller/OrgUnitController.java`, `src/main/java/com/gap/api/Service/OrgUnitService.java`, `src/main/java/com/gap/api/Model/DTO/OrgUnitRequest.java`, `src/main/java/com/gap/api/Model/Entity/OrgUnit.java`, `src/main/resources/db/migration/V1__initial_schema.sql`
- Notas: Restrições de exclusão e campos obrigatórios são definidas pelas chaves estrangeiras e `NOT NULL` do banco.

## T-023 — Mapear e completar testes HTTP/integração [concluida]
- Refs: US-022, AC-068, AC-069, AC-070, AC-071, US-023, AC-072, AC-073, AC-074, US-024, AC-075, AC-076, US-025, AC-077, AC-078, AC-079
- Arquivos: `src/test/java/com/gap/api/Controller/OrgUnitControllerTest.java`, `src/test/java/com/gap/api/Service/OrgUnitServiceTest.java`, `src/test/java/com/gap/api/Integration/OrgUnitServiceIntegrationTest.java`, `test/run-onp-tests.mjs`
- Notas: Anotar todos os critérios, cobrir erros HTTP e provar no banco os campos obrigatórios e referências que bloqueiam exclusão.

## T-024 — Verificar e auditar a feature [concluida]
- Refs: US-022, AC-068, AC-069, AC-070, AC-071, US-023, AC-072, AC-073, AC-074, US-024, AC-075, AC-076, US-025, AC-077, AC-078, AC-079
- Arquivos: `.spec/features/org-unit-crud/spec.md`, `.spec/features/org-unit-crud/tasks.md`, `.spec/verification/org-unit-crud.json`, `test/run-onp-tests.mjs`
- Notas: Rodar verify e audit CI e atualizar provas obsoletas do runner compartilhado.
