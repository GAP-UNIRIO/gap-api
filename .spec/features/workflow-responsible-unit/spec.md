# Spec: Workflow responsible unit

> feature: workflow-responsible-unit
> status: em-implementacao

## Contexto

Ao instanciar um workflow para um pedido, toda etapa pendente precisa ter a unidade responsável configurada no curso. A ausência de qualquer associação exigida deve interromper a criação com conflito, sem salvar etapas.

## Histórias

### US-048 — Criar etapas com unidade responsável

Como solicitante, quero que cada etapa do meu pedido tenha uma unidade responsável, para que o fluxo não fique pendente sem encaminhamento.

#### AC-173 — Rejeitar etapa sem unidade responsável

- **Dado** um curso sem a unidade indicada por uma etapa do template, incluindo coordenação, direção, secretaria, reitoria ou escola
- **Quando** o motor instancia o workflow do pedido
- **Então** responde HTTP 409 com mensagem que identifica a ausência e não persiste nenhuma etapa

#### AC-174 — Resolver unidades responsáveis configuradas

- **Dado** que coordenação, direção, secretaria, reitoria e escola exigidas pelas etapas estão associadas ao curso
- **Quando** o motor instancia o workflow
- **Então** salva as etapas pendentes com as unidades associadas correspondentes

## Fora de escopo

- Alterar a configuração de associações opcionais no cadastro de cursos.
- Alterar templates ou migrations.

## Suposições

Nenhuma.

## Perguntas em aberto

Nenhuma.

