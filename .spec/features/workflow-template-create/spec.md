# Especificação: criação e uso de templates de workflow

> feature: workflow-template-create
> status: implementada

## Contexto

O recurso atual expõe somente `POST /workflow-template` para criar templates. Não há endpoints de consulta, atualização ou exclusão. Templates definem etapas usadas pelo motor ao abrir pedidos: uma configuração pode ser padrão (`courseId=null`) ou específica de um curso.

## Histórias

### US-030 — Criar template de workflow

Como administrador, quero cadastrar etapas de aprovação por tipo de solicitação, com opção de especializar o fluxo para um curso.

#### AC-093 — Criar template e retornar resposta de sucesso
- **Dado** pedido válido para criar template
- **Quando** o cliente envia `POST /workflow-template`
- **Então** recebe HTTP 200, estado `success` e a resposta com id, tipo de pedido e curso

#### AC-094 — Criar template padrão
- **Dado** `courseId` nulo e ainda sem template padrão para o tipo de pedido
- **Quando** o cliente cria o template
- **Então** ele é salvo sem curso associado e a resposta retorna `course=null`

#### AC-095 — Criar template específico de curso
- **Dado** curso existente e ainda sem template específico para o tipo de pedido
- **Quando** o cliente cria o template com esse `courseId`
- **Então** o template fica associado ao curso e a resposta inclui o curso

#### AC-096 — Rejeitar curso inexistente
- **Dado** `courseId` informado sem curso correspondente
- **Quando** o cliente cria o template
- **Então** recebe HTTP 400 com erro `Invalid data`

#### AC-097 — Rejeitar template específico duplicado
- **Dado** template já existente para o mesmo tipo de pedido e curso
- **Quando** o cliente tenta criar outro
- **Então** recebe HTTP 400 com erro `Invalid data`

#### AC-098 — Rejeitar template padrão duplicado
- **Dado** template padrão existente para o tipo de pedido
- **Quando** o cliente tenta criar outro template padrão
- **Então** recebe HTTP 400 com erro `Invalid data`

#### AC-099 — Registrar as etapas fornecidas
- **Dado** lista de etapas com ordem, tipo, unidade responsável, nome e descrição
- **Quando** o template é criado
- **Então** cada etapa é associada ao template com os valores do pedido e nome/descrição persistidos

#### AC-100 — Exigir ao menos uma etapa
- **Dado** `steps` ausente, nulo ou vazio
- **Quando** o cliente envia `POST /workflow-template`
- **Então** recebe HTTP 400 e o template não é criado

#### AC-105 — Validar nome e descrição da etapa
- **Dado** etapa sem nome/descrição, com texto em branco ou acima de 255 caracteres
- **Quando** o cliente envia `POST /workflow-template`
- **Então** recebe HTTP 400 e não cria o template

#### AC-106 — Retornar etapas completas
- **Dado** template criado com etapas contendo ordem, tipo, unidade responsável, nome e descrição
- **Quando** o cliente recebe a resposta de `POST /workflow-template`
- **Então** `data.steps` contém esses campos para cada etapa

#### AC-110 — Resolver departamento no motor de workflow
- **Dado** etapa responsável por `DEPARTAMENTO` e curso associado à unidade departamento
- **Quando** o motor instancia o workflow do pedido
- **Então** a etapa pendente referencia o departamento associado ao curso

#### AC-112 — Rejeitar etapa sem departamento associado
- **Dado** template com etapa responsável por `DEPARTAMENTO` e curso sem departamento associado
- **Quando** o motor instancia o workflow do pedido
- **Então** responde HTTP 409 e não persiste etapas sem responsável

### US-031 — Instanciar workflow para pedido

Como sistema, quero aplicar o template apropriado para gerar as etapas de aprovação de um pedido.

#### AC-101 — Preferir template específico do curso
- **Dado** templates padrão e específico para o tipo do pedido e curso
- **Quando** o workflow é instanciado
- **Então** usa o template específico do curso

#### AC-102 — Usar template padrão como fallback
- **Dado** ausência de template específico e existência de template padrão
- **Quando** o workflow é instanciado
- **Então** usa o template padrão

#### AC-103 — Ordenar e criar etapas pendentes
- **Dado** template escolhido com etapas em ordem de entrada diferente da ordem numérica
- **Quando** o workflow é instanciado
- **Então** cria etapas em `stepOrder` crescente, todas pendentes e associadas ao pedido e às unidades correspondentes do curso

#### AC-104 — Informar indisponibilidade sem template
- **Dado** que não há template específico nem padrão para o tipo do pedido
- **Quando** o workflow é instanciado
- **Então** lança HTTP 503 e não persiste etapas

## Regras observadas

- O tipo do pedido e tipos de etapas/unidade são enums; valores JSON desconhecidos são rejeitados durante a desserialização.
- `courseId=null` identifica o template padrão; `courseId` preenchido identifica configuração específica. O motor tenta a específica primeiro e recorre à padrão.
- O serviço verifica duplicidade por tipo de pedido e curso (ou curso nulo). A migration não declara constraint única para essa combinação.
- `steps` precisa conter ao menos uma etapa; ausência, `null` ou lista vazia são rejeitados com HTTP 400 pela validação do DTO.
- Cada etapa contém `stepName` e `stepDescription`, ambos não brancos e limitados a 255 caracteres, alinhados às colunas obrigatórias da migration.
- O DTO não valida presença/nulidade do tipo de pedido, ordem ou campos internos das etapas.
- A criação retorna HTTP 200, sem `Location`, e não HTTP 201.
- A resposta inclui o objeto `Course` quando há curso e uma lista `steps` com ordem, tipo, unidade responsável, nome e descrição.
- No motor, etapas são ordenadas por `stepOrder`, recebem estado `PENDING` e unidade do curso escolhida conforme `responsibleUnitType`.
- O enum de unidade inclui `DEPARTAMENTO`; o motor usa o departamento associado ao curso.
- Rotas estão sujeitas à autenticação e CSRF globais.

## Fora de escopo

- CRUD de templates (somente criação está exposta).
- Definir um catálogo de workflows por instituição além da regra de template padrão e específico por curso.
- Adicionar novos tipos de unidade além de `DEPARTAMENTO` ou campos além de nome e descrição das etapas.

## Suposições

Nenhuma. As decisões de etapas obrigatórias, campos de etapa e associação de departamento foram confirmadas pelo usuário e registradas nos critérios acima.

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-007 | Se `steps` estiver ausente, nulo ou vazio, qual deve ser o comportamento? | respondida | É obrigatório criar o template com ao menos uma etapa; ausência, `null` ou lista vazia retornam HTTP 400. |
| Q-008 | Um template pode indicar `DEPARTAMENTO` como responsável, mas hoje o curso não guarda uma unidade de departamento. Devemos rejeitar esse tipo no template ou adicionar um vínculo de departamento ao curso? | respondida | Adicionar vínculo de departamento ao curso para que o motor possa resolver essa unidade. |
| Q-009 | Como alinhar a migration (`step_name`/`step_description` obrigatórios) à entidade/request/serviço? | respondida | Adicionar os campos ao modelo e ao request para que o serviço persista os valores; nomes/descrições passam a ser obrigatórios. |
| Q-010 | Os campos `stepName` e `stepDescription` devem também aparecer nas etapas da resposta de criação do template? | respondida | Sim. Os campos são obrigatórios e devem ser devolvidos para cada etapa. |
