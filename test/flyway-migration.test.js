import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import test from 'node:test';

const migration = await readFile('src/main/resources/db/migration/V1__initial_schema.sql', 'utf8');
const departmentMigration = await readFile('src/main/resources/db/migration/V2__add_course_department.sql', 'utf8');
const workflowTemplateMigration = await readFile('src/main/resources/db/migration/V6__workflow_template_integrity.sql', 'utf8');
const coreRolesMigration = await readFile('src/main/resources/db/migration/V5__seed_core_roles.sql', 'utf8');
const roleCleanupMigration = await readFile('src/main/resources/db/migration/V8__remove_non_core_roles.sql', 'utf8');
const requireCourseDepartmentMigration = await readFile('src/main/resources/db/migration/V7__require_course_department.sql', 'utf8').catch(() => '');
const approvalDecisionsMigration = await readFile('src/main/resources/db/migration/V8__order_approval_decisions.sql', 'utf8').catch(() => '');
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

test('@spec:AC-111 V2 adiciona departamento opcional a cursos existentes com chave estrangeira', () => {
  assert.match(departmentMigration, /alter\s+table\s+courses\s+add\s+column\s+department_id\s+bigint\s*;/i);
  assert.match(departmentMigration, /foreign\s+key\s*\(\s*department_id\s*\)\s+references\s+org_units\s*\(\s*id\s*\)/i);
  assert.doesNotMatch(departmentMigration, /department_id\s+bigint\s+not\s+null/i);
  assert.doesNotMatch(migration, /department_id/i, 'V1 stays immutable for databases already baselined at version 1');
});

test('@spec:AC-178 V7 não inventa departamentos para cursos existentes', () => {
  assert.match(requireCourseDepartmentMigration, /alter\s+table\s+courses\s+alter\s+column\s+department_id\s+set\s+not\s+null/i);
  assert.doesNotMatch(requireCourseDepartmentMigration, /\bupdate\s+courses\b/i);
});

test('@spec:AC-179 V7 torna department_id obrigatório sem remover a chave estrangeira definida em V2', () => {
  assert.match(requireCourseDepartmentMigration, /alter\s+table\s+courses\s+alter\s+column\s+department_id\s+set\s+not\s+null/i);
  assert.doesNotMatch(requireCourseDepartmentMigration, /drop\s+constraint\s+fk_courses_department/i);
});

test('@spec:AC-202 @spec:AC-204 @spec:AC-207 V8 persiste justificativas e vínculos entre usuários e unidades', () => {
  assert.match(approvalDecisionsMigration, /alter\s+table\s+approval_stages\s+add\s+column\s+rejection_justification\s+text/i);
  assert.match(approvalDecisionsMigration, /create\s+table\s+users_org_units/i);
  assert.match(approvalDecisionsMigration, /primary\s+key\s*\(\s*user_id\s*,\s*org_unit_id\s*\)/i);
  assert.match(approvalDecisionsMigration, /foreign\s+key\s*\(\s*user_id\s*\)\s+references\s+users/i);
  assert.match(approvalDecisionsMigration, /foreign\s+key\s*\(\s*org_unit_id\s*\)\s+references\s+org_units/i);
});

test('@spec:AC-170 V6 mantém menor id e apaga etapas antes dos templates duplicados', () => {
  assert.match(workflowTemplateMigration, /row_number\s*\(\s*\)\s*over\s*\(\s*partition\s+by\s+order_type\s*,\s*course_id\s+order\s+by\s+id\s*\)/i);
  const deleteSteps = workflowTemplateMigration.search(/delete\s+from\s+workflow_steps/i);
  const deleteTemplates = workflowTemplateMigration.search(/delete\s+from\s+workflow_templates/i);
  assert.ok(deleteSteps >= 0, 'Migration deletes dependent steps');
  assert.ok(deleteTemplates > deleteSteps, 'Migration deletes steps before templates');
  assert.match(workflowTemplateMigration, /create\s+unique\s+index\s+uq_workflow_templates_order_type_course[\s\S]*?where\s+course_id\s+is\s+not\s+null/i);
  assert.match(workflowTemplateMigration, /create\s+unique\s+index\s+uq_workflow_templates_order_type_default[\s\S]*?where\s+course_id\s+is\s+null/i);
});

test('@spec:AC-182 V5 cadastra os dois papéis centrais sem duplicar registros', () => {
  assert.match(coreRolesMigration, /values\s*\(\s*'ROLE_BASIC'\s*\)\s*,\s*\(\s*'ROLE_ADMIN'\s*\)/i);
  assert.match(coreRolesMigration, /on\s+conflict\s*\(\s*authority\s*\)\s+do\s+nothing/i);
});

test('@spec:AC-182 V8 também garante a existência dos papéis centrais', () => {
  assert.match(roleCleanupMigration, /insert\s+into\s+roles\s*\(\s*authority\s*\)[\s\S]*?'ROLE_BASIC'[\s\S]*?'ROLE_ADMIN'[\s\S]*?on\s+conflict\s*\(\s*authority\s*\)\s+do\s+nothing/i);
});

test('@spec:AC-185 V8 remove vínculos antes dos papéis que não pertencem ao catálogo central', () => {
  const deleteLinks = roleCleanupMigration.search(/delete\s+from\s+users_roles/i);
  const deleteRoles = roleCleanupMigration.search(/delete\s+from\s+roles/i);
  assert.ok(deleteLinks >= 0, 'Migration removes user-role links first');
  assert.ok(deleteRoles > deleteLinks, 'Migration removes role rows after links');
  assert.equal((roleCleanupMigration.match(/(?:role\.)?authority\s+not\s+in\s*\(\s*'ROLE_BASIC'\s*,\s*'ROLE_ADMIN'\s*\)/gi) || []).length, 2);
});
