# Spec: Order create

> feature: order-create
> status: implementada

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

### US-049 — Validar dados flexíveis e anexos do pedido

Como usuário autenticado, quero que os dados opcionais e os arquivos anexados respeitem limites claros para enviar pedidos válidos e seguros.

#### AC-175 — Aceitar `extraData` opcional como JSON

- **Dado** um pedido com `extraData` ausente ou nulo, ou contendo um objeto JSON válido
- **Quando** o cliente envia `POST /orders`
- **Então** o pedido é aceito e o valor informado é preservado, sem validação de campos por tipo de solicitação

#### AC-176 — Rejeitar JSON inválido em `extraData`

- **Dado** um corpo de pedido cujo `extraData` não pode ser interpretado como JSON
- **Quando** o cliente envia `POST /orders`
- **Então** recebe HTTP 400 e nenhum pedido ou anexo é processado

#### AC-177 — Limitar a quantidade de anexos

- **Dado** um pedido com até 10 anexos
- **Quando** o cliente envia `POST /orders`
- **Então** a quantidade é aceita; se houver mais de 10, recebe HTTP 400 e nenhum arquivo é movido nem pedido persistido

#### AC-178 — Limitar tamanho e tipos dos arquivos

- **Dado** um pedido de autorização para arquivo PDF, JPEG, PNG ou DOCX
- **Quando** o cliente envia `POST /orders/annex-upload`
- **Então** recebe uma política assinada que permite arquivo de até 5 MiB; uploads acima do limite são rejeitados pelo S3
- **E** pedidos de autorização para outros tipos de conteúdo recebem HTTP 400
- **E** antes de mover um arquivo para o pedido, a API confirma no armazenamento que seu tamanho não excede 5 MiB

#### AC-179 — Vincular chave temporária ao usuário autenticado

- **Dado** uma chave temporária emitida para um usuário autenticado
- **Quando** outro usuário tenta usar essa chave para criar um pedido
- **Então** recebe HTTP 403 e nenhum arquivo é movido nem pedido persistido
- **E** o próprio usuário pode usar sua chave dentro das demais regras de validade

## Fora de escopo

- Validar campos internos de `extraData` de acordo com cada tipo de solicitação.
- Alterar o schema do banco: os limites e a associação de upload serão aplicados no fluxo e nas chaves assinadas, sem persistir novos metadados de upload.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-004 | Solicitações sem anexos são válidas; a lista de anexos pode estar vazia ou ausente. | confirmada | `OrderService.create_savesOrderWithoutAnnexes` e tratamento de `null` no serviço. |
| ASM-005 | O envio da solicitação e do upload exige autenticação e proteção CSRF. | confirmada | Testes de controller usam sessão OIDC e CSRF; segurança da API exige usuário autenticado. |
| ASM-006 | `userId` diferente do usuário autenticado resulta em HTTP 403. | confirmada | Confirmado pelo usuário. |
| ASM-007 | A ausência de template de workflow resulta em HTTP 503 e reverte banco e objetos finais. | confirmada | Confirmado pelo usuário; o teste de integração já verifica rollback/limpeza. |
| ASM-019 | O limite de 5 MB significa 5 MiB, isto é, 5 × 1024 × 1024 bytes. | confirmada | Definido na conversa; alinhado ao limite solicitado e à condição `content-length-range` do POST ao S3. |
| ASM-020 | Tipos permitidos são identificados pelo MIME type exato `application/pdf`, `image/jpeg`, `image/png` e `application/vnd.openxmlformats-officedocument.wordprocessingml.document`. | confirmada | Lista confirmada pelo usuário: PDF, JPEG, PNG e DOCX. |
| ASM-021 | Chaves temporárias legadas, emitidas antes do vínculo por usuário, serão rejeitadas após a implantação. | confirmada | O usuário decidiu rejeitá-las e solicitar novo upload. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-001 | O endpoint `/orders/annex-upload` deve fazer parte desta feature ou ser especificado em uma feature de upload separada? | respondida | Fica nesta feature; faz parte da criação dos pedidos. |
| Q-002 | A identidade autenticada pode criar solicitação para qualquer `userId` existente ou somente para si mesma? | respondida | Somente para o usuário autenticado. |
| Q-003 | Quando não há template de workflow, a solicitação deve sofrer rollback ou ser criada sem etapas? | respondida | Deve sofrer rollback. |
| Q-004 | Qual resposta HTTP deve ser enviada quando não há template de workflow? | respondida | HTTP 503. |
| Q-005 | Quando o usuário autenticado envia um `userId` de outra pessoa, HTTP 403 é o status esperado? | respondida | Confirmado: HTTP 403. |
| Q-040 | Como tratar uploads temporários feitos antes da implantação do vínculo por usuário? | respondida | Rejeitar as chaves legadas e pedir novo upload. |
