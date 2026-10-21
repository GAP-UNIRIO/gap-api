# Especificação: inventário da base existente

> feature: codebase-baseline
> status: implementada

## Contexto

O projeto já continha módulos de produção antes das especificações funcionais atuais. O inventário mantém esses arquivos associados a uma tarefa de baseline até que cada módulo receba especificação funcional própria.

## Histórias

### US-007 — Manter rastreabilidade da base existente

Como mantenedor, quero que os arquivos de produção existentes estejam registrados em tarefas, para que código legado não desapareça da visão de manutenção.

#### AC-014 — Detectar arquivos de produção sem tarefa

- **Dado** o inventário de arquivos registrado nas tarefas
- **Quando** arquivos de produção são comparados com os caminhos mapeados
- **Então** todo arquivo sob `src/main` está associado a uma tarefa
- **E** o teste falha quando um novo arquivo de produção não estiver mapeado

## Fora de escopo

- Reconstruir neste inventário os requisitos funcionais de cada módulo legado.
- Remover código com base apenas na ausência de uma especificação histórica.

## Suposições

Nenhuma.

## Perguntas em aberto

Nenhuma.


