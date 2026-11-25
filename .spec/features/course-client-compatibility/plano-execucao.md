# Plano de execução — course-client-compatibility

> gerado por `onp-spec plano` em 2026-11-25 05:05 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano course-client-compatibility --branch fix/course-client-compatibility`

## Resumo — o que vai acontecer

- **2 tarefa(s) pendente(s)**: 2 em 2 faixa(s) paralela(s) + 0 sequencial(is)
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano course-client-compatibility --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `fix/course-client-compatibility`; levar para a main é decisão sua

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `fix/course-client-compatibility-faixa-1` — worktree `../onp-worktrees/gap-api-course-client-compatibility-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-058 | Provar rejeição de payloads sem vínculos organizacionais | `gpt-5.6-terra` | medium | `src/test/java/com/gap/api/Controller/CourseControllerTest.java` |

#### faixa-2 — branch `fix/course-client-compatibility-faixa-2` — worktree `../onp-worktrees/gap-api-course-client-compatibility-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-059 | Consolidar contrato e auditar a especificação | `gpt-5.6-terra` | medium | `.spec/features/course-client-compatibility/spec.md`, `.spec/features/course-client-compatibility/tasks.md`, `.spec/features/course-crud/spec.md` |

## Gestão de branches e commits

1. branch de trabalho `fix/course-client-compatibility` criada a partir de `main` (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify course-client-compatibility` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/course-client-compatibility/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/gap-api-course-client-compatibility-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano course-client-compatibility --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa course-client-compatibility T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo course-client-compatibility --tabela   # a tabela de andamento
onp-spec resumo course-client-compatibility            # o resumo em texto
```

