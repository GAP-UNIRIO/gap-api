# Especificação: integridade de templates de workflow

> feature: workflow-template-integrity
> status: em-implementacao

## Contexto

Os endpoints de criação e edição aceitam campos de enum nulos e `stepOrder` não positivo. A aplicação rejeita ordens repetidas em uma requisição, mas a consulta prévia de templates duplicados não protege contra duas gravações concorrentes. Esta feature define as garantias de entrada e persistência para administradores que mantêm os fluxos de aprovação.

## Histórias

### US-046 — Manter templates de workflow íntegros

Como administrador, quero que cada template tenha tipos e etapas válidos e não conflite com outro template do mesmo escopo, para que novos pedidos usem um fluxo consistente.

#### AC-166 — Exigir os tipos do template e das etapas

- **Dado** pedido de criação ou edição com `orderType`, `stageType` ou `responsibleUnitType` ausente ou nulo
- **Quando** o administrador envia o pedido
- **Então** recebe HTTP 400 com mensagem de validação clara e nenhuma alteração é persistida

#### AC-167 — Exigir ordem positiva para cada etapa

- **Dado** pedido de criação ou edição com etapa cujo `stepOrder` seja zero ou negativo
- **Quando** o administrador envia o pedido
- **Então** recebe HTTP 400 com mensagem clara e nenhuma alteração é persistida

#### AC-168 — Rejeitar ordens repetidas no mesmo template

- **Dado** pedido de criação ou edição com duas etapas que tenham o mesmo `stepOrder`
- **Quando** o administrador envia o pedido
- **Então** recebe HTTP 400 com mensagem clara e nenhuma alteração é persistida

#### AC-169 — Impedir templates duplicados mesmo em gravações concorrentes

- **Dado** dois pedidos simultâneos para criar templates com o mesmo tipo de solicitação e o mesmo escopo (curso específico ou padrão)
- **Quando** ambos tentam persistir
- **Então** no máximo um template é criado e o `POST` perdedor recebe HTTP 400 com o envelope de erro atual; uma edição concorrente (`PUT`) que colida responde HTTP 409

#### AC-170 — Manter o template de menor id ao remover duplicatas legadas

- **Dado** banco com mais de um template para o mesmo tipo e escopo antes da migration de unicidade
- **Quando** Flyway aplica a migration
- **Então** mantém o template de menor `id` em cada combinação, remove os demais e suas etapas, e cria a proteção de unicidade

## Regras observadas

- `courseId=null` identifica um template padrão; curso preenchido identifica o escopo específico. Um template padrão e um específico podem coexistir para o mesmo tipo de solicitação.
- O serviço atual verifica duplicidade antes de salvar, mas essa consulta não serializa gravações concorrentes.
- O serviço atual rejeita repetição de `stepOrder` no pedido, mas não exige valor positivo.
- A edição já responde HTTP 409 quando encontra outro template com a mesma combinação; a criação existente responde HTTP 400 para duplicidade detectada na consulta prévia. A duplicidade concorrente no `POST` deve preservar HTTP 400.
- Os campos enum são enums Java. Valores desconhecidos no JSON falham na desserialização; esta especificação trata campos ausentes ou nulos.
- Não existe constraint de unicidade para o escopo de `workflow_templates` nas migrations atuais.
- O usuário confirmou que duplicatas legadas podem ser removidas automaticamente na migration porque a aplicação ainda não está em produção.

## Fora de escopo

- Alterar o formato do envelope `BaseResponse`.
- Exigir ordens sequenciais sem lacunas; lacunas são permitidas.

## Suposições

Nenhuma. As decisões de ordem, resposta HTTP e tratamento de duplicatas legadas foram confirmadas pelo usuário.

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-035 | `stepOrder` deve ser apenas positivo e único, permitindo lacunas, ou as etapas precisam usar a sequência 1..N sem lacunas? | respondida | Deve ser positiva e única; lacunas são permitidas. |
| Q-036 | Para preservar o contrato, uma duplicidade concorrente no `POST` deve retornar HTTP 400 como a duplicidade detectada hoje, ou criação e edição devem usar HTTP 409? | respondida | Preservar HTTP 400 para duplicidade no `POST`. |
| Q-037 | Se já houver templates duplicados quando uma futura migration adicionar unicidade, qual política aplicar? | respondida | Remover duplicatas automaticamente; não há produção ativa e o usuário autorizou a limpeza. |
| Q-038 | Qual template deve ser mantido quando uma combinação possui duplicatas? | respondida | Manter o template de menor `id` e excluir os demais, incluindo suas etapas. |
