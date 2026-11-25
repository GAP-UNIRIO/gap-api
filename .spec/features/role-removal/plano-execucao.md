# Plano de execução — role-removal

> gerado por `onp-spec plano` em 2026-11-25 03:36 — NÃO edite à mão;
> mudou tasks.md ou a config? Regenere: `onp-spec plano role-removal`

## Resumo — o que vai acontecer

- **5 tarefa(s) pendente(s)**: 5 em 2 faixa(s) paralela(s) + 0 sequencial(is)
- **1 faixa = 1 worktree + 1 branch + 1 janela de contexto limpa** — faixas não compartilham nenhum arquivo entre si
- prefere outra seleção ou uma após a outra? Regenere com `onp-spec plano role-removal --paralelizar T-xxx,T-yyy` ou `--sequencial`
- tudo acontece na branch de trabalho `spec/role-removal`; levar para a main é decisão sua

## Faixas e ondas

### Onda 1 — faixa-1 ∥ faixa-2

#### faixa-1 — branch `spec/role-removal-faixa-1` — worktree `../onp-worktrees/gap-api-role-removal-faixa-1`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-160 | Definir o catálogo permitido na atribuição de papéis | `gpt-5.6-terra` | medium | `src/main/java/com/gap/api/Service/UserService.java`, `src/main/java/com/gap/api/Model/DTO/UserRolesRequest.java`, `src/test/java/com/gap/api/Service/UserServiceTest.java`, `src/test/java/com/gap/api/Controller/AccessControlTest.java` |
| T-161 | Remover controller e serviço do CRUD `/roles` | `gpt-5.6-terra` | medium | `src/main/java/com/gap/api/Controller/RoleController.java`, `src/main/java/com/gap/api/Service/RoleService.java`, `src/main/java/com/gap/api/Service/Interface/IRoleService.java`, `src/main/java/com/gap/api/Model/DTO/RoleRequest.java`, `src/main/java/com/gap/api/Model/DTO/RoleResponse.java`, `src/main/java/com/gap/api/Config/SecurityConfig.java`, `src/test/java/com/gap/api/Controller/RoleControllerTest.java`, `src/test/java/com/gap/api/Config/SecurityConfigTest.java`, `src/test/java/com/gap/api/Controller/AccessControlTest.java` |
| T-162 | Limpar papéis extras legados por migration | `gpt-5.6-terra` | medium | `src/main/resources/db/migration/V7__remove_non_core_roles.sql`, `src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java`, `test/flyway-migration.test.js` |
| T-164 | Registrar testes anotados, verificar e auditar | `gpt-5.6-terra` | medium | `src/test/java/com/gap/api/Controller/RoleControllerTest.java`, `src/test/java/com/gap/api/Service/UserServiceTest.java`, `src/test/java/com/gap/api/Controller/AccessControlTest.java`, `src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java`, `test/run-onp-tests.mjs`, `.spec/verification/role-removal.json` |

#### faixa-2 — branch `spec/role-removal-faixa-2` — worktree `../onp-worktrees/gap-api-role-removal-faixa-2`

| tarefa | título | modelo | esforço | arquivos |
|---|---|---|---|---|
| T-163 | Atualizar documentação e specs conflitantes | `gpt-5.6-terra` | medium | `README.md`, `.spec/features/access-control/spec.md`, `.spec/features/access-control/tasks.md`, `.spec/features/role-crud/spec.md`, `.spec/features/role-crud/tasks.md`, `.spec/features/role-removal/spec.md` |

## Gestão de branches e commits

1. branch de trabalho `spec/role-removal` criada a partir de `main` (se ainda não existir)
2. cada faixa nasce dela como branch própria e roda no seu worktree — **1 tarefa = 1 commit** (`T-xxx feature: título`)
3. terminou a onda → merge `--no-ff` de cada faixa de volta, na ordem; conflito interrompe a faixa e pede resolução humana
4. faixa mesclada → worktree removido, branch apagada, tarefa marcada `[concluida]` no tasks.md
5. gate final na branch de trabalho: `onp-spec verify role-removal` + `onp-spec audit --ci` — **exit 0 ou não está pronto**

## Como executar

### ▶ Execução — Codex headless (codex exec)

```bash
bash .spec/features/role-removal/executar-tarefas.sh
```

Cada faixa roda `codex exec` com **janela de contexto limpa**, no seu worktree, com
`--model` e `model_reasoning_effort` já definidos por tarefa e sandbox `workspace-write`. Os prompts exatos estão
embutidos no script — quer rodar uma faixa na mão, é só copiá-los de lá.
Logs: `../onp-worktrees/gap-api-role-removal-logs/`.

**Confirmação de custos — antes de executar**: os modelos e esforços por
tarefa estão nas tabelas acima; o agente CONFIRMA com o usuário se estão
dentro da licença/cota dele (modelo forte + esforço alto torra tokens).
Para gastar menos: `onp-spec plano role-removal --modelo gpt-5.6-luna --esforco baixo`
(tudo) ou por tarefa `onp-spec tarefa role-removal T-xxx --modelo <m> --esforco <nível>` — e regenere o plano.

### 📣 Acompanhamento — tabela + resumo no chat (a cada 1 min)

O script roda em **background**: o agente AVISA o usuário antes de iniciar e,
enquanto roda, posta no chat a cada ~1 minuto a **tabela de andamento** (qual
tarefa está rodando, qual não está, o que concluiu/falhou) junto com o
**resumo geral de andamento** (escrito por IA; sem IA, o motor resume). Ao
final, o usuário recebe o resumo completo da execução. A qualquer momento:

```bash
onp-spec resumo role-removal --tabela   # a tabela de andamento
onp-spec resumo role-removal            # o resumo em texto
```

