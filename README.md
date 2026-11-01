# Serviço de ativação de produtos

Desenvolvi esta solução para o desafio técnico de Java Developer Pleno/Sênior. Meu objetivo foi entregar uma API REST clara e funcional para registrar ativações de produtos, consultar seu status e integrar com um provedor externo. Priorizei requisitos obrigatórios, regras de negócio explícitas, tratamento de erros e testes dos comportamentos mais importantes.

## Tecnologias que utilizei

- Java 21, Spring Boot 3.5 e Maven;
- Spring Web, Bean Validation e Problem Details;
- Spring Data JPA/Hibernate, PostgreSQL e Flyway;
- H2 nos testes, JUnit 5, Mockito, MockMvc e Spring Boot Test;
- Springdoc OpenAPI/Swagger, Actuator, Docker, Docker Compose e WireMock;
- GitHub Actions para build e testes a cada push ou pull request.

## Como organizei a solução

- `api`: controllers, DTOs e tratamento de exceções HTTP;
- `config`: propriedades e configuração do cliente HTTP;
- `domain`: entidade, status e regras de transição;
- `integration`: comunicação com o provedor externo;
- `repository`: persistência JPA;
- `service`: casos de uso e orquestração.

Mantive os controllers sem regra de negócio: eles recebem e validam a requisição, depois delegam para os serviços. Coloquei as regras de transição no domínio e mantive a chamada HTTP externa fora da transação de banco.

## Pré-requisitos

Para executar sem containers, eu utilizaria JDK 21, PostgreSQL 16 ou superior e acesso à Internet na primeira execução. Também deixei o Maven Wrapper versionado; por isso, não é necessário instalar Maven globalmente. No Windows, uso `./mvnw.cmd`; em Linux/macOS, `./mvnw`. Para executar a stack completa em containers, é necessário Docker Desktop com Docker Compose.

## Como executar

### Com Docker Compose

```powershell
docker compose up --build
```

Com esse comando, subi PostgreSQL, a aplicação e um WireMock. A API fica em `http://localhost:8080`, e o Swagger fica em `http://localhost:8080/swagger-ui/index.html`. O WireMock simula sucesso e responde `204` para `POST /provider/v1/activate`.

### Localmente

Eu criaria um banco PostgreSQL vazio chamado `activation` e executaria:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/activation"
$env:DB_USERNAME="activation"
$env:DB_PASSWORD="sua-senha"
./mvnw.cmd spring-boot:run
```

| Variável | Padrão | Como a utilizei |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/activation` | URL JDBC do banco |
| `DB_USERNAME` | `activation` | Usuário do banco |
| `DB_PASSWORD` | `activation` | Senha do banco |
| `PROVIDER_BASE_URL` | `http://localhost:8081` | URL-base do provedor |
| `PROVIDER_CONNECT_TIMEOUT` | `2s` | Timeout de conexão |
| `PROVIDER_READ_TIMEOUT` | `5s` | Timeout de resposta |

Configurei o Flyway para aplicar as migrações automaticamente. Não versione senhas nem segredos.

## Endpoints

### Criar solicitação

`POST /api/v1/requests`

```json
{"externalId":"EXT-12345","customerId":"CUST-10001","productCode":"INTERNET-500"}
```

Quando a solicitação é nova, retorno `201 Created`, o header `Location`, UUID, status e datas em UTC/ISO-8601. Validei os três campos como obrigatórios, com até 100 caracteres e apenas letras, números, `_` e `-`.

Quando o provedor aceita a ativação, a resposta de criação contém `status: "COMPLETED"`. Quando o provedor responde com erro HTTP tratável ou ocorre falha de comunicação, a solicitação é persistida com `status: "FAILED"` e esse é o status retornado na resposta. Não retorno `PENDING` como resposta de uma ativação síncrona já finalizada.

### Consultar solicitação

`GET /api/v1/requests/{id}` retorna a solicitação e seu status. Quando o UUID não existe, retorno `404 Not Found`. Para validação, ausência e conflito, retornei respostas `application/problem+json`, sem expor stack traces ou detalhes internos.

## Minha decisão sobre status e duplicidade

Modelei apenas as transições `PENDING → PROCESSING → COMPLETED` e `PROCESSING → FAILED`.

