# Spec: Order create

> feature: order-create
> status: rascunho

## Contexto

Usuários autenticados precisam enviar solicitações acadêmicas com dados e, opcionalmente, anexos, com resposta rastreável e sem deixar arquivos finais órfãos quando a criação falha.

## Histórias

### US-004 — Criar uma solicitação

Como usuário autenticado, quero enviar uma solicitação para que ela seja registrada e encaminhada ao fluxo de aprovação correspondente.

#### AC-004 — Receber confirmação da solicitação criada

- **Dado** um usuário existente e uma solicitação com `userId` e `orderType` válidos
- **Quando** o cliente envia `POST /orders`
- **Então** recebe sucesso HTTP 201, estado `success` e os dados da solicitação criada, incluindo identificador, usuário, tipo, data de criação e anexos

#### AC-005 — Rejeitar corpo inválido

- **Dado** uma solicitação sem `userId` ou sem `orderType`
- **Quando** o cliente envia `POST /orders`
- **Então** recebe HTTP 400 e a solicitação não é processada

#### AC-006 — Informar quando a conta autenticada não está cadastrada

- **Dado** um usuário autenticado cujo e-mail não corresponde a uma conta local
- **Quando** o cliente envia `POST /orders`
- **Então** recebe HTTP 404 com a indicação de que o usuário não foi encontrado

#### AC-013 — Impedir criação em nome de outro usuário

- **Dado** um usuário autenticado e uma solicitação com `userId` pertencente a outra pessoa
- **Quando** o cliente envia `POST /orders`
- **Então** recebe HTTP 403 e a solicitação não é persistida

### US-005 — Anexar arquivos enviados previamente

Como usuário, quero anexar arquivos enviados previamente para que eles acompanhem a solicitação sem aceitar chaves temporárias inválidas.

#### AC-007 — Incorporar anexos temporários válidos

- **Dado** uma solicitação válida com anexos cujas chaves temporárias foram emitidas pelo serviço de armazenamento
- **Quando** a criação é concluída
- **Então** cada arquivo é movido para uma chave final da solicitação e aparece nos dados retornados com nome, tipo de conteúdo e chave final

#### AC-008 — Rejeitar chave temporária inválida

- **Dado** uma solicitação contendo uma chave temporária não emitida pelo serviço de armazenamento
- **Quando** o cliente envia `POST /orders`
- **Então** recebe HTTP 400 e nenhum pedido é persistido ou arquivo movido

#### AC-009 — Compensar falha durante a criação

- **Dado** que um ou mais arquivos já foram movidos para chaves finais
- **Quando** persistência de anexos ou inicialização do fluxo de aprovação falha
- **Então** a transação do banco é revertida e os objetos finais já movidos são excluídos

### US-006 — Autorizar envio de anexo

Como usuário autenticado, quero receber os campos de envio assinado para carregar um anexo temporário antes de criar a solicitação.

#### AC-010 — Receber autorização de upload

- **Dado** metadados válidos de nome e tipo de conteúdo
- **Quando** o cliente envia `POST /orders/annex-upload`
- **Então** recebe HTTP 200 e os campos de envio assinado, incluindo URL, chave temporária e campos exigidos pelo armazenamento

#### AC-011 — Rejeitar metadados inválidos no upload

- **Dado** metadados inválidos
- **Quando** o cliente solicita autorização de upload
- **Então** recebe HTTP 400 e nenhuma autorização é gerada

#### AC-012 — Informar indisponibilidade do armazenamento

- **Dado** metadados válidos e serviço de armazenamento indisponível
- **Quando** o cliente solicita autorização de upload
- **Então** recebe HTTP 503

## Fora de escopo

- Mudanças no mecanismo de armazenamento ou no contrato de autenticação.
- Mudanças no mecanismo de armazenamento ou no contrato geral de autenticação.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-004 | Solicitações sem anexos são válidas; a lista de anexos pode estar vazia ou ausente. | confirmada | `OrderService.create_savesOrderWithoutAnnexes` e tratamento de `null` no serviço. |
| ASM-005 | O envio da solicitação e do upload exige autenticação e proteção CSRF. | confirmada | Testes de controller usam sessão OIDC e CSRF; segurança da API exige usuário autenticado. |
| ASM-006 | `userId` diferente do usuário autenticado resulta em HTTP 403. | confirmada | Confirmado pelo usuário. |
| ASM-007 | A ausência de template de workflow resulta em HTTP 503 e reverte banco e objetos finais. | confirmada | Confirmado pelo usuário; o teste de integração já verifica rollback/limpeza. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-001 | O endpoint `/orders/annex-upload` deve fazer parte desta feature ou ser especificado em uma feature de upload separada? | respondida | Fica nesta feature; faz parte da criação dos pedidos. |
| Q-002 | A identidade autenticada pode criar solicitação para qualquer `userId` existente ou somente para si mesma? | respondida | Somente para o usuário autenticado. |
| Q-003 | Quando não há template de workflow, a solicitação deve sofrer rollback ou ser criada sem etapas? | respondida | Deve sofrer rollback. |
| Q-004 | Qual resposta HTTP deve ser enviada quando não há template de workflow? | respondida | HTTP 503. |
| Q-005 | Quando o usuário autenticado envia um `userId` de outra pessoa, HTTP 403 é o status esperado? | respondida | Confirmado: HTTP 403. |


