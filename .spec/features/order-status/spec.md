# Spec: Status da solicitação

> feature: order-status
> status: implementada

## Contexto

Hoje uma `Order` é criada e não tem estado: não há como saber se ela está em análise, foi deferida ou cancelada. Esta feature dá à solicitação um status que só muda por transições permitidas, com cada mudança registrada no histórico. É a base para acompanhamento do aluno (#25), fila e decisão (#26) e notificações (#27). Faz parte da issue #22 (Fluxo de aprovação).

Status e transições da v1 (Q-020, Q-021 e Q-027):

| De | Para | Evento |
|---|---|---|
| ABERTA | EM_ANALISE | primeira etapa assumida |
| ABERTA | CANCELADA | aluno desistiu (Q-027) |
| EM_ANALISE | EM_DILIGENCIA | avaliador pediu complemento |
| EM_DILIGENCIA | EM_ANALISE | aluno respondeu |
| EM_DILIGENCIA | CANCELADA | prazo da diligência esgotado |
| EM_ANALISE | DEFERIDA | última etapa deferida |
| EM_ANALISE | INDEFERIDA | etapa indeferida |
| DEFERIDA | CONCLUIDA | efetivação confirmada |

Status finais: INDEFERIDA, CANCELADA, CONCLUIDA.

## Histórias

### US-036 — Controlar o estado da solicitação

Como servidor da secretaria, quero que cada solicitação tenha um status que só muda por transições permitidas, para saber em que ponto ela está e impedir saltos inválidos.

#### AC-132 — Criar a solicitação com o status inicial

- **Dado** um usuário autenticado e uma solicitação válida
- **Quando** o cliente envia `POST /orders`
- **Então** a solicitação é persistida com o status `ABERTA` (Q-021)
- **E** a resposta inclui o campo `status`

#### AC-133 — Aplicar uma transição permitida

- **Dado** uma solicitação em um status que admite a transição pedida
- **Quando** o serviço de status aplica a transição
- **Então** o status passa a ser o de destino
- **E** um registro é gravado em `OrderHistory` com status de origem, status de destino, autor e data

#### AC-134 — Recusar uma transição não permitida

- **Dado** uma solicitação em um status que não admite a transição pedida (ex.: DEFERIDA → ABERTA)
- **Quando** o serviço de status tenta aplicar a transição
- **Então** a operação é recusada com HTTP 422 (Q-025) e mensagem clara
- **E** o status e o histórico permanecem inalterados

#### AC-135 — Bloquear mudanças em status final

- **Dado** uma solicitação em status final (INDEFERIDA, CANCELADA ou CONCLUIDA)
- **Quando** qualquer transição é solicitada
- **Então** a operação é recusada com HTTP 422 e o status permanece o mesmo

#### AC-136 — Atribuir status às solicitações já existentes

- **Dado** solicitações gravadas antes desta feature, sem status
- **Quando** a migration da feature é aplicada
- **Então** todas recebem o status `EM_ANALISE` (Q-026) e a coluna passa a ser obrigatória

### US-037 — Desistir de uma solicitação ainda não analisada

Como aluno, quero cancelar uma solicitação que abri enquanto ela ainda não começou a ser analisada, para não ocupar a secretaria com um pedido de que não preciso mais.

#### AC-137 — Cancelar a própria solicitação aberta

- **Dado** um aluno autenticado e uma solicitação dele com status `ABERTA`
- **Quando** o cliente envia `PATCH /orders/{id}/cancel`
- **Então** recebe HTTP 200 com a solicitação no status `CANCELADA`
- **E** a mudança é registrada no histórico com o aluno como autor

#### AC-138 — Recusar o cancelamento fora do status aberta

- **Dado** uma solicitação do aluno em qualquer status diferente de `ABERTA`
- **Quando** o cliente envia `PATCH /orders/{id}/cancel`
- **Então** recebe HTTP 422 com mensagem clara e o status não muda

#### AC-139 — Impedir o cancelamento da solicitação de outra pessoa

- **Dado** um usuário autenticado e uma solicitação que pertence a outro usuário
- **Quando** o cliente envia `PATCH /orders/{id}/cancel`
- **Então** recebe HTTP 403 e o status não muda

#### AC-140 — Informar quando a solicitação não existe

- **Dado** um identificador que não corresponde a nenhuma solicitação
- **Quando** o cliente envia `PATCH /orders/{id}/cancel`
- **Então** recebe HTTP 404 com a indicação de que a solicitação não foi encontrada

## Fora de escopo

- Endpoints de decisão, fila de trabalho e diligência (#26); aqui só o serviço que aplica transições e o cancelamento pelo aluno.
- O gatilho de `ABERTA → EM_ANALISE` (aceite da 1ª etapa), que nasce com a fila de trabalho (#26); aqui a transição só fica disponível no serviço.
- Listagem e detalhe para o aluno (#25).
- Notificações disparadas pela mudança de status (#27).
- Texto exibido ao aluno por status (rótulo público), que fica no Acompanhamento (#25) conforme Q-024.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-013 | O status é um enum Java gravado como texto (`EnumType.STRING`), no mesmo padrão de `OrderType`, com o mapa de transições em código. | confirmada | Confirmado em Q-022. |
| ASM-014 | Cada mudança de status gera um registro em `OrderHistory` com `eventType = STATUS_CHANGED` e `payload` com origem e destino. | confirmada | Confirmado. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-020 | Lista de status da v1? | respondida | Aprovada a lista atual da tabela do Contexto. |
| Q-021 | Status inicial na criação? | respondida | Nasce como `ABERTA` e transiciona para `EM_ANALISE` no aceite da 1ª etapa. |
| Q-022 | Enum Java ou Tabela de Status? | respondida | Enum Java (`EnumType.STRING`) no banco + validação via código. |
| Q-023 | Status explícito ou derivado? | respondida | Explícito, armazenado na entidade `Order`. |
| Q-024 | Localização das mensagens para o aluno? | respondida | Escopo da issue de Acompanhamento (#25). |
| Q-025 | HTTP status para transição inválida? | respondida | `422 Unprocessable Entity`. |
| Q-026 | Status padrão na Migration? | respondida | `EM_ANALISE`. |
| Q-027 | Cancelamento pelo aluno? | respondida | Permitido para status `ABERTA`. |