Minha garantia principal contra duplicidade está na constraint `UNIQUE` de `external_id` no banco. A consulta antes da gravação melhora o caminho comum, mas não considero que ela seja suficiente: duas chamadas simultâneas ainda poderiam consultar antes de qualquer uma gravar. A constraint do banco resolve essa corrida, inclusive entre múltiplas instâncias da aplicação.

- Para o mesmo `externalId` com o mesmo conteúdo, considero um retry idempotente e retorno `200 OK` com o recurso existente.
- Para o mesmo `externalId` com dados diferentes, retorno `409 Conflict`.
- Para duas chamadas idênticas em paralelo, uma grava e a outra trata a violação de unicidade, consultando o recurso já persistido.

Também usei `@Version` para detectar atualizações concorrentes. Registrei a decisão em [ADR 0001](docs/adr/0001-duplicidade-e-integracao.md).

## Minha estratégia para o provedor externo

Depois de persistir e confirmar a transação de criação, marco a solicitação como `PROCESSING` e chamo `POST /provider/v1/activate`. Envio o UUID interno no header `Idempotency-Key` para permitir que o provedor também trate duplicidade.

| Cenário | Status que defini | Retry automático |
| --- | --- | --- |
| HTTP 2xx | `COMPLETED` | Não é necessário |
| HTTP 4xx | `FAILED` | Não |
| HTTP 5xx | `FAILED` | Não nesta versão |
| Timeout/falha inesperada | `FAILED` | Não |

Escolhi não fazer retry automático cego. Um timeout não confirma que o provedor deixou de processar a ativação; ele pode ter concluído e a resposta ter se perdido. Em uma evolução para produção, eu implementaria outbox transacional, worker assíncrono, backoff com jitter, limite de tentativas e reconciliação pela chave de idempotência.

No código, diferenciei respostas HTTP não bem-sucedidas do provedor, falhas de comunicação (incluindo timeout) e respostas que não podem ser interpretadas pelo cliente. Apenas essas falhas conhecidas do provedor levam a solicitação para `FAILED`. Uma falha interna, como erro ao persistir `COMPLETED`, não é tratada como falha do provedor: ela sobe como erro interno, gera `ProblemDetail` com `traceId` e fica registrada no log para diagnóstico.

## Testes que implementei

```powershell
./mvnw.cmd test
```

Implementei testes para criação válida, validação de entrada, consulta inexistente, unicidade no banco, transições de status e os cenários de sucesso e falha na orquestração do provedor. Também cobri o fluxo HTTP completo com um provedor local real, a resposta `COMPLETED`, a resposta `FAILED`, retry idempotente, conflito de payload, falha de comunicação e concorrência com seis criações simultâneas.

Como próxima evolução, eu adicionaria Testcontainers com PostgreSQL real, WireMock/MockWebServer em testes HTTP e um teste de concorrência com múltiplas threads.

## Segurança, observabilidade e análise de lentidão

Nesta entrega, deixei Spring Security/JWT **documentado, mas não implementado**. Em produção, eu configuraria OAuth 2.0 Resource Server, validaria assinatura/JWKS, emissor, audiência e expiração do JWT. Aplicaria `requests.write` no POST, `requests.read` no GET e isolamento por consumidor para prevenir BOLA. Guardaria as credenciais do provedor em um secret manager com rotação.

Configurei Actuator para expor health, liveness e readiness. Para o cenário de lentidão do desafio, eu começaria separando a latência por dependência: p95/p99 da API, pool de conexões, planos e locks do PostgreSQL, além de latência e taxa de erro do provedor. Como CPU está em 45% e memória em 60%, eu investigaria primeiro uma cascata no provedor, rede ou exaustão de pool. Validaria a correção com carga controlada e compararia latência, erros e saturação antes e depois do deploy.

## Pontos pendentes e evolução

Se eu evoluísse esta solução, priorizaria filtros paginados por status e cliente; JWT funcional; outbox, retentativas seguras e reconciliação; métricas Prometheus, logs JSON, tracing OpenTelemetry e CI mais completo; Testcontainers e testes de concorrência.

## Uso de IA

Utilizei IA como apoio para estruturar a solução, revisar decisões e redigir a documentação. Revisei o código e a documentação para garantir que descrevam somente o comportamento que está implementado neste repositorio.
