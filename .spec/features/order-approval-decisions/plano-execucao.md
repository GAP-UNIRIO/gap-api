# Plano de execução — order-approval-decisions

> gerado por `onp-spec plano` em 2026-11-25 14:39 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano order-approval-decisions --sequencial --branch feat/order-approval-decisions`

## Resumo — o que vai acontecer

- **modo SEQUENCIAL (escolha do usuário)**: 4 tarefa(s) pendente(s), UMA APÓS A OUTRA, na árvore principal
- sem worktrees e sem paralelismo — cada tarefa roda numa janela de contexto limpa, na ordem do tasks.md
- tudo acontece na branch de trabalho `feat/order-approval-decisions`; levar para a main é decisão sua

### Avisos

- ⚠ T-058 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-059 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-060 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar
- ⚠ T-061 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Ordem de execução (uma tarefa após a outra)

| tarefa | título | modelo | esforço |
|---|---|---|---|
| T-058 | Escrever e executar testes de aceitação | `gpt-5.6-terra` | medium |
| T-059 | Vincular usuários às unidades organizacionais | `gpt-5.6-terra` | medium |
| T-060 | Implementar decisões sequenciais das etapas | `gpt-5.6-terra` | medium |
| T-061 | Permitir conclusão manual pela secretaria | `gpt-5.6-terra` | medium |

## Gestão de branches e commits

1. branch de trabalho `feat/order-approval-decisions` criada a partir de `main` (se ainda não existir)
2. as tarefas rodam nela mesma, na ordem — **1 tarefa = 1 commit** (`T-xxx feature: título`), marcada `[concluida]` só com trabalho feito
3. gate final na branch de trabalho: `onp-spec verify order-approval-decisions` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/order-approval-decisions/executar-tarefas.sh
```

Cada tarefa roda `codex exec` com **janela de contexto limpa**, na árvore principal,
uma após a outra, com `--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`.
Os prompts exatos estão embutidos no script.
Logs: `../onp-worktrees/gap-api-order-approval-decisions-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano order-approval-decisions --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa order-approval-decisions T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo order-approval-decisions --tabela   # a tabela de andamento
onp-spec resumo order-approval-decisions            # o resumo em texto
```

