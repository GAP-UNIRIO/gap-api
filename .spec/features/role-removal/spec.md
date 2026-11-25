# Especificação: Remoção do CRUD de papéis

> feature: role-removal
> status: rascunho

## Contexto

A API já possui `ROLE_BASIC` e `ROLE_ADMIN` como papéis de acesso do produto. O recurso `/roles` permite criar autoridades arbitrárias e alterar ou excluir papéis, embora a aplicação não tenha regras de autorização próprias para papéis novos. Esta feature remove a administração do catálogo pela API, mantendo os papéis centrais, o login, a proteção por `ROLE_ADMIN` e a atribuição de papéis a usuários por administradores.

## Histórias

### US-049 — Manter um catálogo de papéis controlado pelo sistema

Como responsável pela API, quero que os papéis de acesso sejam definidos pelo sistema, para que administradores não criem permissões sem regras de acesso correspondentes.

#### AC-175 — Remover endpoints de administração do catálogo

- **Dado** um cliente autenticado, inclusive um administrador
- **Quando** tenta listar, consultar, criar, alterar ou excluir papéis por `/roles`
- **Então** a API não expõe operações para administrar esse catálogo (resposta 404 para todas as rotas `/roles`)

#### AC-176 — Preservar os papéis centrais

- **Dado** um banco iniciado pelas migrations
- **Quando** a aplicação cria ou atualiza uma conta pela regra normal de login
- **Então** `ROLE_BASIC` continua disponível para usuários comuns e `ROLE_ADMIN` continua disponível para administradores, sem CRUD público para esses registros

#### AC-177 — Preservar a atribuição administrativa dos papéis centrais

- **Dado** um administrador, um usuário existente e papéis centrais existentes
- **Quando** o administrador usa `PUT /users/{id}/roles`
- **Então** o usuário recebe exatamente os papéis enviados dentre `ROLE_BASIC` e `ROLE_ADMIN`
- **E** usuário sem `ROLE_ADMIN` continua recebendo 403

#### AC-178 — Recusar authorities fora do catálogo central

- **Dado** um administrador enviando `PUT /users/{id}/roles` com autoridade diferente de `ROLE_BASIC` ou `ROLE_ADMIN`
- **Quando** a API valida a solicitação
- **Então** responde 400 com mensagem clara e não altera os papéis do usuário

#### AC-179 — Remover papéis extras já existentes sem retirar papéis centrais

- **Dado** um banco contendo papéis fora do catálogo central associados a usuários
- **Quando** a migration de remoção é aplicada
- **Então** remove as associações a papéis extras e os registros desses papéis
- **E** preserva `ROLE_BASIC`, `ROLE_ADMIN` e suas associações

## Fora de escopo

- Remover a entidade, tabela ou relação de papéis, que continuam sendo usadas pelo Spring Security e pelo vínculo de usuários.
- Remover `PUT /users/{id}/roles`.
- Criar papéis com permissões configuráveis em tempo de execução.
- Adicionar papéis como `ROLE_SUPERADMIN`.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-021 | “Remover roles” significa remover o CRUD `/roles`, e não remover o modelo de papéis usado por autenticação e usuários. | confirmada | Contexto do pedido e conversa: os papéis centrais continuam necessários. |
| ASM-022 | O catálogo válido nesta feature contém somente `ROLE_BASIC` e `ROLE_ADMIN`; atribuições de outros nomes deixam de ser permitidas. | confirmada | Usuário confirmou que o catálogo se limita a esses dois papéis. |
| ASM-023 | A migration de limpeza pode remover dados de papéis extras após desvinculá-los dos usuários. | confirmada | Usuário autorizou remover vínculos e registros extras na migration. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-039 | Papéis extras existentes devem ser removidos do banco e de usuários durante a migration, ou apenas desativados/ignorados pela aplicação? | respondida | Remover associações e registros extras, preservando `ROLE_BASIC`, `ROLE_ADMIN` e as associações desses papéis. |
| Q-040 | O catálogo permitido deve ser apenas `ROLE_BASIC` e `ROLE_ADMIN`? | respondida | Sim; somente esses dois papéis são permitidos. |
