# Especificação: CRUD de instituições

> feature: institution-crud
> status: implementada

## Contexto

A API permite consultar e administrar instituições pelo recurso `/institutions`. A especificação registra o contrato atual dos endpoints e as restrições observadas nos DTOs, serviço e entidade.

## Histórias

### US-016 — Consultar instituições

Como consumidor da API, quero listar instituições e consultar uma instituição pelo identificador.

#### AC-045 — Listar instituições por id
- **Dado** instituições cadastradas
- **Quando** o cliente envia `GET /institutions`
- **Então** recebe HTTP 200, `success` e instituições em ordem crescente de id

#### AC-046 — Listar sem instituições
- **Dado** que não há instituições cadastradas
- **Quando** o cliente envia `GET /institutions`
- **Então** recebe HTTP 200, `success` e lista vazia

#### AC-047 — Consultar instituição existente
- **Dado** uma instituição existente
- **Quando** o cliente envia `GET /institutions/{id}`
- **Então** recebe HTTP 200 e os campos id, nome, código, provedores, configurações, estado ativo e data de criação

#### AC-048 — Consultar instituição inexistente
- **Dado** um identificador sem instituição correspondente
- **Quando** o cliente envia `GET /institutions/{id}`
- **Então** recebe HTTP 404

### US-017 — Cadastrar instituição

Como gestor, quero cadastrar instituições com provedores de autenticação e assinatura.

#### AC-049 — Criar instituição
- **Dado** dados válidos para uma instituição
- **Quando** o cliente envia `POST /institutions`
- **Então** recebe HTTP 201, `success`, `Location` apontando para o recurso e os dados criados

#### AC-050 — Validar campos obrigatórios e limites
- **Dado** nome ou código ausente/em branco, nome acima de 255 caracteres, código acima de 50, ou provedor obrigatório ausente
- **Quando** o cliente envia `POST /institutions`
- **Então** recebe HTTP 400

#### AC-051 — Restringir valores dos provedores
- **Dado** valor desconhecido para um dos provedores
- **Quando** o cliente envia `POST /institutions`
- **Então** recebe HTTP 400; os valores válidos são os enums documentados nas regras observadas

#### AC-052 — Código único
- **Dado** que já existe instituição com o código enviado
- **Quando** o cliente tenta criar outra instituição
- **Então** recebe HTTP 409 e não cria registro adicional

#### AC-053 — Ativar por padrão
- **Dado** uma instituição válida sem `active` ou com `active=false`
- **Quando** o cliente cria a instituição
- **Então** o padrão é `true` e o valor explícito `false` é respeitado

### US-018 — Atualizar instituição

Como gestor, quero atualizar os dados de uma instituição existente.

#### AC-054 — Atualizar instituição existente
- **Dado** uma instituição existente e dados válidos
- **Quando** o cliente envia `PUT /institutions/{id}`
- **Então** recebe HTTP 200 com nome, código, provedores e configurações atualizados

#### AC-055 — Preservar active quando omitido
- **Dado** instituição existente e `active` omitido ou nulo
- **Quando** o cliente atualiza a instituição
- **Então** o valor atual de `active` é mantido

#### AC-056 — Permitir manter o mesmo código
- **Dado** instituição existente e seu código atual no pedido
- **Quando** o cliente atualiza a instituição
- **Então** não ocorre conflito por duplicidade

#### AC-057 — Rejeitar código pertencente a outra instituição
- **Dado** outra instituição que já utiliza o novo código
- **Quando** o cliente atualiza a instituição
- **Então** recebe HTTP 409 e mantém os dados atuais

#### AC-058 — Informar instituição inexistente
- **Dado** identificador sem instituição correspondente
- **Quando** o cliente envia `PUT /institutions/{id}`
- **Então** recebe HTTP 404 e não cria instituição

### US-019 — Excluir instituição

Como gestor, quero excluir uma instituição existente.

#### AC-059 — Excluir instituição existente
- **Dado** uma instituição existente
- **Quando** o cliente envia `DELETE /institutions/{id}`
- **Então** recebe HTTP 200, `success` e remove o registro

#### AC-060 — Informar instituição inexistente na exclusão
- **Dado** identificador sem instituição correspondente
- **Quando** o cliente envia `DELETE /institutions/{id}`
- **Então** recebe HTTP 404

## Regras observadas

- A listagem é ordenada por id crescente.
- `name` tem limite de 255 caracteres; `code` tem limite de 50 e é único. O código é descrito na entidade como CNPJ, mas não há validação de formato, dígitos verificadores ou normalização.
- Os provedores são enums persistidos como texto; os valores aceitos estão listados no critério AC-051.
- `settings` é um mapa JSON opcional, retornado sem transformação.
- Na criação, `active` omitido/nulo resulta em `true`; na atualização, omitido/nulo preserva o valor atual.
- `createdAt` é gerado na persistência e não é atualizável pelo DTO.
- Escritas seguem autenticação e CSRF globais.

## Fora de escopo

- Validar CNPJ ou impor regras específicas ao mapa `settings`.
- Alterar autenticação, autorização ou CSRF global.
- Definir regras de domínio adicionais para seleção dos provedores.

## Suposições

Nenhuma; esta especificação descreve o comportamento do código existente.

## Perguntas em aberto

Nenhuma.
