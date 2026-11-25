# Tasks: Order create

> feature: order-create

## T-004 — Vincular o pedido ao usuário autenticado [concluida]
- Refs: US-004, AC-004, AC-006, AC-013
- Arquivos: src/main/java/com/gap/api/Controller/OrderController.java, src/main/java/com/gap/api/Service/Interface/IOrderService.java, src/main/java/com/gap/api/Service/OrderService.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java
- Notas: Resolver o usuário pelo e-mail OIDC e rejeitar `userId` diferente com HTTP 403.

## T-005 — Retornar 503 se não existir template e manter rollback [concluida]
- Refs: US-004, AC-009
- Arquivos: src/main/java/com/gap/api/Service/WorkflowEngineService.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java, src/test/java/com/gap/api/Integration/OrderServiceIntegrationTest.java
- Notas: Falha do workflow deve reverter o banco e limpar objetos finais movidos.

## T-006 — Anotar os critérios, corrigir compilação e executar a suíte direcionada [concluida]
- Refs: US-004, AC-004, AC-005, AC-006, AC-013, US-005, AC-007, AC-008, AC-009, US-006, AC-010, AC-011, AC-012
- Arquivos: src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java, src/test/java/com/gap/api/Service/WorkflowEngineServiceTest.java, src/test/java/com/gap/api/Service/WorkflowTemplateServiceTest.java, src/test/java/com/gap/api/Integration/OrderServiceIntegrationTest.java, test/run-onp-tests.mjs, onpspec.config.json
- Notas: Adicionar anotações `@spec:AC-xxx`, atualizar as chamadas de `WorkflowStepRequest` e rodar testes Java/Javascript usados pelo verificador.

## T-055 — Validar o contrato JSON e a quantidade de anexos [concluida]
- Refs: US-049, AC-175, AC-176, AC-177
- Arquivos: src/main/java/com/gap/api/Model/DTO/OrderRequest.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java
- Notas: Preservar `extraData` opcional como JSON e rejeitar corpo malformado; aceitar no máximo 10 anexos, sem mover arquivos nem persistir pedido em caso de excesso.

## T-056 — Restringir tamanho e tipos na autorização de upload [concluida]
- Refs: US-049, AC-178
- Arquivos: src/main/java/com/gap/api/Model/DTO/AnnexUploadRequest.java, src/main/java/com/gap/api/Service/AWSStorageService.java, src/main/java/com/gap/api/Service/Interface/IStorageService.java, src/main/java/com/gap/api/Service/OrderService.java, src/main/java/com/gap/api/Controller/OrderController.java, src/test/java/com/gap/api/Service/AWSStorageServiceTest.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, test/run-onp-tests.mjs
- Notas: Permitir apenas `application/pdf`, `image/jpeg`, `image/png` e `application/vnd.openxmlformats-officedocument.wordprocessingml.document`; assinar política S3 com tamanho máximo de 5 MiB e verificar o tamanho do objeto antes de movê-lo.

## T-057 — Vincular chaves temporárias ao usuário autenticado [concluida]
- Refs: US-049, AC-179
- Arquivos: src/main/java/com/gap/api/Controller/OrderController.java, src/main/java/com/gap/api/Service/Interface/IOrderService.java, src/main/java/com/gap/api/Service/OrderService.java, src/main/java/com/gap/api/Service/Interface/IStorageService.java, src/main/java/com/gap/api/Service/AWSStorageService.java, src/test/java/com/gap/api/Controller/OrderControllerTest.java, src/test/java/com/gap/api/Service/OrderServiceTest.java, src/test/java/com/gap/api/Service/AWSStorageServiceTest.java
- Notas: Incluir identificador do usuário na assinatura da chave e verificar titularidade antes de qualquer movimentação ou persistência; fechar a decisão de compatibilidade Q-040 antes de implementar.
