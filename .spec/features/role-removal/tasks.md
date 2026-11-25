# Tarefas: Remoção do CRUD de papéis

> feature: role-removal

## T-160 — Definir o catálogo permitido na atribuição de papéis [concluida]
- Refs: US-049, AC-177, AC-178
- Arquivos: src/main/java/com/gap/api/Service/UserService.java, src/main/java/com/gap/api/Model/DTO/UserRolesRequest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java
- Notas: Validar lista completa antes de alterar o usuário; resposta clara e sem atualização parcial. Q-040 foi respondida: catálogo limitado aos dois papéis centrais.

## T-161 — Remover controller e serviço do CRUD `/roles` [concluida]
- Refs: US-049, AC-175
- Arquivos: src/main/java/com/gap/api/Config/SecurityConfig.java, src/test/java/com/gap/api/Controller/RoleControllerTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java, test/run-onp-tests.mjs
- Notas: Remover RoleController, RoleService, IRoleService, RoleRequest, RoleResponse e os testes exclusivos do CRUD. Manter entidade Role, RoleRepository, constantes e migration de seed. As rotas sem mapeamento retornam 404.

## T-162 — Limpar papéis extras legados por migration [concluida]
- Refs: US-049, AC-176, AC-179
- Arquivos: src/main/resources/db/migration/V8__remove_non_core_roles.sql, src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java, test/flyway-migration.test.js
- Notas: Q-039 foi respondida autorizando a limpeza. Garantir os papéis centrais sem duplicá-los, remover vínculos extras antes dos registros e preservar as associações centrais. Não reescrever migrations já publicadas.

## T-163 — Atualizar documentação e specs conflitantes [concluida]
- Refs: US-049, AC-175, AC-176, AC-177, AC-178, AC-179
- Arquivos: README.md, .spec/features/access-control/spec.md, .spec/features/access-control/tasks.md, .spec/features/role-crud/README.md, .spec/features/role-crud/spec-historica.md, .spec/features/role-crud/tasks-historicas.md, .spec/features/role-crud/executar-tarefas-antigo.sh, .spec/features/role-crud/plano-execucao-antigo.html, .spec/features/role-crud/plano-execucao-antigo.md, .spec/features/role-crud/plano-antigo.json, .spec/features/role-removal/spec.md
- Notas: Arquivar o CRUD e os planos antigos sem apagar rastreabilidade; atualizar matriz de acesso e documentar que os papéis são definidos pelo sistema.

## T-164 — Registrar testes anotados, verificar e auditar [pendente]
- Refs: US-049, AC-175, AC-176, AC-177, AC-178, AC-179
- Arquivos: src/test/java/com/gap/api/Controller/RoleControllerTest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Controller/AccessControlTest.java, src/test/java/com/gap/api/Integration/RoleServiceIntegrationTest.java, test/run-onp-tests.mjs, .spec/verification/role-removal.json
- Notas: Um teste anotado `@spec:AC-xxx` por critério; executar verify e audit --ci. As decisões Q-039 e Q-040 foram respondidas.
