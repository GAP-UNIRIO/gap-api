import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const migration = await readFile('src/main/resources/db/migration/V4__add_order_status.sql', 'utf8');

test('@spec:AC-136 migration preenche o status das solicitações existentes e torna a coluna obrigatória', () => {
  const addColumn = migration.search(/alter table orders\s+add column status varchar/i);
  const fillExisting = migration.search(/update orders\s+set status\s*=\s*'EM_ANALISE'/i);
  const setNotNull = migration.search(/alter table orders\s+alter column status set not null/i);

  assert.ok(addColumn >= 0, 'A migration deve criar a coluna orders.status');
  assert.ok(fillExisting > addColumn, 'As solicitações existentes devem receber EM_ANALISE depois de a coluna existir');
  assert.ok(setNotNull > fillExisting, 'A coluna só deve virar obrigatória depois de preenchida');
});
