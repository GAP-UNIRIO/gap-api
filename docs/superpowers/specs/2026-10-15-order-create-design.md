# Criação de order com anexos e workflow

## Objetivo

Implementar na branch `feature/order-create`, criada a partir de `origin/main`, o fluxo de criação de uma order com anexos enviados previamente pelo frontend e etapas de aprovação instanciadas a partir de um workflow template.

## Desenho

O frontend solicita autorização de upload individualmente para cada anexo. A API retorna uma URL de POST pré-assinado para AWS S3, os campos exigidos pelo formulário e uma chave opaca reservada sob o prefixo temporário. O frontend envia o arquivo diretamente ao storage.

Na criação da order, o frontend envia os dados do pedido e as chaves temporárias com os metadados dos anexos. A API valida o usuário e a origem de cada chave, transfere cada objeto do prefixo temporário para o prefixo final da order e persiste os registros dos anexos usando as chaves finais. Em seguida, instancia e persiste as approval stages a partir do template específico do curso ou, na ausência dele, do template padrão para o tipo da order.

O storage será encapsulado por uma interface substituível, com implementação inicial orientada a AWS S3. Bucket, região, prefixos temporário/final e segredo para validar chaves serão configurações externas.

## Regras observáveis

1. Cada autorização corresponde a um anexo e retorna URL, chave temporária e campos de POST assinados limitados à chave e ao tipo de conteúdo solicitados.
2. A API rejeita metadados inválidos e falha com erro de serviço indisponível quando o storage não está configurado.
3. A criação aceita apenas chaves temporárias emitidas pela API; chaves inválidas não provocam cópia nem persistência.
4. A criação valida a existência do usuário antes de mover arquivos. Usuário inexistente resulta em 404 e não persiste order, anexos ou etapas.
5. Para uma order válida, cada arquivo é copiado para o destino final e o objeto temporário é removido; o anexo persistido aponta para a chave final.
6. O workflow específico do curso tem precedência sobre o template padrão. As etapas são criadas na ordem definida pelo template, com status inicial pendente e unidade responsável resolvida pelo curso do usuário.
7. Ausência de template aplicável falha na criação sem persistir order, anexos ou etapas e compensa transferências de arquivos já realizadas.
8. Falha ao copiar qualquer anexo interrompe a criação, não persiste dados e limpa transferências anteriores já concluídas.
9. Se a persistência da order, anexos ou etapas falhar depois das transferências, a transação do banco é revertida e as cópias finais concluídas são removidas.
10. Se a limpeza do objeto temporário falhar após uma cópia, a cópia final é removida e a falha é propagada.

## Limites

- O conteúdo dos arquivos não trafega pela API.
- Consultar ou baixar anexos não faz parte deste fluxo.
- A compensação cobre operações S3 observáveis pela aplicação; falhas simultâneas na operação principal e na compensação serão propagadas e registradas como erro.

## Estratégia de teste

Antes da implementação, criar testes unitários e de controller anotados por critério de aceite para os caminhos de sucesso e falha: autorização válida/inválida e storage não configurado; criação sem/com anexos; usuário inexistente; chave temporária inválida; template específico e fallback padrão; ausência de template; falhas de cópia, limpeza temporária e persistência; rollback de banco e compensação de arquivos finais. Executar os testes antes de implementar e confirmar que falham pelos contratos ausentes ou comportamento ainda não implementado. Depois, implementar até passarem e executar a suíte completa.

## Decisões e pressupostos

- O provedor de produção ainda não foi escolhido; AWS S3 é o alvo inicial e deve permanecer isolado por interface.
- O upload usa POST pré-assinado de curta duração e uma chave aleatória autenticada pela API.
- O endpoint de criação recebe um anexo por item, contendo nome, tipo de conteúdo e chave temporária.
- O padrão do domínio já existente para respostas, autenticação e identificadores será mantido.
- O workflow engine existente na `main` será integrado ao serviço de criação, sem duplicar sua lógica de seleção/instanciação.
