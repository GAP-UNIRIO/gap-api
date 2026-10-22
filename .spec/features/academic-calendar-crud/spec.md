# Especificação: CRUD de calendário acadêmico

> feature: academic-calendar-crud
> status: implementada

## Contexto

A API mantém calendários acadêmicos por semestre. Cada calendário registra o período letivo, prazos e limites acadêmicos, e pode ser ativado para ser o calendário vigente. Esta especificação descreve o comportamento atualmente implementado nos endpoints `/academic-calendars`.

## Histórias

### US-008 — Consultar calendários acadêmicos

Como consumidor da API, quero listar calendários e consultar um calendário pelo identificador para conhecer seus dados e estado.

#### AC-015 — Listar calendários por identificador

- **Dado** calendários acadêmicos cadastrados
- **Quando** o cliente envia `GET /academic-calendars`
- **Então** recebe HTTP 200, estado `success` e os calendários ordenados por identificador crescente

#### AC-016 — Retornar calendário por identificador

- **Dado** um calendário existente
- **Quando** o cliente envia `GET /academic-calendars/{id}`
- **Então** recebe HTTP 200, estado `success` e os campos do calendário, incluindo semestre, datas, prazos, limites e estado ativo

#### AC-017 — Informar calendário inexistente na consulta

- **Dado** um identificador sem calendário correspondente
- **Quando** o cliente envia `GET /academic-calendars/{id}`
- **Então** recebe HTTP 404

### US-009 — Cadastrar calendário acadêmico

Como gestor acadêmico, quero cadastrar os dados de um semestre para manter período, prazos e limites disponíveis na API.

#### AC-018 — Criar calendário válido

- **Dado** semestre não cadastrado, datas válidas e campos obrigatórios presentes
- **Quando** o cliente envia `POST /academic-calendars`
- **Então** recebe HTTP 201, estado `success`, cabeçalho `Location` e os dados do calendário criado

#### AC-019 — Validar campos obrigatórios e tamanho do semestre

- **Dado** corpo sem semestre, com semestre em branco, semestre acima de 10 caracteres ou sem data inicial/final
- **Quando** o cliente envia `POST /academic-calendars`
- **Então** recebe HTTP 400 e a resposta identifica dados inválidos

#### AC-020 — Rejeitar período com término anterior ao início

- **Dado** uma data final anterior à data inicial
- **Quando** o cliente cria ou atualiza um calendário
- **Então** recebe HTTP 400 e os dados não são persistidos

#### AC-021 — Rejeitar semestre duplicado

- **Dado** um calendário já cadastrado para o semestre informado
- **Quando** o cliente tenta criar outro calendário para esse semestre
- **Então** recebe HTTP 409 e nenhum novo calendário é criado

#### AC-022 — Criar inativo por padrão

- **Dado** um pedido de criação sem o campo `active`
- **Quando** o calendário é criado
- **Então** o campo `active` retornado é `false`

#### AC-023 — Permitir período de um único dia

- **Dado** datas inicial e final iguais
- **Quando** o cliente cria ou atualiza o calendário
- **Então** a operação é aceita

### US-010 — Atualizar calendário acadêmico

Como gestor acadêmico, quero atualizar um calendário existente para corrigir ou manter os dados do semestre.

#### AC-024 — Atualizar dados do calendário

- **Dado** um calendário existente e um corpo válido
- **Quando** o cliente envia `PUT /academic-calendars/{id}`
- **Então** recebe HTTP 200, estado `success` e os dados atualizados

#### AC-025 — Manter o estado ativo quando omitido na atualização

- **Dado** um calendário existente e corpo de atualização sem `active`
- **Quando** o cliente atualiza o calendário
- **Então** o valor atual de `active` é preservado

#### AC-026 — Ativar um calendário desativando os demais

- **Dado** outros calendários possivelmente ativos
- **Quando** um calendário é criado ou atualizado com `active=true`
- **Então** esse calendário fica ativo e os demais são desativados

#### AC-027 — Rejeitar atualização de semestre duplicado

- **Dado** outro calendário já cadastrado para o novo semestre informado
- **Quando** o cliente atualiza o calendário
- **Então** recebe HTTP 409 e o calendário permanece sem alteração

#### AC-028 — Informar calendário inexistente na atualização

- **Dado** um identificador sem calendário correspondente
- **Quando** o cliente envia `PUT /academic-calendars/{id}`
- **Então** recebe HTTP 404

### US-011 — Excluir calendário acadêmico

Como gestor acadêmico, quero excluir um calendário existente para remover um registro que não deve mais ser mantido.

#### AC-029 — Excluir calendário existente

- **Dado** um calendário existente
- **Quando** o cliente envia `DELETE /academic-calendars/{id}`
- **Então** recebe HTTP 200, estado `success` e o registro é removido

