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

## T-032 — Validar nomes de curso na requisição [concluida]
- Refs: US-013, AC-037, AC-118
- Arquivos: `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `.spec/features/course-crud/spec.md`
- Notas: Aplicar validação não vazia e cobrir criação e atualização por HTTP.

## T-033 — Expandir contrato para unidades organizacionais [pendente]
- Refs: US-016, AC-120, AC-121, AC-122
- Arquivos: `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`
- Notas: Adicionar IDs obrigatórios, resumos de resposta e cenários HTTP para vínculos e tipos incompatíveis.

## T-034 — Validar responsáveis obrigatórios e seus papéis [pendente]
- Refs: US-013, US-014, AC-119, US-017, AC-123, AC-124
- Arquivos: `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java`
- Notas: Resolver usuários explicitamente e rejeitar ausência, inexistência ou papel incompatível.

## T-035 — Resolver unidades organizacionais por tipo [pendente]
- Refs: US-016, AC-120, AC-121, AC-122
- Arquivos: `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java`
- Notas: Resolver todas as associações obrigatórias e garantir tipo específico.

## T-036 — Verificar e auditar regras novas [pendente]
- Refs: AC-037, AC-118, AC-119, AC-120, AC-121, AC-122, AC-123, AC-124
- Arquivos: `.spec/features/course-crud/spec.md`, `.spec/features/course-crud/tasks.md`, `.spec/verification/course-crud.json`
- Notas: Rodar verify e audit; atualizar prova e fechar tarefa somente com PASS.

## T-031 — Associar curso a departamento [concluida]
- Refs: AC-107, AC-108, AC-109
- Arquivos: `src/main/java/com/gap/api/Model/Entity/Course.java`, `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java`
- Notas: Novas criações/atualizações exigem uma unidade DEPARTAMENTO; cursos legados podem continuar sem vínculo.
