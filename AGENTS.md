# AGENTS.md

Instruções para agentes de IA (Claude, Copilot, Cursor, Codex etc.) que trabalham neste repositório.
Leia este arquivo antes de alterar qualquer código.

## 1. Visão geral do projeto

- **Nome:** gap-api
- **Descrição:** API de solicitações acadêmicas (CRUDs, entidades e fluxo de decisões). `[ajustar]`
- **Repositório:** github.com/GAP/gap-api
- **Branch principal:** `main`

## 2. Stack técnica

- Java `21`
- Spring Boot 
- Build: `Gradle`
- Banco de dados: `PostgreSQL`
- Testes: JUnit 5 e Mockito
- FlyWay

## 3. Como rodar

```bash
# compilar
./gradlew build

# executar testes
./gradlew test

# subir a aplicação
./gradlew bootRun
```

Nunca invente comandos. Se algum comando acima estiver errado, corrija o arquivo junto com a mudança.

## 4. Estrutura de pacotes

Siga a organização já existente. `[ajustar conforme o projeto real]`

```
src/main/java/.../
├── config/        # arquivos de configuração
├── controller/    # endpoints REST, sem regra de negócio
├── service/       # regras de negócio
├── repository/    # acesso a dados (Spring Data)
├── model/entity   # entidades JPA
├── model/entity   # objetos de entrada e saída
└── exception/     # exceções e handlers globais
```

Regras:
- Controllers não acessam repositories diretamente; passam pelo service.
- Entidades não vazam para a resposta da API; use DTOs.
- Não crie novos pacotes ou camadas sem necessidade clara.

## 5. Padrão de resposta da API

Todas as respostas usam o envelope `BaseResponse`:

```java
BaseResponse {
    String message;
    String status;
    Object data;
}
```

- Mantenha esse formato em todos os endpoints, inclusive em erros.
- Não crie outro formato de resposta sem alinhar antes.
- Não deixe em hipoteses de erro mensagens genéricas como "Internal Server Error" ou "Bad Request". Sempre retorne uma mensagem clara e útil.
- Não vaze a stack trace ou detalhes internos do sistema na resposta da API.


## 6. Convenções de código

- Nomes de classes em PascalCase, métodos e variáveis em camelCase, constantes em UPPER_SNAKE_CASE.
- Nomes de pacotes, endpoints e campos de API em inglês, seguindo o código existente.
- Mensagens de comentários e documentação podem ser em português, mantendo o padrão já usado no projeto.
- Prefira injeção por construtor.
- Não deixe código comentado, `System.out.println` ou imports não usados.
- Não adicione dependências novas sem justificar no PR.
- A aplicação deve conter logs claros e úteis, usando `Logger` do Spring Boot. Evite logs excessivos em produção.
- Os logs devem ser pontuais nos lugares necessários, evitando excessos e poluição.
- Ao se deparar com um problema, siga boas práticas e procure design patterns antes de inventar soluções complexas. Se não houver solução clara, pergunte antes de implementar algo grande.

## 7. CRUDs e entidades

- Antes de criar um CRUD, confirme que a entidade não depende de outras para ser criada (ou que as dependências já existem).
- Cada CRUD deve ter: entidade, repository, service, DTOs, controller e testes.
- Operações de escrita devem validar entrada (Bean Validation ou validação no service).
- Retorne `404` quando o registro não existir e use o `BaseResponse` com mensagem clara.

## 8. Testes

- Todo service novo ou alterado deve ter testes unitários com **Mockito**, mockando os repositories.
- Nomeie os testes no padrão `metodo_cenario_resultadoEsperado` `[ajustar se houver outro padrão]`.
- Cubra caminho feliz, entidade não encontrada e validações.
- Não rode testes de integração pesados sem necessidade; use o escopo unitário por padrão.
- Antes de considerar uma tarefa concluída, rode os testes e confirme que passam.
- Sempre crie os testes de falha antes de implementar a solução, para garantir que o código falha nos casos esperados.

## 9. Git e fluxo de trabalho

- Crie branches a partir de `main` no formato `feat/<descrição-curta>`(em features) ou `fix/<descricao-curta>`(em ajustes).
  Exemplo: `feat/basic-cruds`.
- Commits pequenos e com mensagem no imperativo, em português ou inglês (mantenha o padrão do histórico).
- Nunca faça push direto na `main`.
- Não commite arquivos de configuração com segredos (senhas, tokens, chaves). Use variáveis de ambiente.
- Commite baseado nas subtarefas definidas, portanto, sempre que finalizar uma subtarefa, realize o commit.

## 10. Limites e cuidados

- Não altere migrations, schema do banco ou configurações de produção sem pedido explícito.
- Não remova testes existentes para fazer o build passar.
- Se a tarefa for ambígua, pergunte antes de implementar uma grande mudança.
- Ao terminar, resuma o que foi alterado, quais testes foram rodados e o que ficou pendente.
- Mudanças autorizadas no schema do banco devem ser versionadas utilizando o FlyWay

## 11. Estado atual (opcional)

Mantenha esta seção curta e atualizada, ou remova-a.

- Trabalho em andamento: CRUDs das entidades básicas (`feature/15-basic-cruds`).
- Pendências: testes unitários dos services com Mockito.