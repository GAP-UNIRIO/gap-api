# Tarefas: CRUD de instituições

> feature: institution-crud

## T-016 — Registrar o contrato existente [concluida]
- Refs: US-016, AC-045, AC-046, AC-047, AC-048, US-017, AC-049, AC-050, AC-051, AC-052, AC-053, US-018, AC-054, AC-055, AC-056, AC-057, AC-058, US-019, AC-059, AC-060
- Arquivos: `.spec/features/institution-crud/spec.md`, `src/main/java/com/gap/api/Controller/InstitutionController.java`, `src/main/java/com/gap/api/Service/InstitutionService.java`, `src/main/java/com/gap/api/Model/DTO/InstitutionRequest.java`, `src/main/java/com/gap/api/Model/DTO/InstitutionResponse.java`, `src/main/java/com/gap/api/Model/Entity/Institution.java`
- Notas: Documentar somente regras confirmadas no código; código identificado como CNPJ não recebe validação de formato.

## T-017 — Rastrear e completar testes de serviço e HTTP [concluida]
- Refs: US-016, AC-045, AC-046, AC-047, AC-048, US-017, AC-049, AC-050, AC-051, AC-052, AC-053, US-018, AC-054, AC-055, AC-056, AC-057, AC-058, US-019, AC-059, AC-060
- Arquivos: `src/test/java/com/gap/api/Controller/InstitutionControllerTest.java`, `src/test/java/com/gap/api/Service/InstitutionServiceTest.java`, `test/run-onp-tests.mjs`
- Notas: Todo critério deve ter caso anotado `@spec:AC-xxx`; acrescentar cobertura HTTP para validação e respostas de erro.

## T-018 — Verificar e auditar a feature [concluida]
- Refs: US-016, AC-045, AC-046, AC-047, AC-048, US-017, AC-049, AC-050, AC-051, AC-052, AC-053, US-018, AC-054, AC-055, AC-056, AC-057, AC-058, US-019, AC-059, AC-060
- Arquivos: `.spec/features/institution-crud/spec.md`, `.spec/features/institution-crud/tasks.md`, `.spec/verification/institution-crud.json`, `test/run-onp-tests.mjs`
- Notas: Rodar verify da feature e audit CI; atualizar qualquer prova tornada obsoleta pelo runner compartilhado.
