# Plano de execução — course-crud

> gerado por `onp-spec plano` em 2026-10-21 19:53 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano course-crud --sequencial --modelo gpt-5.6-luna --esforco low`

## Resumo — o que vai acontecer

- **modo SEQUENCIAL (escolha do usuário)**: 3 tarefa(s) pendente(s), UMA APÓS A OUTRA, na árvore principal (2 já concluída(s): T-012, T-013)
- sem worktrees e sem paralelismo — cada tarefa roda numa janela de contexto limpa, na ordem do tasks.md
- **custo travado pelo usuário**: modelo `gpt-5.6-luna` · esforço `low` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/course-crud`; levar para a main é decisão sua

### Avisos

- ⚠ T-011 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-014 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Ordem de execução (uma tarefa após a outra)

| tarefa | título | modelo | esforço |
|---|---|---|---|
| T-011 | Especificar comportamento atual do CRUD | `gpt-5.6-luna` | low |
| T-014 | Projetar responsáveis em formato de resumo | `gpt-5.6-luna` | low |
| T-015 | Verificar a feature e auditar as especificações | `gpt-5.6-luna` | low |

## Gestão de branches e commits

1. branch de trabalho `spec/course-crud` criada do ponto atual (se ainda não existir)
2. as tarefas rodam nela mesma, na ordem — **1 tarefa = 1 commit** (`T-xxx feature: título`), marcada `[concluida]` só com trabalho feito
3. gate final na branch de trabalho: `onp-spec verify course-crud` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/course-crud/executar-tarefas.sh
```

Cada tarefa roda `codex exec` com **janela de contexto limpa**, na árvore principal,
uma após a outra, com `--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`.
Os prompts exatos estão embutidos no script.
Logs: `../onp-worktrees/gap-api-course-crud-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano course-crud --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa course-crud T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo course-crud --tabela   # a tabela de andamento
onp-spec resumo course-crud            # o resumo em texto
```

