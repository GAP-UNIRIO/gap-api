# Especificação: CRUD de cursos

> feature: course-crud
> status: implementada

## Contexto

A API permite consultar, cadastrar, atualizar e excluir cursos. Um curso tem nome e vínculos com pessoas designadas como diretor, coordenador e secretário. Esta especificação registra o comportamento encontrado nos endpoints `/courses` e nos serviços associados.

## Histórias

### US-012 — Consultar cursos

Como consumidor da API, quero listar cursos e consultar um curso pelo identificador para conhecer os registros cadastrados.

#### AC-031 — Listar cursos por identificador

- **Dado** cursos cadastrados
- **Quando** o cliente envia `GET /courses`
- **Então** recebe HTTP 200, estado `success` e cursos ordenados por identificador crescente

#### AC-032 — Listar sem cursos cadastrados

- **Dado** que não há cursos cadastrados
- **Quando** o cliente envia `GET /courses`
- **Então** recebe HTTP 200, estado `success` e uma lista vazia

#### AC-033 — Consultar curso existente

- **Dado** um curso existente
- **Quando** o cliente envia `GET /courses/{id}`
- **Então** recebe HTTP 200, estado `success` e ao menos o identificador e o nome do curso

#### AC-044 — Retornar resumo dos responsáveis

- **Dado** um curso com diretor, coordenador e secretário associados
- **Quando** o cliente consulta o curso
- **Então** cada responsável é representado somente por identificador, nome e e-mail, sem serializar as demais propriedades ou relações do usuário

#### AC-034 — Informar curso inexistente na consulta

- **Dado** um identificador sem curso correspondente
- **Quando** o cliente envia `GET /courses/{id}`
- **Então** recebe HTTP 404

### US-013 — Cadastrar curso

Como gestor, quero cadastrar um curso com nome e responsáveis para manter a estrutura acadêmica disponível.

#### AC-035 — Criar curso

- **Dado** um nome disponível e identificadores de usuários para diretor, coordenador e secretário
- **Quando** o cliente envia `POST /courses`
- **Então** recebe HTTP 201, estado `success`, cabeçalho `Location` e identificador e nome do curso criado

#### AC-036 — Rejeitar nome duplicado na criação

- **Dado** que já existe um curso com o nome informado
- **Quando** o cliente tenta criar outro curso com esse nome
- **Então** recebe HTTP 409 e o registro existente permanece único

#### AC-037 — Rejeitar nome ausente

- **Dado** um corpo sem nome de curso
- **Quando** o cliente envia `POST /courses`
- **Então** a restrição de integridade do banco rejeita a operação e a API responde HTTP 409

### US-014 — Atualizar curso

Como gestor, quero alterar o nome e os responsáveis de um curso existente para manter seus dados atualizados.

#### AC-038 — Atualizar nome e responsáveis

- **Dado** um curso existente e identificadores válidos para os três responsáveis
- **Quando** o cliente envia `PUT /courses/{id}`
- **Então** recebe HTTP 200, estado `success` e o curso atualizado

#### AC-039 — Informar curso inexistente na atualização

- **Dado** um identificador sem curso correspondente
- **Quando** o cliente envia `PUT /courses/{id}`
- **Então** recebe HTTP 404 e nenhum curso é criado

#### AC-040 — Rejeitar nome duplicado na atualização

- **Dado** outro curso já cadastrado com o novo nome informado
- **Quando** o cliente atualiza o curso
- **Então** recebe HTTP 409 e o registro não substitui o outro curso

### US-015 — Excluir curso

Como gestor, quero excluir um curso que não deve mais permanecer cadastrado.

#### AC-041 — Excluir curso sem referências

- **Dado** um curso existente que não é referenciado por outros registros
- **Quando** o cliente envia `DELETE /courses/{id}`
- **Então** recebe HTTP 200, estado `success` e o curso é removido

#### AC-042 — Informar curso inexistente na exclusão

- **Dado** um identificador sem curso correspondente
- **Quando** o cliente envia `DELETE /courses/{id}`
- **Então** recebe HTTP 404

#### AC-043 — Rejeitar exclusão de curso referenciado

- **Dado** um curso referenciado por usuário, evento de formatura ou modelo de workflow
- **Quando** o cliente tenta excluí-lo
- **Então** a restrição de integridade impede a exclusão e a API responde HTTP 409

#### AC-107 — Exigir departamento ao cadastrar ou atualizar curso
- **Dado** requisição de criação ou atualização sem `departmentId`
- **Quando** o cliente envia `POST /courses` ou `PUT /courses/{id}`
- **Então** recebe HTTP 400 e o curso não é salvo

#### AC-108 — Associar departamento existente ao curso
- **Dado** `departmentId` de uma unidade existente do tipo `DEPARTAMENTO`
- **Quando** o cliente cria ou atualiza um curso
- **Então** a associação é persistida e a resposta contém o resumo do departamento (`id`, `name`)

#### AC-109 — Rejeitar unidade que não seja departamento
- **Dado** `departmentId` inexistente ou associado a unidade de outro tipo
- **Quando** o cliente cria ou atualiza um curso
- **Então** recebe HTTP 400 e nenhuma alteração é salva

## Regras observadas no código

- A listagem usa ordenação crescente por `id`.
- `name` é `NOT NULL` e único no banco. O DTO não aplica validação de campo; nome vazio ou composto por espaços não é rejeitado pelo código de validação da requisição.
- `directorId`, `coordinatorId` e `secretaryId` são usados pelo serviço para obter referências de usuário ao criar e atualizar o curso.
- A entidade também possui vínculos com unidades organizacionais e uma lista de usuários; os endpoints e DTOs atuais de curso não oferecem operações para administrar esses vínculos.
- `departmentId` é obrigatório nas requisições de criação e atualização e deve apontar para `OrgUnitType.DEPARTAMENTO`; respostas devolvem apenas identificador e nome da unidade.
- Cursos existentes antes da V2 podem permanecer sem departamento; essas associações devem ser preenchidas por uma atualização administrativa posterior. O fluxo que use etapa `DEPARTAMENTO` precisa de curso associado a essa unidade.
- Erros de integridade do banco são convertidos pelo handler global em HTTP 409.
- Os endpoints seguem a autenticação e a proteção CSRF definidas globalmente para a API.
- A mensagem de recurso inexistente atualmente menciona “Instituição”; a especificação define o status HTTP 404, sem fixar essa mensagem incorreta.

## Fora de escopo

- Administrar vínculos com unidades organizacionais ou listar usuários do curso.
- Validar se o usuário designado possui um papel específico de diretor, coordenador ou secretário.
- Alterar as regras globais de autenticação, CSRF ou tratamento de integridade.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-010 | Cursos existentes não têm um mapeamento automático confiável para departamentos. | confirmada | O usuário escolheu manter os cursos legados sem vínculo; novas criações e atualizações exigem departamento. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-006 | Qual formato deve ser devolvido para `director`, `coordinator` e `secretary`: resumo (id, nome e e-mail) ou entidade `User` completa? A entidade completa contém relações de volta para o curso. | respondida | Resumo contendo somente id, nome e e-mail. |
