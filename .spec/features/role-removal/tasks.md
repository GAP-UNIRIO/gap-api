# Tarefas: Remoção do CRUD de papéis

> feature: role-removal

## T-160 — Definir o catálogo permitido na atribuição de papéis [pendente]
- Refs: US-049, AC-177, AC-178
- Arquivos: src/main/java/com/gap/api/Service/UserService.java, src/main/java/com/gap/api/Model/DTO/UserRolesRequest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java
- Notas: Validar lista completa antes de alterar o usuário; resposta clara e sem atualização parcial. Q-040 foi respondida: catálogo limitado aos dois papéis centrais.

## T-161 — Remover controller e serviço do CRUD `/roles` [pendente]
- Refs: US-049, AC-175
- Arquivos: src/main/java/com/gap/api/Controller/RoleController.java, src/main/java/com/gap/api/Service/RoleService.java, src/main/java/com/gap/api/Service/Interface/IRoleService.java, src/main/java/com/gap/api/Model/DTO/RoleRequest.java, src/main/java/com/gap/api/Model/DTO/RoleResponse.java, src/main/java/com/gap/api/Config/SecurityConfig.java, src/test/java/com/gap/api/Controller/RoleControllerTest.java, src/test/java/com/gap/api/Config/SecurityConfigTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java
- Notas: Manter entidade Role, RoleRepository, constantes e migration de seed. Confirmar que todo método `/roles` deixa de ser mapeado e retorna 404.

## T-162 — Limpar papéis extras legados por migration [pendente]
- Refs: US-049, AC-176, AC-179
- Arquivos: src/main/resources/db/migration/V7__remove_non_core_roles.sql, src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java, test/flyway-migration.test.js
- Notas: Q-039 foi respondida autorizando a limpeza. Remover vínculos extras antes dos registros; preservar `ROLE_BASIC`, `ROLE_ADMIN` e suas atribuições. Não reescrever migrations já publicadas.

## T-163 — Atualizar documentação e specs conflitantes [pendente]
- Refs: US-049, AC-175, AC-176, AC-177, AC-178, AC-179
- Arquivos: README.md, .spec/features/access-control/spec.md, .spec/features/access-control/tasks.md, .spec/features/role-crud/spec.md, .spec/features/role-crud/tasks.md, .spec/features/role-removal/spec.md
- Notas: Marcar role-crud como substituída por role-removal sem apagar rastreabilidade histórica; atualizar matriz de acesso e explicar que os dois papéis são definidos pelo sistema.

## T-164 — Registrar testes anotados, verificar e auditar [pendente]
- Refs: US-049, AC-175, AC-176, AC-177, AC-178, AC-179
- Arquivos: src/test/java/com/gap/api/Controller/RoleControllerTest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java, src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java, test/run-onp-tests.mjs, .spec/verification/role-removal.json
- Notas: Um teste anotado `@spec:AC-xxx` por critério; executar verify e audit --ci. AC-179 depende da aprovação da política de migração de dados.
