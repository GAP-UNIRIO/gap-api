import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const migration = await readFile('src/main/resources/db/migration/V5__seed_core_roles.sql', 'utf8');

test('@spec:AC-144 migration cadastra ROLE_BASIC e ROLE_ADMIN e dá ROLE_BASIC aos usuários existentes', () => {
  assert.match(migration, /insert into roles\s*\(\s*authority\s*\)/i, 'A migration deve inserir em roles(authority)');
  assert.match(migration, /'ROLE_BASIC'/, 'ROLE_BASIC deve ser cadastrado');
  assert.match(migration, /'ROLE_ADMIN'/, 'ROLE_ADMIN deve ser cadastrado');
  assert.match(migration, /on conflict\s*\(\s*authority\s*\)\s*do nothing/i, 'Papéis já existentes não podem quebrar nem ser duplicados');
  assert.match(migration, /insert into users_roles[\s\S]*'ROLE_BASIC'[\s\S]*not exists/i, 'Usuários existentes devem receber ROLE_BASIC sem duplicar');
});
