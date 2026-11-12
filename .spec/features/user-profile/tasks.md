# Tarefas: perfil e conclusão de cadastro do usuário

> feature: user-profile

## T-019 — Especificar os endpoints atuais de perfil [concluida]
- Refs: US-020, AC-061, AC-062, AC-063, US-021, AC-064, AC-065, AC-066, AC-067
- Arquivos: `.spec/features/user-profile/spec.md`, `src/main/java/com/gap/api/Controller/UserController.java`, `src/main/java/com/gap/api/Service/UserService.java`, `src/main/java/com/gap/api/Model/DTO/UserComplementRequest.java`, `src/main/java/com/gap/api/Model/DTO/UserResponse.java`, `src/main/java/com/gap/api/Model/Entity/User.java`, `src/main/java/com/gap/api/Service/CustomOidcUserService.java`
- Notas: O projeto não expõe CRUD administrativo de usuários; documentar as rotas de autoatendimento que existem.

## T-020 — Anotar e completar testes do perfil [concluida]
- Refs: US-020, AC-061, AC-062, AC-063, US-021, AC-064, AC-065, AC-066, AC-067
- Arquivos: `src/test/java/com/gap/api/Controller/UserControllerTest.java`, `src/test/java/com/gap/api/Service/UserServiceTest.java`, `test/run-onp-tests.mjs`
- Notas: Mapear casos existentes, adicionar respostas HTTP 404 e cobrir composição do indicador de cadastro completo.

## T-022 — Validar matrícula e preservar compatibilidade do contrato [pendente]
- Refs: AC-132, AC-133, AC-134
- Arquivos: `.spec/features/user-profile/spec.md`, `src/main/java/com/gap/api/Model/DTO/UserComplementRequest.java`, `src/main/java/com/gap/api/Controller/UserController.java`, `src/main/java/com/gap/api/Service/UserService.java`, `src/test/java/com/gap/api/Controller/UserControllerTest.java`, `README.md`
- Notas: aceitar nome JSON corrigido e alias legado, validar matrícula no limite HTTP e documentar regra sem fixar comprimento não confirmado.

## T-023 — Verificar feature e auditar especificações [pendente]
- Refs: US-020, AC-061, AC-062, AC-063, US-021, AC-064, AC-065, AC-066, AC-067, AC-132, AC-133, AC-134, US-022, AC-135, AC-136
- Arquivos: `.spec/features/user-profile/spec.md`, `.spec/features/user-profile/tasks.md`, `.spec/verification/user-profile.json`, `test/run-onp-tests.mjs`
- Notas: Rodar verify e audit CI; renovar provas de outras features se o runner compartilhado mudar.

## T-024 — Exigir cadastro completo para acessar recursos protegidos [concluida]
- Refs: US-022, AC-135, AC-136
- Arquivos: `src/main/java/com/gap/api/Config/SecurityConfig.java`, `src/main/java/com/gap/api/Repository/UserRepository.java`, `src/test/java/com/gap/api/Controller/AcademicCalendarControllerTest.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Controller/InstitutionControllerTest.java`, `src/test/java/com/gap/api/Controller/OrderControllerTest.java`, `src/test/java/com/gap/api/Controller/OrgUnitControllerTest.java`, `src/test/java/com/gap/api/Controller/RoleControllerTest.java`, `src/test/java/com/gap/api/Controller/WorkflowTemplateControllerTest.java`
- Notas: consultar matrícula e curso persistidos por e-mail OIDC em cada requisição protegida; manter consulta e complementação do próprio perfil acessíveis; responder 403 com BaseResponse.
