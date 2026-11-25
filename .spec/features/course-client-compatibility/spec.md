# Especificação: Compatibilidade de clientes do CRUD de cursos

> feature: course-client-compatibility
> status: rascunho

## Contexto

O contrato atual de `/courses` exige os cinco IDs de unidades organizacionais. Clientes antigos que enviam apenas nome, diretor, coordenador, secretário e departamento recebem HTTP 400. O dono do produto decidiu manter os cinco vínculos organizacionais obrigatórios para todos os clientes; esta feature documenta e prova esse contrato, incluindo a rejeição explícita de payloads antigos incompletos.

## Histórias

### US-051 — Manter vínculos organizacionais obrigatórios

Como mantenedor da API, quero exigir os vínculos organizacionais no cadastro e na atualização de cursos, para garantir que cursos geridos pela API contenham a estrutura institucional completa.

#### AC-181 — Rejeitar criação sem os cinco vínculos organizacionais

- **Dado** uma requisição de criação que omite um ou mais dos IDs de reitoria, coordenação, direção, secretaria e escola
- **Quando** o cliente envia `POST /courses`
- **Então** recebe HTTP 400 e nenhum curso é salvo

#### AC-182 — Rejeitar atualização sem os cinco vínculos organizacionais

- **Dado** um curso existente e uma requisição de atualização que omite um ou mais dos IDs de reitoria, coordenação, direção, secretaria e escola
- **Quando** o cliente envia `PUT /courses/{id}`
- **Então** recebe HTTP 400 e o curso não é alterado

#### AC-183 — Atualizar curso com todos os vínculos organizacionais

- **Dado** um curso existente e uma requisição com os cinco IDs de unidades existentes dos tipos correspondentes
- **Quando** o cliente envia `PUT /courses/{id}`
- **Então** recebe HTTP 200 e os cinco vínculos são atualizados e devolvidos no resumo do curso

## Regras

- `reitoriaId`, `coordenacaoId`, `direcaoId`, `secretariaId` e `escolaId` permanecem obrigatórios em `POST` e `PUT`, mesmo quando o cliente é antigo.
- A decisão mantém a regra registrada em Q-019 de `course-crud`; payloads antigos sem estes IDs continuam recebendo HTTP 400.
- `departmentId` também permanece obrigatório conforme o contrato vigente, mas não faz parte dos cinco vínculos organizacionais discutidos nesta decisão.
- Campos ausentes em `PUT` não preservam associações: a requisição é rejeitada pela validação antes de qualquer alteração.
- Associações explicitamente válidas são persistidas; unidade inexistente ou de tipo incompatível continua sendo rejeitada.
- Nenhuma alteração de schema ou migration é necessária para este contrato.

## Fora de escopo

- Aceitar payloads antigos sem os cinco IDs organizacionais.
- Preservar vínculos ausentes durante `PUT`.
- Tornar `departmentId` opcional ou alterar a obrigatoriedade de diretor, coordenador ou secretário.
- Alterar schema, migrations ou dados existentes.

## Suposições

Nenhuma.

## Perguntas em aberto

Nenhuma. A decisão de manter os cinco vínculos organizacionais obrigatórios foi confirmada pelo usuário.
