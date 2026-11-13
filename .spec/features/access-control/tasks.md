# Tasks: Controle de acesso por papel

> feature: access-control

## T-043 — Cadastrar e proteger os papéis do núcleo [concluida]
- Refs: US-039, AC-144, AC-154
- Arquivos: src/main/resources/db/migration/V5__seed_core_roles.sql, src/main/java/com/gap/api/Service/RoleService.java, src/main/java/com/gap/api/Model/Entity/Role.java
- Notas: V5 porque a V4 é da feature order-status. Papéis `ROLE_BASIC` e `ROLE_ADMIN` (Q-028).

## T-044 — Carregar os papéis no login e expor em /users/me [concluida]
- Refs: US-040, AC-145, AC-146, AC-147, AC-148
- Arquivos: src/main/java/com/gap/api/Service/CustomOidcUserService.java, src/main/java/com/gap/api/Model/DTO/UserResponse.java, src/main/java/com/gap/api/Repository/RoleRepository.java, src/main/resources/application.yaml, README.md
- Notas: Novo usuário recebe `ROLE_BASIC` (Q-029); e-mails de `APP_SECURITY_ADMIN_EMAILS` recebem `ROLE_ADMIN` (Q-030).

## T-045 — Proteger os endpoints por papel [concluida]
- Refs: US-041, AC-149, AC-150, AC-151, AC-152, AC-153
- Arquivos: src/main/java/com/gap/api/Config/SecurityConfig.java, .spec/features/api-docs/spec.md, src/test/java/com/gap/api/Integration/ApiDocsIntegrationTest.java
- Notas: Matriz do Contexto (Q-031). Sem login: 401 em JSON (Q-033); o AC-143 da api-docs, que esperava o redirecionamento, passa a esperar 401. Acesso negado: 403 em JSON.

## T-046 — Atribuir papéis a um usuário [concluida]
- Refs: US-042, AC-155, AC-156, AC-157
- Arquivos: src/main/java/com/gap/api/Controller/UserController.java, src/main/java/com/gap/api/Service/Interface/IUserService.java, src/main/java/com/gap/api/Service/UserService.java, src/main/java/com/gap/api/Model/DTO/UserRolesRequest.java
- Notas: `PUT /users/{id}/roles`, só administrador (Q-032).

## T-047 — Testar os critérios e registrar no gate [concluida]
- Refs: US-039, AC-144, AC-154, US-040, AC-145, AC-146, AC-147, AC-148, US-041, AC-149, AC-150, AC-151, AC-152, AC-153, US-042, AC-155, AC-156, AC-157
- Arquivos: src/test/java/com/gap/api/Controller/AccessControlTest.java, src/test/java/com/gap/api/Service/CustomOidcUserServiceTest.java, src/test/java/com/gap/api/Service/RoleServiceTest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Controller/UserControllerTest.java, src/test/java/com/gap/api/Controller/InstitutionControllerTest.java, src/test/java/com/gap/api/Controller/OrgUnitControllerTest.java, src/test/java/com/gap/api/Controller/CourseControllerTest.java, src/test/java/com/gap/api/Controller/RoleControllerTest.java, src/test/java/com/gap/api/Controller/AcademicCalendarControllerTest.java, src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java, test/access-control-migration.test.js, test/run-onp-tests.mjs
- Notas: Testes que falham escritos antes da implementação, com `@spec:AC-xxx` no nome. Os testes de controller existentes dos cadastros passam a logar como `ROLE_ADMIN`.
