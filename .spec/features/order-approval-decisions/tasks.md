# Tasks: Order approval decisions

> feature: order-approval-decisions

## T-058 — Escrever e executar testes de aceitação [em-andamento]

- Refs: US-052, AC-200, AC-201, AC-202, AC-203, AC-204, AC-205, AC-206, AC-207
- Arquivos: src/test/java/com/gap/api/Service/ApprovalDecisionServiceTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java, src/test/java/com/gap/api/Service/OrderStatusServiceTest.java, src/test/java/com/gap/api/Service/UserServiceTest.java, src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Controller/UserControllerTest.java, test/flyway-migration.test.js, test/run-onp-tests.mjs
- Notas: testes Mockito e HTTP cobrem aprovação, reprovação, autorização por unidade, ordem sequencial e conclusão manual.

## T-059 — Vincular usuários às unidades organizacionais [em-andamento]

- Refs: US-052, AC-204, AC-207
- Arquivos: src/main/java/com/gap/api/Model/Entity/User.java, src/main/java/com/gap/api/Model/DTO/UserOrgUnitsRequest.java, src/main/java/com/gap/api/Model/DTO/UserOrgUnitsResponse.java, src/main/java/com/gap/api/Service/UserService.java, src/main/java/com/gap/api/Service/Interface/IUserService.java, src/main/java/com/gap/api/Controller/UserController.java, src/main/java/com/gap/api/Config/SecurityConfig.java, src/main/resources/db/migration/V8__order_approval_decisions.sql
- Notas: usuário pode pertencer a várias unidades; endpoint administrativo atualiza vínculos.

## T-060 — Implementar decisões sequenciais das etapas [em-andamento]

- Refs: US-052, AC-200, AC-201, AC-202, AC-204, AC-205
- Arquivos: src/main/java/com/gap/api/Model/Entity/ApprovalStage.java, src/main/java/com/gap/api/Model/DTO/ApprovalDecisionRequest.java, src/main/java/com/gap/api/Model/DTO/ApprovalStageResponse.java, src/main/java/com/gap/api/Model/DTO/OrderResponse.java, src/main/java/com/gap/api/Repository/ApprovalStageRepository.java, src/main/java/com/gap/api/Service/ApprovalDecisionService.java, src/main/java/com/gap/api/Service/OrderService.java, src/main/java/com/gap/api/Service/WorkflowEngineService.java, src/main/java/com/gap/api/Service/Interface/IOrderService.java, src/main/java/com/gap/api/Controller/OrderController.java
- Notas: justificar reprovações, validar vínculo à unidade e impedir decisão fora de ordem; sem notificações.

## T-061 — Permitir conclusão manual pela secretaria [em-andamento]

- Refs: US-052, AC-203, AC-206
- Arquivos: src/main/java/com/gap/api/Model/Entity/Order.java, src/main/java/com/gap/api/Service/OrderStatusService.java, src/main/java/com/gap/api/Service/OrderService.java, src/main/java/com/gap/api/Controller/OrderController.java, src/main/java/com/gap/api/Service/Interface/IOrderService.java
- Notas: após entrega ao aluno, somente usuário da secretaria conclui; todas as etapas precisam estar aprovadas.