#### AC-030 — Informar calendário inexistente na exclusão

- **Dado** um identificador sem calendário correspondente
- **Quando** o cliente envia `DELETE /academic-calendars/{id}`
- **Então** recebe HTTP 404 e nenhum registro é removido

### US-032 — Validar consistência dos dados do semestre

Como gestor acadêmico, quero que o sistema valide o código do semestre, o período e os prazos para evitar calendários inconsistentes.

#### AC-113 — Validar formato e semestre permitido
- **Dado** `semester` fora do formato `XXXX.Y`, com ano de quatro dígitos ou período diferente de `1` e `2`
- **Quando** o cliente cria ou atualiza um calendário
- **Então** recebe HTTP 400 e o calendário não é persistido

#### AC-114 — Conferir datas compatíveis com o semestre
- **Dado** `semester=XXXX.1` com início entre 1º de janeiro e 31 de julho de XXXX e término até 30 de setembro de XXXX, ou `semester=XXXX.2` com início entre 1º de maio e 31 de dezembro de XXXX e término até 31 de março de XXXX+1; início e término são futuros
- **Quando** o cliente cria ou atualiza um calendário
- **Então** as datas são aceitas se também respeitarem a ordem temporal; datas fora da janela correspondente recebem HTTP 400

#### AC-115 — Exigir datas futuras
- **Dado** `startDate` ou `endDate` igual ou anterior à data atual
- **Quando** o cliente cria ou atualiza um calendário
- **Então** recebe HTTP 400 e o calendário não é persistido

#### AC-116 — Exigir prazos definidos dentro do período
- **Dado** `deadlines` ausente, nulo, vazio, com chave em branco, valor nulo ou data fora de `[startDate, endDate]`
- **Quando** o cliente cria ou atualiza um calendário
- **Então** recebe HTTP 400 e o calendário não é persistido

#### AC-117 — Validar genericamente os limites acadêmicos
- **Dado** `academicLimits` ausente, nulo, vazio, com chave em branco, valor nulo ou valor negativo
- **Quando** o cliente cria ou atualiza um calendário
- **Então** recebe HTTP 400; chaves desconhecidas são aceitas até existir catálogo completo

## Regras observadas no código

- `semester` é obrigatório, segue exatamente `XXXX.1` ou `XXXX.2` e deve ser único.
- `startDate` e `endDate` são obrigatórias; igualdade é permitida, mas `endDate` anterior a `startDate` é rejeitada.
- `semester` segue `XXXX.Y`, com ano de quatro dígitos e semestre `1` ou `2`.
- As datas são futuras, obedecem à janela ampla do semestre e `endDate` não precede `startDate`.
- `deadlines` é obrigatório e não vazio; chaves não brancas mapeiam datas não nulas no intervalo inclusivo do calendário.
- `academicLimits` é obrigatório e não vazio; as chaves podem ser desconhecidas até que o catálogo completo seja definido. Cada entrada requer chave não branca e valor inteiro não nulo maior ou igual a zero.
- No cadastro, `active` omitido ou `false` mantém o calendário inativo.
- Na atualização, `active` omitido preserva o valor atual; `true` ativa esse calendário e desativa os demais; `false` desativa somente este.
- A listagem é ordenada por `id` crescente.
- Os endpoints estão sujeitos à configuração geral de autenticação/CSRF da aplicação.

## Fora de escopo

- Definir o significado ou catálogo de chaves de `deadlines` e `academicLimits`.
- Adicionar operações de ativação ou consulta diferentes dos endpoints CRUD existentes.
- Alterar regras de autorização ou segurança globais.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-008 | Os mapas de prazos e limites são obrigatórios e não vazios; enquanto não houver catálogo de limites, aceitam chaves não brancas ainda desconhecidas. | confirmada | O usuário determinou prazos obrigatórios e pediu validação genérica de limites antes de mapear todos os tipos. |
| ASM-009 | O CRUD de calendário segue a autenticação e a proteção CSRF globais da API. | confirmada | Os testes de controller usam login OIDC e CSRF em operações de escrita; a configuração está em `SecurityConfig`. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-011 | Qual janela flexível de datas deve relacionar cada semestre ao ano `XXXX`? | respondida | Confirmado: `.1` inicia entre janeiro e julho e termina até setembro de XXXX; `.2` inicia entre maio e dezembro de XXXX e termina até março de XXXX+1. Ambas as datas devem ser futuras. |
| Q-012 | Qual faixa numérica genérica deve ser permitida nos limites acadêmicos enquanto o catálogo de chaves não está pronto? | respondida | Confirmado: mapa não vazio, chaves não brancas, valores inteiros não nulos e maiores ou iguais a zero; chaves desconhecidas são aceitas. |
