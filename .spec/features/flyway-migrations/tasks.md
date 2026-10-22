# Tasks: Flyway migrations

> feature: flyway-migrations

## T-001 — Adicionar Flyway e configurar execução e baseline [concluida]
- Refs: US-001, AC-002, US-003, AC-003
- Arquivos: build.gradle, src/main/resources/application-prod.yaml, src/main/resources/application-dev.yaml, src/main/resources/application-test.yaml, onpspec.config.json
- Notas: Testes/desenvolvimento mantêm ddl-auto atual; compatibilizar Flyway com o ciclo de schema usado nos testes.

## T-002 — Criar migration inicial do schema [concluida]
- Refs: US-001, AC-001
- Arquivos: src/main/resources/db/migration/V1__initial_schema.sql
- Notas: Derivar todas as tabelas e relações das entidades JPA e respeitar a convenção física do Spring/Hibernate.

## T-003 — Verificar migration e configuração por testes anotados [concluida]
- Refs: US-001, AC-001, US-002, AC-002, US-003, AC-003
- Arquivos: test/flyway-migration.test.js
- Notas: Verificações automatizadas da migration e das opções de produção; validação contra PostgreSQL real depende de um banco disponível.

## T-032 — Adicionar migration incremental de departamento em cursos [concluida]
- Refs: US-001, AC-111
- Arquivos: src/main/resources/db/migration/V2__add_course_department.sql, test/flyway-migration.test.js, .spec/features/flyway-migrations/spec.md
- Notas: Preservar nulos para cursos existentes e adicionar FK para `org_units`.
