# Especificação: perfil e conclusão de cadastro do usuário

> feature: user-profile
> status: implementada

## Contexto

O recurso `/users` expõe operações do próprio usuário autenticado: consultar seu perfil e complementar matrícula e curso. Não há endpoints administrativos de criação, listagem, atualização por id ou exclusão. A conta é localizada/criada durante o login OIDC em `CustomOidcUserService`.

## Histórias

### US-020 — Consultar o próprio perfil

Como usuário autenticado, quero consultar meus dados de cadastro para saber se meu cadastro está completo.

#### AC-061 — Retornar o perfil do usuário autenticado
- **Dado** usuário autenticado com conta cadastrada
- **Quando** envia `GET /users/me`
- **Então** recebe HTTP 200, `success` e id, e-mail, nome, matrícula, id do curso e indicador de cadastro completo

#### AC-062 — Informar conta não cadastrada
- **Dado** usuário autenticado cujo e-mail não está cadastrado
- **Quando** envia `GET /users/me`
- **Então** recebe HTTP 404

#### AC-063 — Calcular indicador de cadastro completo
- **Dado** usuário com matrícula não nula/não vazia e curso associado
- **Quando** consulta o perfil
- **Então** `isCompletedCadaster` é `true`; se faltar matrícula ou curso, é `false`

### US-021 — Completar os próprios dados acadêmicos

Como usuário autenticado, quero informar minha matrícula e curso para completar meu cadastro.

#### AC-064 — Associar matrícula e curso da conta autenticada
- **Dado** conta existente correspondente ao e-mail do principal OIDC e curso existente
- **Quando** envia `PUT /users/me/complemento` com `registrarionNumber` e `courseId`
- **Então** recebe HTTP 200, `success`, dados atualizados e `isCompletedCadaster=true`

#### AC-065 — Informar conta inexistente ao complementar
- **Dado** que não existe conta para o e-mail do principal OIDC
- **Quando** envia `PUT /users/me/complemento`
- **Então** recebe HTTP 404 e nenhum curso é consultado ou usuário salvo

#### AC-066 — Informar curso inexistente ao complementar
- **Dado** conta existente e `courseId` sem curso correspondente
- **Quando** envia `PUT /users/me/complemento`
- **Então** recebe HTTP 404 e a matrícula/curso existentes não são alterados nem salvos

#### AC-067 — Usar identidade autenticada, sem aceitar identidade no corpo
- **Dado** principal OIDC autenticado e corpo com apenas matrícula e id do curso
- **Quando** completa o cadastro
- **Então** a conta alterada é aquela localizada pelo e-mail do principal, sem campo de identidade escolhido pelo cliente

## Regras observadas

- `GET /users/me` deriva o e-mail do principal OIDC e retorna `UserResponse`, que não serializa as relações de pedidos e papéis.
- `isCompletedCadaster` é calculado como matrícula não nula/não vazia **e** curso não nulo.
- O DTO de complemento não tem anotações de validação e seu campo está escrito `registrarionNumber` (com essa grafia); não há validação de formato ou não vazio para matrícula.
- O serviço substitui matrícula e curso pelos valores enviados após localizar a conta autenticada. O curso é validado por existência; conta ou curso inexistente resulta em 404.
- O cadastro de conta/perfil básico ocorre no login OIDC; este recurso não oferece criação independente de conta.
- Rotas seguem a autenticação/CSRF definidos globalmente.

## Fora de escopo

- CRUD administrativo de usuários, alteração de e-mail/nome, administração de papéis ou pedidos.
- Definir formato/obrigatoriedade da matrícula, imutabilidade após conclusão ou regra de domínio de vínculo com curso.
- Provisionamento e validação de domínio de e-mail no login OIDC.

## Suposições

Nenhuma; os critérios descrevem o comportamento atual.

## Perguntas em aberto

Nenhuma para documentar o comportamento atual. A ausência de validação de matrícula é registrada como comportamento observado, sem inferir uma regra desejada.
