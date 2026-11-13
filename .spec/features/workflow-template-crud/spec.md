# Spec: Cadastro completo de templates de workflow

> feature: workflow-template-crud
> status: em-implementacao

## Contexto

Hoje só existe `POST /workflow-template` (feature workflow-template-create): o administrador cria um fluxo de aprovação, mas não consegue consultá-lo, corrigi-lo nem removê-lo sem mexer no banco. Esta feature completa o cadastro com consulta, edição e exclusão. Fecha o item "Completar o cadastro de fluxos" da issue #22 (Fluxo de aprovação). O acesso já é restrito a `ROLE_ADMIN` pela feature access-control.

Editar ou excluir um template não afeta solicitações já abertas: as etapas de cada solicitação (`ApprovalStage`) são cópias criadas na abertura e não referenciam o template (ASM-017).

## Histórias

### US-043 — Consultar os templates de workflow

Como administrador, quero listar e consultar os fluxos de aprovação cadastrados, para saber por quais etapas cada tipo de solicitação passa.

#### AC-158 — Listar os templates

- **Dado** templates cadastrados
- **Quando** o cliente envia `GET /workflow-template`
- **Então** recebe HTTP 200 com todos os templates, cada um com tipo de solicitação, curso (ou `null` no padrão da instituição) e etapas ordenadas por `stepOrder`

#### AC-159 — Consultar um template

- **Dado** um template existente
- **Quando** o cliente envia `GET /workflow-template/{id}`
- **Então** recebe HTTP 200 com o template e suas etapas ordenadas por `stepOrder`

#### AC-160 — Informar template inexistente

- **Dado** um identificador sem template correspondente
- **Quando** o cliente consulta, edita ou exclui esse template
- **Então** recebe HTTP 404 com mensagem indicando que o template não foi encontrado

### US-044 — Editar um template de workflow

Como administrador, quero corrigir o tipo, o curso ou as etapas de um fluxo, para ajustá-lo sem recriar o cadastro.

#### AC-161 — Substituir os dados do template

- **Dado** um template existente e um pedido válido
- **Quando** o cliente envia `PUT /workflow-template/{id}`
- **Então** recebe HTTP 200 e o template passa a ter exatamente o tipo, o curso e as etapas enviados

#### AC-162 — Recusar edição que duplica outro template

- **Dado** outro template já cadastrado para o mesmo tipo de solicitação e curso
- **Quando** o cliente edita um template para essa mesma combinação
- **Então** recebe HTTP 409 e nada é alterado

#### AC-163 — Recusar curso inexistente na edição

- **Dado** um `courseId` sem curso correspondente
- **Quando** o cliente edita um template com esse curso
- **Então** recebe HTTP 404 e nada é alterado

#### AC-164 — Recusar etapas com ordem repetida

- **Dado** etapas com o mesmo `stepOrder`
- **Quando** o cliente cria ou edita um template
- **Então** recebe HTTP 400 com mensagem clara e nada é salvo

### US-045 — Excluir um template de workflow

Como administrador, quero remover um fluxo que não é mais usado, para que novas solicitações daquele tipo usem o fluxo padrão ou deixem de ser aceitas.

#### AC-165 — Excluir o template e suas etapas

- **Dado** um template existente
- **Quando** o cliente envia `DELETE /workflow-template/{id}`
- **Então** recebe HTTP 200 e o template e suas etapas são removidos

## Fora de escopo

- Prazo e regras por etapa (SLA, exige parecer, permite diligência): movido para a issue #26 (Fila e decisão), onde é usado.
- Hierarquia de unidades organizacionais: depende de decisão do time (item "se decidido" da #22).
- Mudar os códigos de erro do `POST` existente (hoje 400 `Invalid data` para duplicidade e curso inexistente, conforme AC-096 a AC-098).

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-017 | Editar ou excluir um template não altera solicitações já abertas, porque `ApprovalStage` copia tipo e unidade na abertura e não referencia o template. | confirmada | Verificado no modelo: `ApprovalStage` não tem chave para `workflow_templates`. |
| ASM-018 | A resposta passa a trazer o curso como resumo (`id` e `name`) em vez da entidade `Course` inteira, que tem relações preguiçosas e referência circular com `User` e quebraria a serialização nas consultas. O formato JSON de `course.id` é mantido. | aberta | Confirmar com o time que o front não usa outros campos de `course` nessa resposta. |

## Perguntas em aberto

Nenhuma.
