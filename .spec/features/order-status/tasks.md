# Tasks: Status da solicitação

> feature: order-status

## T-039 — Adicionar o status à solicitação com migration [concluida]
- Refs: US-036, AC-132, AC-136
- Arquivos: src/main/resources/db/migration/V4__add_order_status.sql, src/main/java/com/gap/api/Model/Entity/Order.java, src/main/java/com/gap/api/Model/DTO/OrderResponse.java, src/main/java/com/gap/api/Service/OrderService.java
- Notas: Enum `Order.OrderStatus` gravado como texto, com o mapa de transições no próprio enum (Q-022). Solicitação nasce `ABERTA` (Q-021); existentes viram `EM_ANALISE` na migration (Q-026).

## T-040 — Criar o serviço de transição de status com histórico [concluida]
- Refs: US-036, AC-133, AC-134, AC-135
- Arquivos: src/main/java/com/gap/api/Service/Interface/IOrderStatusService.java, src/main/java/com/gap/api/Service/OrderStatusService.java
- Notas: Transição inválida ou a partir de status final responde 422 (Q-025). Cada mudança grava `OrderHistory` com `eventType = STATUS_CHANGED` e `payload` com origem e destino (ASM-014).

## T-041 — Permitir que o aluno cancele a solicitação aberta [concluida]
- Refs: US-037, AC-137, AC-138, AC-139, AC-140
- Arquivos: src/main/java/com/gap/api/Controller/OrderController.java, src/main/java/com/gap/api/Service/Interface/IOrderService.java, src/main/java/com/gap/api/Service/OrderService.java, src/main/java/com/gap/api/Config/SecurityConfig.java
- Notas: `PATCH /orders/{id}/cancel`; só o dono, só em `ABERTA` (Q-027). `PATCH` incluído nos métodos liberados pelo CORS para o front conseguir chamar o endpoint.

## T-042 — Testar os critérios e registrar no gate [concluida]
- Refs: US-036, AC-132, AC-133, AC-134, AC-135, AC-136, US-037, AC-137, AC-138, AC-139, AC-140
- Arquivos: src/test/java/com/gap/api/Service/OrderStatusServiceTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, test/order-status-migration.test.js, test/run-onp-tests.mjs
- Notas: Testes que falham escritos antes da implementação, com `@spec:AC-xxx` no nome.
