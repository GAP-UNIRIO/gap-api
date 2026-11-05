# Spec: Documentação da API (Swagger)

> feature: api-docs
> status: rascunho

## Contexto

Quem consome a API (front-end e quem testa manualmente) precisa de um contrato navegável e sempre atualizado dos endpoints, sem depender de ler o código. A documentação é gerada pelo springdoc-openapi a partir dos controllers e fica acessível em `/api/doc`.

## Histórias

### US-038 — Consultar a documentação da API

Como desenvolvedor que consome a API, quero abrir a documentação interativa em `/api/doc` para conhecer e testar os endpoints sem ler o código.

#### AC-141 — Publicar o contrato OpenAPI sem login

- **Dado** a aplicação em execução em um perfil com documentação habilitada
- **Quando** um cliente sem sessão envia `GET /v3/api-docs`
- **Então** recebe HTTP 200 com o contrato OpenAPI, título `GAP API` e os caminhos dos endpoints existentes, incluindo `/orders`

#### AC-142 — Abrir a interface do Swagger em /doc

- **Dado** a aplicação em execução em um perfil com documentação habilitada
- **Quando** um cliente sem sessão acessa `GET /doc`
- **Então** é redirecionado para a interface do Swagger UI
- **E** `GET /swagger-ui/index.html` responde HTTP 200 sem exigir login

#### AC-143 — Manter os endpoints de negócio protegidos

- **Dado** a liberação das rotas de documentação
- **Quando** um cliente sem sessão chama um endpoint de negócio, como `GET /users/me`
- **Então** continua sendo redirecionado para o login OAuth2, sem receber dados

## Fora de escopo

- Anotações descritivas (`@Operation`, `@Schema`) em cada endpoint; podem ser acrescentadas aos poucos, junto com cada feature.
- Autenticação pelo próprio Swagger UI; o "Try it out" usa a sessão já aberta no navegador.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-011 | A documentação fica desabilitada no perfil `prod` e acessível sem login nos demais perfis. | aberta | Confirmar com o time se o front precisa da documentação em produção. |
| ASM-012 | O "Try it out" do Swagger UI deve enviar o token CSRF lido do cookie `XSRF-TOKEN`, para funcionar com a proteção CSRF atual. | confirmada | `springdoc.swagger-ui.csrf.enabled: true` em `application.yaml`. |

## Perguntas em aberto

Nenhuma.
