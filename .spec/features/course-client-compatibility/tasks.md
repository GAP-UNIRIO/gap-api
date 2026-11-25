# Tarefas: Compatibilidade de clientes do CRUD de cursos

> feature: course-client-compatibility

## T-058 — Provar rejeição de payloads sem vínculos organizacionais [concluida]
- Refs: US-051, AC-181, AC-182, AC-183
- Arquivos: src/test/java/com/gap/api/Controller/CourseControllerTest.java
- Notas: Antes de qualquer ajuste, conferir cobertura existente. Criar casos HTTP anotados que provem HTTP 400 e ausência de gravação para POST e PUT sem qualquer um dos cinco IDs; manter/confirmar caso feliz de PUT com todos os vínculos válidos.

## T-059 — Consolidar contrato e auditar a especificação [concluida]
- Refs: US-051, AC-181, AC-182, AC-183
- Arquivos: .spec/features/course-client-compatibility/spec.md, .spec/features/course-client-compatibility/tasks.md, .spec/features/course-crud/spec.md
- Notas: Sincronizar Q-019 e regras do CRUD de cursos com a decisão confirmada; executar verify da feature e audit global quando os testes anotados estiverem implementados.
