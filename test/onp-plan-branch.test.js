import test from 'node:test';
import assert from 'node:assert/strict';
import { montarPlano, renderPlanoMd } from '../.agents/skills/onp-spec-driven/scripts/lib/src/core/plano.js';

function project() {
  const tasks = [
    { id: 'T-001', title: 'First task', status: 'pendente', line: 1, files: ['src/a.js'], refs: ['AC-001'] },
    { id: 'T-002', title: 'Second task', status: 'pendente', line: 2, files: ['src/b.js'], refs: ['AC-002'] },
  ];
  return {
    config: {
      rootDir: process.cwd(), specDir: '.spec', testCommand: 'node --test',
      paralelo: { model: 'gpt-5.6-terra', esforco: 'medio', maxParalelas: 3 },
    },
    features: [{ name: 'workflow-fix', tasks: { tasks }, spec: { stories: [] } }],
  };
}

test('montarPlano uses an explicit branch in sequential and parallel modes', () => {
  const sequential = montarPlano(project(), 'workflow-fix', {
    agent: 'codex', sequencial: true, branch: 'fix/workflow-responsible-unit',
  });
  assert.equal(sequential.branchTrabalho, 'fix/workflow-responsible-unit');
  assert.equal(sequential.branchBase, 'main');
  assert.match(renderPlanoMd(sequential), /--sequencial --branch fix\/workflow-responsible-unit/);
  assert.match(renderPlanoMd(sequential), /criada a partir de `main`/);

  const parallel = montarPlano(project(), 'workflow-fix', {
    agent: 'codex', branch: 'feat/workflow-fix', branchBase: 'release',
  });
  assert.equal(parallel.branchTrabalho, 'feat/workflow-fix');
  assert.equal(parallel.branchBase, 'release');
  assert.deepEqual(parallel.faixas.map((lane) => lane.branch), [
    'feat/workflow-fix-faixa-1', 'feat/workflow-fix-faixa-2',
  ]);
  assert.match(renderPlanoMd(parallel), /--branch feat\/workflow-fix --base-branch release/);
});
