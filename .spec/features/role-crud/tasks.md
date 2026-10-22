# Tarefas: CRUD de papéis

> feature: role-crud

## T-025 — Especificar o CRUD e suas restrições [concluida]
- Refs: US-026, AC-080, AC-081, AC-082, AC-083, US-027, AC-084, AC-085, AC-086, US-028, AC-087, AC-088, AC-089, US-029, AC-090, AC-091, AC-092
- Arquivos: `.spec/features/role-crud/spec.md`, `src/main/java/com/gap/api/Controller/RoleController.java`, `src/main/java/com/gap/api/Service/RoleService.java`, `src/main/java/com/gap/api/Model/DTO/RoleRequest.java`, `src/main/java/com/gap/api/Model/Entity/Role.java`, `src/main/resources/db/migration/V1__initial_schema.sql`
- Notas: Registrar autoridade única e bloqueio de exclusão por associação a usuários.

## T-026 — Anotar e completar testes de HTTP e banco [concluida]
- Refs: US-026, AC-080, AC-081, AC-082, AC-083, US-027, AC-084, AC-085, AC-086, US-028, AC-087, AC-088, AC-089, US-029, AC-090, AC-091, AC-092
- Arquivos: `src/test/java/com/gap/api/Controller/RoleControllerTest.java`, `src/test/java/com/gap/api/Service/RoleServiceTest.java`, `src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java`, `test/run-onp-tests.mjs`
- Notas: Anotar casos de serviço existentes, completar HTTP de erros/validação e provar no banco o bloqueio por FK.

## T-027 — Verificar feature e auditar especificações [concluida]
- Refs: US-026, AC-080, AC-081, AC-082, AC-083, US-027, AC-084, AC-085, AC-086, US-028, AC-087, AC-088, AC-089, US-029, AC-090, AC-091, AC-092
- Arquivos: `.spec/features/role-crud/spec.md`, `.spec/features/role-crud/tasks.md`, `.spec/verification/role-crud.json`, `test/run-onp-tests.mjs`
- Notas: Rodar verify e audit CI; renovar provas dependentes do runner compartilhado.
