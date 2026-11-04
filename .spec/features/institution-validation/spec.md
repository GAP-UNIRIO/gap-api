# Especificação: validação de instituições

> feature: institution-validation
> status: implementada

## Contexto

Gestores precisam cadastrar instituições com identificadores CNPJ consistentes e configurações JSON que a API consiga persistir com limites previsíveis.

## Histórias

### US-035 — Cadastrar instituições com dados seguros

Como gestor, quero que a API valide o CNPJ e as configurações antes de persistir uma instituição, para evitar identificadores ambíguos e configurações inválidas.

#### AC-126 — Validar e normalizar CNPJ
- **Dado** CNPJ válido com ou sem pontuação
- **Quando** o cliente cria ou atualiza uma instituição
- **Então** a API aceita o valor e retorna e persiste apenas os 14 dígitos

#### AC-127 — Rejeitar CNPJ inválido
- **Dado** código que não tenha 14 dígitos ou dígitos verificadores válidos
- **Quando** o cliente cria ou atualiza uma instituição
- **Então** recebe HTTP 400 e nenhuma instituição é persistida ou alterada

#### AC-128 — Evitar duplicidade por representação
- **Dado** instituição com um CNPJ cadastrado
- **Quando** o cliente tenta cadastrar ou atualizar usando o mesmo CNPJ com outra pontuação
- **Então** recebe HTTP 409 e não altera os dados existentes

#### AC-129 — Limitar mapa de configurações
- **Dado** configurações JSON opcionais com até 16 KiB serializados e profundidade máxima 8
- **Quando** o cliente cria ou atualiza uma instituição
- **Então** a API aceita mapas com chaves textuais e valores JSON nulos, textuais, numéricos, booleanos, mapas ou listas

#### AC-130 — Rejeitar configurações inválidas
- **Dado** configurações acima de 16 KiB, profundidade acima de 8 ou valor que não pertença aos tipos JSON permitidos
- **Quando** o cliente cria ou atualiza uma instituição
- **Então** recebe HTTP 400 e nenhuma instituição é persistida ou alterada

#### AC-131 — Converter colisão concorrente do CNPJ
- **Dado** duas requisições concorrentes para o mesmo CNPJ e a consulta prévia não detecta duplicidade
- **Quando** o banco rejeita uma gravação pela constraint única de código
- **Então** a requisição perdedora recebe HTTP 409 com o envelope BaseResponse

## Fora de escopo

- Restringir chaves ou formatos internos de `settings`.
- Alterar migrations ou schema do banco.

## Suposições

Nenhuma. O CNPJ e os limites de configurações foram confirmados pelo solicitante.

## Perguntas em aberto

Nenhuma.


