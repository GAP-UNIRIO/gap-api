# Tarefas: Departamento obrigatório nos cursos

> feature: course-department-compatibility

## T-053 — Definir recriação da base e validar estratégia de migration [concluida]
- Refs: US-050, AC-178, AC-179, AC-180
- Arquivos: .spec/features/course-department-compatibility/spec.md, src/main/resources/db/migration/V2__add_course_department.sql
- Notas: Base descartada pelo usuário; a base de teste foi recriada pelas migrations atualizadas, sem associação manual.

## T-054 — Impor NOT NULL em nova migration [concluida]
- Refs: US-049, AC-176, US-050, AC-178, AC-179
- Arquivos: src/main/resources/db/migration/V7__require_course_department.sql, src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java
- Notas: Migration V7 aplica SET NOT NULL após V6 sem backfill e preserva a FK. Testes focados de migration e integração PostgreSQL passaram.

## T-055 — Provar validação de departamento na API [concluida]
- Refs: US-049, AC-175, AC-177
- Arquivos: src/main/java/com/gap/api/Model/Entity/Course.java, src/main/java/com/gap/api/Model/DTO/CourseRequest.java, src/main/java/com/gap/api/Service/CourseService.java, src/test/java/com/gap/api/Controller/CourseControllerTest.java, src/test/java/com/gap/api/Service/CourseServiceTest.java
- Notas: Testes anotados cobrem ausência e unidade inválida; testes unitários usam Mockito. Campo JPA marcado nullable=false.

## T-056 — Atualizar documentação do CRUD e executar verificações [concluida]
- Refs: US-049, AC-175, AC-176, AC-177, US-050, AC-178, AC-179, AC-180
- Arquivos: .spec/features/course-crud/spec.md, .spec/features/course-department-compatibility/spec.md, .spec/features/course-department-compatibility/tasks.md, .spec/verification/course-department-compatibility.json
- Notas: Atualizar contrato do CRUD e registrar prova de cada critério. Auditoria global ainda apresenta falhas preexistentes fora desta feature.



