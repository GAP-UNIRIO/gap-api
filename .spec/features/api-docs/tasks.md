# Tasks: Documentação da API (Swagger)

> feature: api-docs

## T-037 — Adicionar o springdoc e publicar a documentação em /doc [concluída]
- Refs: US-038, AC-141, AC-142, AC-143
- Arquivos: build.gradle, src/main/java/com/gap/api/Config/OpenApiConfig.java, src/main/java/com/gap/api/Config/SecurityConfig.java, src/main/resources/application.yaml, src/main/resources/application-prod.yaml, README.md
- Notas: Dependência `springdoc-openapi-starter-webmvc-ui`; `springdoc.swagger-ui.path=/doc`; rotas `/doc`, `/swagger-ui/**` e `/v3/api-docs/**` liberadas sem login; documentação desligada em `prod` (ASM-011).

## T-038 — Testar a documentação e registrar no gate [concluída]
- Refs: US-038, AC-141, AC-142, AC-143
- Arquivos: src/test/java/com/gap/api/Integration/ApiDocsIntegrationTest.java, test/run-onp-tests.mjs
- Notas: Teste de integração com MockMvc no perfil `test`; incluir a classe na lista do `run-onp-tests.mjs`.
