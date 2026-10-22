# Spec: Flyway migrations

> feature: flyway-migrations
> status: implementada

## Contexto

A API GAP usa PostgreSQL e atualmente depende do Hibernate para atualizar o schema. A equipe precisa versionar a estrutura derivada das entidades e impedir alterações automáticas no banco de produção.

## Histórias

### US-001 — Versionar o schema PostgreSQL

Como equipe responsável pela API, queremos migrations Flyway que representem as entidades JPA, para que bancos novos recebam o schema e a evolução estrutural seja rastreável.

#### AC-001 — Migration inicial representa as entidades

- **Dado** um banco PostgreSQL vazio
- **Quando** a aplicação inicia com Flyway habilitado
- **Então** a migration inicial cria as tabelas, colunas, chaves, restrições e tipos PostgreSQL correspondentes às entidades, incluindo JSONB, enums textuais e relações

### US-002 — Preservar bancos existentes em produção

Como responsável pela implantação, quero adotar os bancos existentes por baseline, para manter os dados e evitar recriação do schema.

#### AC-002 — Baseline registra schema existente

- **Dado** um banco existente não vazio cujo schema corresponde às entidades atuais
- **Quando** Flyway inicia com baseline automático na versão inicial
- **Então** Flyway registra o baseline sem executar DDL destrutivo nem alterar os dados existentes

### US-003 — Impedir DDL automático em produção

Como responsável pela produção, quero que o Hibernate valide em vez de alterar o schema, para que somente migrations versionadas modifiquem a estrutura.

#### AC-003 — Hibernate não altera schema produtivo

- **Dado** o perfil de produção
- **Quando** a aplicação inicia
- **Então** Hibernate usa `ddl-auto: validate` e Flyway gerencia as migrations

#### AC-111 — Evoluir cursos com departamento sem perder registros legados
- **Dado** banco existente baselineado em V1
- **Quando** Flyway aplica V2
- **Então** adiciona `courses.department_id` anulável e sua chave estrangeira para `org_units`, preservando cursos já cadastrados

## Fora de escopo

- Desligar `ddl-auto` nos perfis de desenvolvimento e teste.
- Migração automática de dados de negócio para atribuir departamentos a cursos existentes.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-001 | O schema dos bancos existentes está alinhado às entidades atuais; o baseline registra essa versão sem reconciliar diferenças. | confirmada | O usuário escolheu baseline para bancos existentes. |
| ASM-002 | A versão 1 identifica o schema atual: bancos vazios executam V1 e bancos não vazios são baselineados na versão 1. | confirmada | Estratégia compatível com baseline informado pelo usuário. |
| ASM-003 | Convenções físicas seguem o naming strategy padrão Spring/Hibernate; campos sem tamanho explícito usam varchar(255), exceto texto e jsonb declarados. | confirmada | Modelo SQL derivado dos mapeamentos JPA e convenções padrão do projeto. |

## Perguntas em aberto

Nenhuma.
