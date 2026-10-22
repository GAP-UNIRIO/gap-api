# Especificação: CRUD de papéis

> feature: role-crud
> status: implementada

## Contexto

A API administra papéis de autorização pelo recurso `/roles`. Cada papel tem uma `authority` única, que também é exposta como autoridade Spring Security.

## Histórias

### US-026 — Consultar papéis

Como gestor, quero listar papéis e consultar um pelo identificador.

#### AC-080 — Listar papéis por id
- **Dado** papéis cadastrados
- **Quando** o cliente envia `GET /roles`
- **Então** recebe HTTP 200, `success` e papéis ordenados por id crescente

#### AC-081 — Listar sem papéis
- **Dado** que não há papéis cadastrados
- **Quando** o cliente envia `GET /roles`
- **Então** recebe HTTP 200 e lista vazia

#### AC-082 — Consultar papel existente
- **Dado** papel existente
- **Quando** o cliente envia `GET /roles/{id}`
- **Então** recebe HTTP 200 com id e `authority`

#### AC-083 — Consultar papel inexistente
- **Dado** identificador sem papel correspondente
- **Quando** o cliente envia `GET /roles/{id}`
- **Então** recebe HTTP 404

### US-027 — Cadastrar papel

Como gestor, quero cadastrar um papel com sua autoridade.

#### AC-084 — Criar papel
- **Dado** `authority` não vazia, com até 255 caracteres e ainda não utilizada
- **Quando** o cliente envia `POST /roles`
- **Então** recebe HTTP 201, `success`, cabeçalho `Location` e o papel criado

#### AC-085 — Validar autoridade
- **Dado** autoridade ausente, em branco ou acima de 255 caracteres
- **Quando** o cliente envia `POST /roles` ou `PUT /roles/{id}`
- **Então** recebe HTTP 400 por validação do DTO

#### AC-086 — Rejeitar autoridade duplicada na criação
- **Dado** autoridade já cadastrada
- **Quando** o cliente tenta criar outro papel com a mesma autoridade
- **Então** recebe HTTP 409 e não cria outro registro

### US-028 — Atualizar papel

Como gestor, quero alterar a autoridade de um papel existente.

#### AC-087 — Atualizar papel existente
- **Dado** papel existente e autoridade nova disponível
- **Quando** o cliente envia `PUT /roles/{id}`
- **Então** recebe HTTP 200 com a autoridade atualizada

#### AC-088 — Rejeitar autoridade de outro papel
- **Dado** autoridade pertencente a outro papel
- **Quando** o cliente atualiza o papel
- **Então** recebe HTTP 409 e mantém a autoridade original

#### AC-089 — Atualizar papel inexistente
- **Dado** identificador sem papel correspondente
- **Quando** o cliente envia `PUT /roles/{id}`
- **Então** recebe HTTP 404 e não cria papel

### US-029 — Excluir papel

Como gestor, quero remover papéis que não devem mais ser usados.

#### AC-090 — Excluir papel sem referências
- **Dado** papel existente que não esteja associado a usuários
- **Quando** o cliente envia `DELETE /roles/{id}`
- **Então** recebe HTTP 200 e o papel é removido

#### AC-091 — Excluir papel inexistente
- **Dado** identificador sem papel correspondente
- **Quando** o cliente envia `DELETE /roles/{id}`
- **Então** recebe HTTP 404

#### AC-092 — Impedir exclusão de papel associado a usuário
- **Dado** papel associado a usuário pela relação `users_roles`
- **Quando** o cliente tenta excluir o papel
- **Então** a chave estrangeira impede a exclusão e a API retorna HTTP 409

## Regras observadas

- `authority` é obrigatória, não pode ser branca, aceita no máximo 255 caracteres e é única no DTO/entidade/banco.
- A listagem é ordenada por id crescente.
- A mesma autoridade no update do próprio papel é permitida; autoridade em uso por outro papel resulta em HTTP 409.
- `Role` implementa `GrantedAuthority`; o recurso CRUD não define nomes ou catálogo fixo de authorities.
- A associação de papéis a usuários é feita pela relação `users_roles`, fora das rotas `/roles`.
- A exclusão executa flush para detectar referências; violações de integridade são convertidas para HTTP 409.
- As rotas seguem autenticação e CSRF globais.

## Fora de escopo

- Definir quais authorities devem existir ou quais perfis podem administrá-las.
- Associar/desassociar papéis de usuários via este CRUD.
- Alterar regras globais de autenticação/autorização.

## Suposições

Nenhuma; a especificação descreve o comportamento existente.

## Perguntas em aberto

Nenhuma.
