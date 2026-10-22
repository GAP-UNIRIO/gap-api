# Tarefas: CRUD de cursos

> feature: course-crud

## T-011 — Especificar comportamento atual do CRUD [concluida]
- Refs: US-012, AC-031, AC-032, AC-033, AC-034, AC-044, US-013, AC-035, AC-036, AC-037, US-014, AC-038, AC-039, AC-040, US-015, AC-041, AC-042, AC-043
- Arquivos: `.spec/features/course-crud/spec.md`, `src/main/java/com/gap/api/Controller/CourseController.java`, `src/main/java/com/gap/api/Service/CourseService.java`, `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/main/java/com/gap/api/Model/Entity/Course.java`
- Notas: Q-006 foi respondida; o contrato escolhido é resumo do responsável com id, nome e e-mail, divergente do tipo retornado atualmente.

## T-012 — Anotar e completar testes de serviço e HTTP [concluida]
- Refs: US-012, AC-031, AC-032, AC-033, AC-034, AC-044, US-013, AC-035, US-014, AC-038, AC-039, US-015, AC-041, AC-042
- Arquivos: `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`
- Notas: Adicionar `@spec:AC-xxx` aos casos existentes e testar erros 404 das rotas que ainda não têm cobertura HTTP.

## T-013 — Provar restrições do banco e conflitos HTTP [concluida]
- Refs: US-013, AC-036, AC-037, US-014, AC-040, US-015, AC-043
- Arquivos: `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java`, `test/run-onp-tests.mjs`
- Notas: Cobrir nome único/obrigatório, nome duplicado em atualização e exclusão bloqueada por referência; incluir classes de curso no runner.

## T-014 — Projetar responsáveis em formato de resumo [concluida]
- Refs: US-012, AC-044
- Arquivos: `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`
- Notas: Remover a serialização direta das entidades `User` e retornar somente id, nome e e-mail para cada responsável.

## T-015 — Verificar a feature e auditar as especificações [concluida]
- Refs: US-012, AC-031, AC-032, AC-033, AC-034, AC-044, US-013, AC-035, AC-036, AC-037, US-014, AC-038, AC-039, AC-040, US-015, AC-041, AC-042, AC-043
- Arquivos: `.spec/features/course-crud/spec.md`, `.spec/features/course-crud/tasks.md`, `.spec/verification/academic-calendar-crud.json`, `.spec/verification/codebase-baseline.json`, `.spec/verification/flyway-migrations.json`, `.spec/verification/order-create.json`
- Notas: Rodar verify para features cujas provas ficarem obsoletas e fechar com `onp-spec audit --ci`.

## T-031 — Associar curso a departamento [concluida]
- Refs: AC-107, AC-108, AC-109
- Arquivos: `src/main/java/com/gap/api/Model/Entity/Course.java`, `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java`
- Notas: Novas criações/atualizações exigem uma unidade DEPARTAMENTO; cursos legados podem continuar sem vínculo.
