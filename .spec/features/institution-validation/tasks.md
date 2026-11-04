# Tarefas: validação de instituições

> feature: institution-validation

## T-001 — Validar dados e mapear conflitos do CNPJ [concluida]
- Refs: US-035, AC-126, AC-127, AC-128, AC-129, AC-130, AC-131
- Arquivos: `.spec/features/institution-validation/spec.md`, `.spec/features/institution-validation/tasks.md`, `src/main/java/com/gap/api/Model/DTO/InstitutionRequest.java`, `src/main/java/com/gap/api/Validation/ValidCnpj.java`, `src/main/java/com/gap/api/Validation/CnpjValidator.java`, `src/main/java/com/gap/api/Validation/ValidSettings.java`, `src/main/java/com/gap/api/Validation/SettingsValidator.java`, `src/main/java/com/gap/api/Service/InstitutionService.java`, `src/test/java/com/gap/api/Service/InstitutionServiceTest.java`, `src/test/java/com/gap/api/Validation/InstitutionValidationTest.java`, `src/test/java/com/gap/api/Controller/InstitutionControllerTest.java`, `test/run-onp-tests.mjs`
- Notas: validar primeiro critérios de falha; normalizar antes de consultar/salvar; preservar envelope BaseResponse para conflito de unique.


