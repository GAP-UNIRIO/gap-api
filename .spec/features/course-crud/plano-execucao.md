# Plano de execução — course-crud

> gerado por `onp-spec plano` em 2026-10-22 18:35 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano course-crud --paralelizar T-032,T-033,T-034,T-035,T-036 --modelo gpt-5.6-luna --esforco low`

## Resumo — o que vai acontecer

- **5 tarefa(s) pendente(s)**: 5 em 1 faixa(s) paralela(s) + 0 sequencial(is) (6 já concluída(s): T-011, T-012, T-013, T-014, T-015, T-031)
- **seleção do usuário**: paralelizar só T-032, T-033, T-034, T-035, T-036 — as demais rodam uma após a outra, ao final
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano course-crud --paralelizar T-xxx,T-yyy` ou `--sequencial`
- **custo travado pelo usuário**: modelo `gpt-5.6-luna` · esforço `low` em TODAS as tarefas (vence tasks.md e config)
- tudo acontece na branch de trabalho `spec/course-crud`; levar para a main é decisão sua

### Avisos

- ⚠ T-032 está [em-andamento] — entrou no plano; se já houver trabalho local, commite antes de executar

## Faixas e ondas

### Onda 1 — faixa-1

#### faixa-1 — branch `spec/course-crud-faixa-1` — worktree `../onp-worktrees/gap-api-course-crud-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-032 | Validar nomes de curso na requisição | `gpt-5.6-luna` | low | `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `.spec/features/course-crud/spec.md` |
| T-033 | Expandir contrato para unidades organizacionais | `gpt-5.6-luna` | low | `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Model/DTO/CourseResponse.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java` |
| T-034 | Validar responsáveis obrigatórios e seus papéis | `gpt-5.6-luna` | low | `src/main/java/com/gap/api/Model/DTO/CourseRequest.java`, `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Controller/CourseControllerTest.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java` |
| T-035 | Resolver unidades organizacionais por tipo | `gpt-5.6-luna` | low | `src/main/java/com/gap/api/Service/CourseService.java`, `src/test/java/com/gap/api/Service/CourseServiceTest.java`, `src/test/java/com/gap/api/Integration/CourseServiceIntegrationTest.java` |
| T-036 | Verificar e auditar regras novas | `gpt-5.6-luna` | low | `.spec/features/course-crud/spec.md`, `.spec/features/course-crud/tasks.md`, `.spec/verification/course-crud.json` |

## Gestão de branches e commits

1. branch de trabalho `spec/course-crud` criada do ponto atual (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify course-crud` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/course-crud/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
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

