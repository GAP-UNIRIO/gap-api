# Spec: Order approval decisions

> feature: order-approval-decisions
> status: implementada

## Contexto

Responsáveis pelas etapas de um fluxo precisam aprovar ou reprovar solicitações acadêmicas. A aprovação libera a próxima etapa; a reprovação encerra a solicitação com justificativa.

## Histórias

### US-052 — Decidir uma etapa de solicitação

Como responsável pela etapa atual, quero aprovar ou reprovar a solicitação, para que o fluxo avance ou seja encerrado com uma justificativa registrada.

#### AC-200 — Aprovação avança o fluxo

- **Dado** uma solicitação aberta com uma etapa pendente
- **Quando** o responsável aprova a etapa
- **Então** a etapa fica aprovada e a próxima etapa, se houver, pode ser decidida

#### AC-201 — Reprovação exige justificativa e encerra

- **Dado** uma solicitação com uma etapa pendente
- **Quando** o responsável reprova a etapa sem justificativa
- **Então** a reprovação é recusada e a solicitação permanece sem alteração

#### AC-202 — Reprovação justificada encerra a solicitação

- **Dado** uma solicitação com uma etapa pendente
- **Quando** o responsável reprova a etapa com justificativa
- **Então** a justificativa fica registrada, a etapa fica reprovada e a solicitação é encerrada como indeferida

#### AC-203 — Aprovar a última etapa não conclui a solicitação

- **Dado** uma solicitação cuja última etapa está pendente
- **Quando** um usuário da unidade responsável aprova essa etapa
- **Então** a etapa fica aprovada e a solicitação permanece em análise até a entrega do produto ao aluno

#### AC-204 — Somente usuário da unidade responsável decide a etapa

- **Dado** uma etapa pendente atribuída a uma unidade organizacional
- **Quando** um usuário sem vínculo com essa unidade tenta aprová-la ou reprová-la
- **Então** a decisão é recusada e a etapa permanece pendente

#### AC-205 — Apenas a etapa atual pode ser decidida

- **Dado** uma solicitação com etapas pendentes em ordem
- **Quando** um usuário tenta decidir uma etapa posterior antes da aprovação das anteriores
- **Então** a decisão é recusada e o fluxo permanece inalterado

#### AC-206 — Secretaria conclui manualmente após todas as aprovações

- **Dado** uma solicitação cujas etapas foram todas aprovadas
- **Quando** um usuário vinculado à unidade de secretaria registra a entrega do produto ao aluno e conclui a solicitação
- **Então** a solicitação passa para o status concluído

#### AC-207 — Administrador vincula usuários às unidades

- **Dado** um usuário existente e unidades organizacionais cadastradas
- **Quando** um administrador atualiza as unidades vinculadas ao usuário
- **Então** o vínculo fica salvo e pode autorizar decisões das etapas correspondentes

## Fora de escopo

- Enviar notificações ao solicitante ou a responsáveis.
- Alterar templates ou sua configuração de etapas.
- Aprovação automática ou conclusão automática após a última etapa.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-042 | A reprovação encerra a solicitação usando o status `INDEFERIDA`. | confirmada | Usuário confirmou que solicitações reprovadas são encerradas. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-042 | Como validar que o usuário autenticado é responsável pela unidade da etapa? Atualmente não há associação usuário-unidade no modelo. | respondida | Autorizar somente usuários vinculados à unidade responsável. |
| Q-043 | Autoriza adicionar campo e migration Flyway para armazenar a justificativa da reprovação? | respondida | Sim; armazenar a justificativa na entidade ApprovalStage. |
| Q-044 | A última aprovação deve encerrar a solicitação? | respondida | Não. A solicitação fica em análise até a entrega ao aluno; a secretaria conclui manualmente depois. |



