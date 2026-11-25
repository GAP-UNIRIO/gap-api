# Especificação: Departamento obrigatório nos cursos

> feature: course-department-compatibility
> status: rascunho

## Contexto

A API já exige `departmentId` em criação e atualização, enquanto `V2__add_course_department.sql` adicionou `courses.department_id` anulável. Como a aplicação ainda não está em produção e o banco atual foi descartado, a V7 conclui a regra no banco e impede cursos sem departamento. Bases descartáveis devem ser recriadas com as migrations atualizadas.

## Histórias

### US-049 — Exigir departamento para cada curso

Como mantenedor da API, quero que cada curso tenha um departamento válido tanto na API quanto no banco, para impedir registros incompletos e manter a regra consistente em todos os caminhos de escrita.

#### AC-175 — Criação e atualização exigem departamento

- **Dado** uma requisição de criação ou atualização sem `departmentId`
- **Quando** o cliente envia `POST /courses` ou `PUT /courses/{id}`
- **Então** a API responde HTTP 400 com mensagem clara e não salva nem altera o curso

#### AC-176 — Banco rejeita curso sem departamento

- **Dado** uma tentativa de inserir ou atualizar um curso diretamente no banco com `department_id` nulo
- **Quando** a transação é executada
- **Então** a restrição `NOT NULL` rejeita a gravação

#### AC-177 — Associação requer unidade do tipo departamento

- **Dado** uma requisição com `departmentId` inexistente ou associado a unidade que não seja `DEPARTAMENTO`
- **Quando** a API valida a criação ou atualização
- **Então** responde HTTP 400 com mensagem clara e não grava a alteração

### US-050 — Recriar a base com a nova restrição

Como responsável pela implantação, quero recriar o banco com o modelo atualizado quando os dados existentes puderem ser descartados, para iniciar com cursos sempre associados a departamentos corretos.

#### AC-178 — Migration não inventa departamentos

- **Dado** uma base com curso cujo `department_id` está nulo
- **Quando** a migration que aplica a obrigatoriedade é executada
- **Então** a migration não preenche o departamento automaticamente e o banco impede a aplicação da restrição enquanto houver esse curso

#### AC-179 — Migration aplica NOT NULL à coluna de departamento

- **Dado** uma base recriada e sem cursos incompletos
- **Quando** a migration de obrigatoriedade é executada
- **Então** `courses.department_id` fica `NOT NULL` e a chave estrangeira para `org_units(id)` permanece ativa

#### AC-180 — Cursos com departamento continuam disponíveis

- **Dado** um curso associado a uma unidade `DEPARTAMENTO`
- **Quando** clientes consultam, listam ou atualizam esse curso
- **Então** o curso permanece disponível e a resposta inclui o resumo do departamento (`id`, `name`)

## Regras

- A coluna `courses.department_id` será obrigatória após uma migration Flyway nova; migrations já aplicadas não serão editadas.
- Nenhum departamento será atribuído automaticamente a registros existentes.
- A migration aplica `NOT NULL`; PostgreSQL rejeita a operação se ainda houver linhas nulas. Nenhum departamento é preenchido automaticamente.
- A API só aceita `OrgUnitType.DEPARTAMENTO` existente.
- Respostas seguem o envelope `BaseResponse`; erros não expõem detalhes internos.

## Fora de escopo

- Atribuir departamentos em lote por heurística.
- Mudar o tipo de departamento ou remover a chave estrangeira.

## Suposições

| ID | Suposição | Status | Resolução |
|---|---|---|---|
| ASM-040 | Não há cliente ou ambiente de produção dependendo de cursos sem departamento. | confirmada | O usuário informou que a aplicação ainda não está em produção. |
| ASM-041 | Os dados atuais do banco podem ser descartados; o banco será recriado após a alteração do schema. | confirmada | O usuário informou que pretende dropar e recriar o banco com as entidades e associações corretas. |

## Perguntas em aberto

| ID | Pergunta | Status | Resposta |
|---|---|---|---|
| Q-040 | Dropar e recriar o banco resolve a presença de cursos legados sem departamento? | respondida | Sim; o usuário descartou o banco e a V7 aplica `NOT NULL` ao recriar a base pelas migrations. |
| Q-041 | Como será aplicada a associação manual em cada ambiente: atualização SQL revisada, endpoint administrativo existente ou edição pela API? | respondida | Não haverá associação manual; o banco será descartado e recriado com as relações corretas. |


