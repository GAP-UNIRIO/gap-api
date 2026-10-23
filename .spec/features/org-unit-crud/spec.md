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

#### AC-074 — Validar campos obrigatórios e nome
- **Dado** tipo ausente, nome ausente/em branco ou nome com mais de 255 caracteres
- **Quando** o cliente envia POST ou PUT para `/org-units`
- **Então** a API retorna HTTP 400 antes de persistir

#### AC-125 — Impedir nomes organizacionais duplicados globalmente
- **Dado** uma unidade já cadastrada com determinado nome
- **Quando** o cliente tenta cadastrar ou renomear outra unidade com o mesmo nome
- **Então** a API retorna HTTP 409 e mantém os dados existentes
- **E** a restrição única do banco impede duplicidade global, independentemente do tipo

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
- `type` é obrigatório. `name` é obrigatório, não pode ser branco e aceita até 255 caracteres, conforme o tamanho da coluna.
- `name` é único globalmente entre todos os tipos de unidade; a aplicação retorna HTTP 409 para duplicatas e o banco aplica uma restrição única como proteção final.
- As referências de curso (reitoria, coordenação, direção, secretaria e escola) e etapa de aprovação (`org_unit_id`) impedem a exclusão enquanto existirem.
- Rotas estão sujeitas à autenticação e CSRF globais.

## Fora de escopo

- Normalização de nomes para comparação (por exemplo, ignorar maiúsculas/minúsculas ou espaços periféricos).
- Alterar tipos de unidade ou relações com cursos/etapas.
- Autorizações distintas das regras globais.

## Suposições

Nenhuma; a especificação descreve restrições e respostas observadas no código e schema.

## Perguntas em aberto

Nenhuma.
