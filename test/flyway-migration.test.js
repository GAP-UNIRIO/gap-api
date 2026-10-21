import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const migration = await readFile('src/main/resources/db/migration/V1__initial_schema.sql', 'utf8');
const production = await readFile('src/main/resources/application-prod.yaml', 'utf8');

test('@spec:AC-001 migration inicial cobre tabelas e tipos PostgreSQL das entidades', () => {
  const tables = {
    academic_calendars: ['id', 'semester', 'start_date', 'end_date', 'deadlines', 'academic_limits', 'is_active'],
    annexes: ['id', 'order_id', 'url', 'name', 'content_type'],
    approval_stages: ['id', 'name', 'status', 'step_order', 'created_at', 'order_id', 'user_id', 'org_unit_id'],
    courses: ['id', 'name', 'director_id', 'coordinator_id', 'secretary_id', 'reitoria_id', 'coordenacao_id', 'direcao_id', 'secretaria_id', 'escola_id'],
    graduation_events: ['id', 'name', 'event_date', 'course_id'],
    institutions: ['id', 'name', 'code', 'auth_provider', 'signature_provider', 'settings', 'is_active', 'created_at'],
    notifications: ['id', 'user_id', 'order_id', 'title', 'message', 'is_read', 'action_url', 'created_at'],
    order_histories: ['id', 'order_id', 'actor_id', 'event_type', 'description', 'created_at', 'payload'],
    orders: ['id', 'user_id', 'created_at', 'tipo_solicitacao', 'extra_data', 'graduation_event_id'],
    org_units: ['id', 'type', 'name'],
    roles: ['id', 'authority'],
    users: ['id', 'email', 'registration_number', 'name', 'course_id'],
    users_roles: ['user_id', 'role_id'],
    workflow_steps: ['id', 'template_id', 'step_order', 'stage_type', 'responsible_unit_type', 'step_description', 'step_name'],
    workflow_templates: ['id', 'order_type', 'course_id'],
  };

  for (const [table, columns] of Object.entries(tables)) {
    const tableSql = migration.match(new RegExp(`create table ${table}\\s*\\(([\\s\\S]*?)\\n\\);`, 'i'))?.[1];
    assert.ok(tableSql, `Missing table: ${table}`);
    for (const column of columns) {
      assert.match(tableSql, new RegExp(`\\b${column}\\b`, 'i'), `Missing column ${table}.${column}`);
    }
  }
  assert.match(migration, /jsonb/i);
  assert.match(migration, /foreign key/i);
  assert.match(migration, /on delete cascade/i);
  assert.match(migration, /create sequence approval_stages_seq/i);
});

test('@spec:AC-002 perfil de produção configura baseline na versão inicial', () => {
  assert.match(production, /baseline-on-migrate:\s*true/i);
  assert.match(production, /baseline-version:\s*1/i);
});

test('@spec:AC-003 Hibernate valida schema sem DDL automático em produção', () => {
  assert.match(production, /ddl-auto:\s*validate/i);
});
