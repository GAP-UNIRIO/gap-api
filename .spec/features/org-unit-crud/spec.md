# Especificação: CRUD de unidades organizacionais

> feature: org-unit-crud
> status: implementada

## Contexto

A API administra unidades organizacionais em `/org-units`. Uma unidade possui tipo enum e nome; unidades podem ser referenciadas por cursos e etapas de aprovação.

## Histórias

### US-022 — Consultar unidades organizacionais

Como consumidor da API, quero listar unidades e consultar uma pelo identificador.

#### AC-068 — Listar unidades por id
- **Dado** unidades cadastradas
- **Quando** o cliente envia `GET /org-units`
- **Então** recebe HTTP 200, `success` e a lista em ordem crescente de id

#### AC-069 — Listar sem unidades
- **Dado** que não há unidades cadastradas
- **Quando** o cliente envia `GET /org-units`
- **Então** recebe HTTP 200 e uma lista vazia

#### AC-070 — Consultar unidade existente
- **Dado** uma unidade existente
- **Quando** o cliente envia `GET /org-units/{id}`
- **Então** recebe HTTP 200 e id, tipo e nome

#### AC-071 — Consultar unidade inexistente
- **Dado** um identificador sem unidade correspondente
- **Quando** o cliente envia `GET /org-units/{id}`
- **Então** recebe HTTP 404

### US-023 — Cadastrar unidade organizacional

Como gestor, quero cadastrar unidade com tipo e nome.

#### AC-072 — Criar unidade
- **Dado** tipo e nome válidos
- **Quando** o cliente envia `POST /org-units`
- **Então** recebe HTTP 201, `success`, `Location` e a unidade criada

#### AC-073 — Aceitar apenas tipos conhecidos
- **Dado** valor de tipo que não pertence ao enum
- **Quando** o cliente envia `POST /org-units`
- **Então** recebe HTTP 400

#### AC-074 — Rejeitar tipo ou nome nulo
- **Dado** tipo ou nome nulo
- **Quando** o serviço tenta persistir a unidade
- **Então** a restrição `NOT NULL` do banco rejeita a operação e a API retorna HTTP 409

### US-024 — Atualizar unidade organizacional

Como gestor, quero alterar tipo e nome de uma unidade existente.

#### AC-075 — Atualizar unidade existente
- **Dado** uma unidade existente e tipo/nome novos
- **Quando** o cliente envia `PUT /org-units/{id}`
- **Então** recebe HTTP 200 com tipo e nome atualizados

#### AC-076 — Atualizar unidade inexistente
- **Dado** um identificador sem unidade correspondente
- **Quando** o cliente envia `PUT /org-units/{id}`
- **Então** recebe HTTP 404 e nenhuma unidade é criada

### US-025 — Excluir unidade organizacional

Como gestor, quero excluir unidade que não seja mais necessária.

#### AC-077 — Excluir unidade sem referências
- **Dado** unidade existente não referenciada
- **Quando** o cliente envia `DELETE /org-units/{id}`
- **Então** recebe HTTP 200 e a unidade é removida

#### AC-078 — Excluir unidade inexistente
- **Dado** identificador sem unidade correspondente
- **Quando** o cliente envia `DELETE /org-units/{id}`
- **Então** recebe HTTP 404

#### AC-079 — Impedir exclusão de unidade referenciada
- **Dado** unidade referenciada por curso ou etapa de aprovação
- **Quando** o cliente tenta excluí-la
- **Então** a chave estrangeira impede a exclusão e a API retorna HTTP 409

## Regras observadas

- A listagem é ordenada por id crescente.
- Tipos aceitos: `REITORIA`, `COORDENACAO`, `DIRECAO`, `SECRETARIA`, `DEPARTAMENTO` e `ESCOLA`.
- O DTO não aplica validação de presença/tamanho em tipo e nome. `type` e `name` são `NOT NULL` no banco; valores nulos falham na persistência e a API trata violações de integridade como HTTP 409.
- Nome não possui restrição de unicidade no schema.
- As referências de curso (reitoria, coordenação, direção, secretaria e escola) e etapa de aprovação (`org_unit_id`) impedem a exclusão enquanto existirem.
- Rotas estão sujeitas à autenticação e CSRF globais.

## Fora de escopo

- Regras para nomes únicos, normalização ou limite de tamanho.
- Alterar tipos de unidade ou relações com cursos/etapas.
- Autorizações distintas das regras globais.

## Suposições

Nenhuma; a especificação descreve restrições e respostas observadas no código e schema.

## Perguntas em aberto

Nenhuma.
