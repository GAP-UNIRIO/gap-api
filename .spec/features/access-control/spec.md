# Spec: Controle de acesso por papel

> feature: access-control
> status: implementada

## Contexto

Hoje qualquer usuário logado com e-mail da UNIRIO acessa qualquer endpoint: um aluno pode, por exemplo, apagar uma instituição ou cadastrar fluxos de aprovação. A entidade `Role` e a tabela `users_roles` existem, mas os papéis do banco não são carregados no login e nenhum endpoint verifica papel. Esta feature cria os papéis do núcleo, carrega os papéis do usuário na sessão, protege os endpoints e permite ao administrador atribuir papéis. É pré-requisito da fila e decisão (#26). Faz parte da issue #20 (Controle de acesso), itens 1 e 2.

Papéis do núcleo (Q-028): `ROLE_BASIC` (aluno e usuário comum) e `ROLE_ADMIN` (servidores: secretaria, coordenação e administração). Um papel `ROLE_SUPERADMIN` para operações muito específicas pode vir depois, fora desta feature.

Matriz de acesso (Q-031):

| Endpoint | Leitura (GET) | Escrita (POST/PUT/PATCH/DELETE) |
|---|---|---|
| `/institutions`, `/org-units`, `/courses`, `/academic-calendars` | qualquer usuário logado | ADMIN |
| `/roles`, `/workflow-template` | ADMIN | ADMIN |
| `/users/{id}/roles` | — | ADMIN |
| `/users/me`, `/users/me/complemento` | qualquer usuário logado | qualquer usuário logado |
| `/orders`, `/orders/annex-upload`, `/orders/{id}/cancel` | — | qualquer usuário logado (o serviço já garante que é o dono) |
| `/doc`, `/v3/api-docs` | livre (já existente) | — |

## Histórias

### US-039 — Ter os papéis do núcleo cadastrados

Como administrador, quero que os papéis do núcleo existam em todo ambiente e não possam ser apagados, para atribuí-los sem cadastro manual.

#### AC-144 — Cadastrar os papéis do núcleo pela migration

- **Dado** um banco com as migrations anteriores aplicadas
- **Quando** a migration desta feature é aplicada
- **Então** a tabela `roles` contém `ROLE_BASIC` e `ROLE_ADMIN`
- **E** a migration não falha nem duplica papéis se algum deles já existir
- **E** todo usuário já existente recebe `ROLE_BASIC`, sem duplicar quem já o tem

#### AC-154 — Impedir a exclusão dos papéis do núcleo

- **Dado** um administrador
- **Quando** ele envia `DELETE /roles/{id}` para `ROLE_BASIC` ou `ROLE_ADMIN`
- **Então** recebe HTTP 409 com mensagem clara e o papel continua existindo

### US-040 — Entrar com os papéis do banco

Como usuário, quero que o sistema reconheça meus papéis ao entrar, para acessar o que me cabe.

#### AC-145 — Carregar os papéis do usuário no login

- **Dado** um usuário com papéis atribuídos em `users_roles`
- **Quando** ele faz login pelo Google
- **Então** a sessão contém esses papéis como autoridades

#### AC-146 — Atribuir o papel padrão ao novo usuário

- **Dado** um e-mail permitido que ainda não existe na base
- **Quando** a pessoa faz o primeiro login
- **Então** o usuário é criado com o papel `ROLE_BASIC`

#### AC-147 — Definir o primeiro administrador pela variável de ambiente

- **Dado** um e-mail listado na variável de ambiente `APP_SECURITY_ADMIN_EMAILS`
- **Quando** a pessoa com esse e-mail faz login
- **Então** o usuário passa a ter o papel `ROLE_ADMIN`

#### AC-148 — Mostrar os papéis do usuário logado

- **Dado** um usuário logado
- **Quando** o cliente envia `GET /users/me`
- **Então** a resposta inclui a lista de papéis do usuário

### US-041 — Proteger os endpoints por papel

Como responsável pelo sistema, quero que cada endpoint só aceite os papéis previstos, para que um usuário comum não altere cadastros nem decida pedidos.

#### AC-149 — Restringir a escrita de cadastros ao administrador

- **Dado** um usuário logado sem `ROLE_ADMIN`
- **Quando** ele chama um endpoint de escrita de cadastro (ex.: `DELETE /institutions/{id}`)
- **Então** recebe HTTP 403 e nada é alterado

#### AC-150 — Permitir ao administrador a escrita de cadastros

- **Dado** um usuário com `ROLE_ADMIN`
- **Quando** ele chama um endpoint de escrita de cadastro
- **Então** a operação segue normalmente

#### AC-151 — Aplicar a matriz de acesso

- **Dado** a matriz de acesso do Contexto (Q-031)
- **Quando** um usuário chama um endpoint para o qual não tem papel
- **Então** recebe HTTP 403

#### AC-152 — Responder acesso negado no padrão da API

- **Dado** um usuário logado sem permissão para o endpoint
- **Quando** a chamada é recusada
- **Então** a resposta é HTTP 403 com `BaseResponse` (`status: error`) e mensagem clara, sem detalhes internos

#### AC-153 — Responder chamada sem login

- **Dado** um cliente sem sessão
- **Quando** ele chama um endpoint de negócio
- **Então** recebe HTTP 401 com `BaseResponse` (`status: error`) e mensagem indicando que é preciso fazer login, em vez do redirecionamento para o Google (Q-033)

### US-042 — Atribuir papéis a um usuário

Como administrador, quero definir os papéis de um usuário, para promover um servidor a administrador ou rebaixá-lo.

#### AC-155 — Atribuir papéis

- **Dado** um administrador, um usuário existente e uma lista de papéis existentes
- **Quando** ele envia `PUT /users/{id}/roles`
- **Então** recebe HTTP 200 e o usuário passa a ter exatamente esses papéis

#### AC-156 — Recusar a atribuição por quem não é administrador

- **Dado** um usuário sem `ROLE_ADMIN`
- **Quando** ele envia `PUT /users/{id}/roles`
- **Então** recebe HTTP 403 e os papéis não mudam

#### AC-157 — Recusar usuário ou papel inexistente

- **Dado** um administrador
- **Quando** ele envia `PUT /users/{id}/roles` com usuário ou papel que não existe
- **Então** recebe HTTP 404 com mensagem indicando o que não foi encontrado, e nada muda

## Fora de escopo

- Papel com escopo por curso ou unidade, e a relação com `Course.coordinator`/`secretary`/`director` (item 3 da #20).
- Vínculo dos dados à instituição do usuário (item 4 da #20).
- Papel `ROLE_SUPERADMIN`.
- Mudança de papéis refletida em sessão já aberta: vale a partir do próximo login (ASM-015).

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-015 | Papéis alterados no banco só passam a valer no próximo login do usuário. | aberta | |
| ASM-016 | O CRUD de `/roles` continua permitindo criar papéis novos (RF53), mas os do núcleo não podem ser apagados (HTTP 409). | confirmada | Q-034. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-028 | Quais são os papéis do núcleo? | respondida | `ROLE_BASIC` (aluno) e `ROLE_ADMIN` (todo o resto). `ROLE_SUPERADMIN` talvez no futuro. |
| Q-029 | Usuário novo recebe qual papel no primeiro login? | respondida | `ROLE_BASIC`; depois é alterado conforme a necessidade. |
| Q-030 | Como surge o primeiro administrador? | respondida | Variável de ambiente com a lista de e-mails. |
| Q-031 | A matriz de acesso do Contexto está correta? | respondida | Sim; a consulta de cursos fica aberta a qualquer usuário logado. |
| Q-032 | Endpoint para o administrador atribuir papéis já nesta feature? | respondida | Sim, `PUT /users/{id}/roles`, só para administrador. |
| Q-033 | Chamada sem login deve continuar redirecionando para o Google (302) ou responder 401 em JSON? | respondida | 401 em JSON; o front redireciona o usuário para a página de acesso negado, que pede o login de novo. |
| Q-034 | Impedir a exclusão dos papéis do núcleo? | respondida | Sim, HTTP 409. |
