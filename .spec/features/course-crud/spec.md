# Especificação: CRUD de cursos

> feature: course-crud
> status: em implementação

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

#### AC-037 — Rejeitar nome ausente na requisição

- **Dado** um corpo sem nome de curso
- **Quando** o cliente envia `POST /courses`
- **Então** a validação da requisição rejeita a operação e a API responde HTTP 400

#### AC-118 — Rejeitar nome vazio ou composto por espaços
- **Dado** uma requisição de criação ou atualização com `name` vazio ou contendo somente espaços
- **Quando** o cliente envia `POST /courses` ou `PUT /courses/{id}`
- **Então** recebe HTTP 400 e o curso não é salvo nem alterado

#### AC-119 — Exigir os três responsáveis
- **Dado** uma requisição de criação ou atualização sem um ou mais IDs de diretor, coordenador e secretário
- **Quando** o cliente envia `POST /courses` ou `PUT /courses/{id}`
- **Então** recebe HTTP 400 e o curso não é salvo nem alterado

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

### US-016 — Administrar unidades organizacionais do curso

Como gestor, quero associar unidades de reitoria, coordenação, direção, secretaria e escola ao curso para que os fluxos institucionais encontrem essas unidades.

#### AC-120 — Persistir e devolver vínculos organizacionais
- **Dado** IDs existentes e de tipos correspondentes para reitoria, coordenação, direção, secretaria e escola
- **Quando** o cliente cria ou atualiza um curso com esses IDs
- **Então** as cinco associações são persistidas e devolvidas como resumos com `id` e `name`

#### AC-121 — Rejeitar tipo de unidade incompatível
- **Dado** um ID existente de unidade cujo tipo não corresponde ao campo organizacional informado
- **Quando** o cliente cria ou atualiza um curso
- **Então** recebe HTTP 400 e nenhuma alteração é salva

#### AC-122 — Exigir os cinco vínculos organizacionais
- **Dado** uma requisição de criação ou atualização sem um ou mais IDs de reitoria, coordenação, direção, secretaria e escola
- **Quando** o cliente cria ou atualiza um curso
- **Então** recebe HTTP 400 e nenhuma associação é alterada

### US-017 — Validar responsáveis do curso

Como gestor, quero garantir que os usuários associados existem e exercem a função informada para evitar vínculos incorretos.

#### AC-123 — Rejeitar ID de responsável inexistente
- **Dado** um ID inexistente para diretor, coordenador ou secretário
- **Quando** o cliente cria ou atualiza um curso
- **Então** recebe HTTP 400 com mensagem que identifica a função e o ID inexistente, sem salvar alterações

#### AC-124 — Exigir papel correspondente ao responsável
- **Dado** um usuário existente sem o papel correspondente ao campo (`DIRECTOR`, `COORDINATOR` ou `SECRETARY`)
- **Quando** o cliente cria ou atualiza um curso
- **Então** recebe HTTP 400 com mensagem que identifica a função sem papel válido, sem salvar alterações

## Regras observadas no código

- A listagem usa ordenação crescente por `id`.
- `name` é obrigatório, não pode ser vazio ou composto somente por espaços e é único; a validação da requisição rejeita nome ausente ou em branco com HTTP 400.
- `directorId`, `coordinatorId` e `secretaryId` são obrigatórios e devem apontar para usuários existentes com o papel correspondente.
- Os campos `reitoriaId`, `coordenacaoId`, `direcaoId`, `secretariaId` e `escolaId` são obrigatórios nas novas requisições; devem referenciar unidades com tipo correspondente.
- `departmentId` é obrigatório nas requisições de criação e atualização e deve apontar para `OrgUnitType.DEPARTAMENTO`; respostas devolvem apenas identificador e nome da unidade.
- Cursos existentes antes da V2 podem permanecer sem departamento; essas associações devem ser preenchidas por uma atualização administrativa posterior. O fluxo que use etapa `DEPARTAMENTO` precisa de curso associado a essa unidade.
- Erros de integridade do banco são convertidos pelo handler global em HTTP 409.
- IDs inexistentes e associações organizacionais inválidas são rejeitados com HTTP 400; a mensagem identifica o vínculo inválido.
- Os endpoints seguem a autenticação e a proteção CSRF definidas globalmente para a API.
- A mensagem de recurso inexistente atualmente menciona “Instituição”; a especificação define o status HTTP 404, sem fixar essa mensagem incorreta.

## Fora de escopo

- Listar ou administrar os usuários matriculados no curso.
- Alterar as regras globais de autenticação, CSRF ou tratamento de integridade.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-010 | Cursos existentes não têm um mapeamento automático confiável para departamentos. | confirmada | O usuário escolheu manter os cursos legados sem vínculo; novas criações e atualizações exigem departamento. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-006 | Qual formato deve ser devolvido para `director`, `coordinator` e `secretary`: resumo (id, nome e e-mail) ou entidade `User` completa? A entidade completa contém relações de volta para o curso. | respondida | Resumo contendo somente id, nome e e-mail. |
| Q-013 | Os IDs de diretor, coordenador e secretário são obrigatórios? | respondida | Sim, os três são obrigatórios. |
| Q-014 | Qual resposta usar para IDs de responsáveis inexistentes? | respondida | HTTP 400 com mensagem específica para função e ID. |
| Q-015 | Validar se cada responsável tem o papel correspondente? | respondida | Sim; nomes oficiais de papel ainda precisam ser confirmados. |
| Q-016 | O CRUD deve receber e atualizar os vínculos organizacionais? | respondida | Sim, deve administrar reitoria, coordenação, direção, secretaria e escola. |
| Q-017 | Clientes antigos podem omitir `departmentId` em atualizações? | respondida | Não; a obrigatoriedade de `departmentId` deve ser mantida. |
| Q-018 | Quais são as authorities oficiais para os papéis de diretor, coordenador e secretário? | aberta | Recomenda-se `DIRECTOR`, `COORDINATOR` e `SECRETARY`; confirmar antes de implementar a validação. |
| Q-019 | Os vínculos organizacionais são obrigatórios? Como tratar campos omitidos em `PUT`? | respondida | As cinco associações são obrigatórias em `POST` e `PUT`; não há campos omitidos permitidos. |
