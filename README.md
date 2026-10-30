# Serviço de ativação de produtos

Esta é a solução que desenvolvi para o desafio técnico de Java Developer Pleno/Sênior. Construí uma API REST para registrar ativações de produtos, consultar seu status e integrar com um provedor externo. Priorizei requisitos obrigatórios, regras explícitas, testes e decisões fáceis de justificar tecnicamente.

## Tecnologias

- Java 21, Spring Boot 3.5 e Maven
- Spring Web, Bean Validation e Problem Details
- Spring Data JPA/Hibernate, PostgreSQL e Flyway
- H2 apenas para testes, JUnit 5, Mockito, MockMvc e Spring Boot Test
- Springdoc OpenAPI/Swagger, Actuator, Docker e WireMock
- GitHub Actions para build e testes a cada push/pull request

## Arquitetura

```text
api/           controllers, DTOs e exceções HTTP
config/        propriedades e cliente HTTP
domain/        entidade, status e transições
integration/   comunicação com o provedor
repository/    persistência JPA
service/       casos de uso e orquestração
```

O controller não contém regra de negócio. As transições ficam no domínio, a persistência fica nos serviços/repositório e a chamada HTTP ocorre fora da transação de banco.

## Pré-requisitos

- JDK 21;
- PostgreSQL 16+ para executar sem containers;
- Internet na primeira execução para dependências Maven;
- Docker Desktop com Compose, somente para a execução conteinerizada.

O Maven Wrapper está versionado: não é preciso instalar Maven globalmente. No Windows, use `./mvnw.cmd`; em Linux/macOS, `./mvnw`.

## Executar

### Docker Compose

```powershell
docker compose up --build
```

O Compose sobe PostgreSQL, aplicação e WireMock. A API fica em `http://localhost:8080`; Swagger em `http://localhost:8080/swagger-ui/index.html`. O WireMock responde `204` para `POST /provider/v1/activate`.

### Localmente

Crie um banco PostgreSQL vazio chamado `activation` e execute:

```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/activation"
$env:DB_USERNAME="activation"
$env:DB_PASSWORD="sua-senha"
./mvnw.cmd spring-boot:run
```

| Variável | Padrão | Descrição |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/activation` | URL JDBC |
| `DB_USERNAME` | `activation` | Usuário do banco |
| `DB_PASSWORD` | `activation` | Senha do banco |
| `PROVIDER_BASE_URL` | `http://localhost:8081` | URL do provedor |
| `PROVIDER_CONNECT_TIMEOUT` | `2s` | Timeout de conexão |
| `PROVIDER_READ_TIMEOUT` | `5s` | Timeout de resposta |

As migrações são aplicadas automaticamente pelo Flyway. Não versione segredos.

## API

### Criar solicitação

`POST /api/v1/requests`

```json
{"externalId":"EXT-12345","customerId":"CUST-10001","productCode":"INTERNET-500"}
```

Um novo pedido retorna `201 Created`, header `Location`, UUID, status e datas UTC/ISO-8601. Os três campos são obrigatórios, aceitam até 100 caracteres e somente letras, números, `_` e `-`.

### Consultar solicitação

`GET /api/v1/requests/{id}` retorna o pedido e seu status. UUID inexistente retorna `404`. Validação, ausência e conflito usam `application/problem+json`, sem stack trace.

## Duplicidade e status

O domínio permite somente:

```text
PENDING → PROCESSING → COMPLETED
                     ↘ FAILED
```

`external_id` possui constraint `UNIQUE`; ela é a garantia real para chamadas simultâneas e múltiplas instâncias. A consulta prévia serve apenas ao caminho comum.

- Mesmo `externalId` e mesmo conteúdo: considero retry idempotente e retorno `200 OK` com o recurso existente.
- Mesmo `externalId` e conteúdo diferente: retorno `409 Conflict`.
- Chamadas idênticas em paralelo: uma grava; a outra trata a violação de unicidade e consulta o registro persistido.

Uso também `@Version` para detectar atualização concorrente. Veja a decisão em [ADR 0001](docs/adr/0001-duplicidade-e-integracao.md).

## Integração externa

Após o commit da criação, o pedido muda para `PROCESSING` e chama `POST /provider/v1/activate` com o UUID interno no header `Idempotency-Key`.

| Cenário | Status | Retry automático |
| --- | --- | --- |
| HTTP 2xx | `COMPLETED` | Não necessário |
| HTTP 4xx | `FAILED` | Não |
| HTTP 5xx | `FAILED` | Não nesta versão |
| Timeout/falha inesperada | `FAILED` | Não |

Não implementei retry cego: em timeout o provedor pode ter ativado o produto, mesmo sem resposta. Para produção, eu evoluiria com outbox transacional, worker, backoff/jitter, tentativas limitadas e reconciliação via chave de idempotência.

## Testes

```powershell
./mvnw.cmd test
```

Cobri criação válida, validação, 404, unicidade no banco, transições e sucesso/falha da integração na orquestração. A suíte foi executada neste ambiente com **7 testes, 0 falhas e 0 erros**.

Melhorias previstas: Testcontainers com PostgreSQL real, WireMock/MockWebServer nos testes HTTP e concorrência com múltiplas threads.

## Segurança, observabilidade e lentidão

Spring Security/JWT está **documentado, não implementado**. Em produção eu usaria OAuth 2.0 Resource Server, validaria assinatura/JWKS, emissor, audiência e expiração; aplicaria `requests.write` no POST, `requests.read` no GET e isolamento por consumidor para prevenir BOLA. Credenciais do provedor ficariam em secret manager com rotação.

Actuator expõe health, liveness e readiness. Para a lentidão relatada (API 300 ms → 2,5 s; banco 50 ms → 1,2 s; provedor 400 ms → 3 s), eu compararia p95/p99 por dependência, pool de conexões, planos/locks/conexões do PostgreSQL, latência/erros do provedor e traces por correlação. Com CPU a 45% e memória a 60%, investigaria primeiro cascata do provedor, rede e exaustão do pool. Validaria a correção com carga controlada e comparação antes/depois de latência, erros e saturação.

## Pendências e evolução

- filtros paginados por status e cliente;
- JWT funcional;
- outbox, retentativas seguras e reconciliação;
- Prometheus, logs JSON, tracing OpenTelemetry e CI;
- Testcontainers e testes concorrentes.

## Uso de IA

Utilizei IA como apoio para estruturar a solução, revisar decisões e redigir a documentação. Revisei o código para que a documentação descreva o comportamento implementado e me preparei para explicar essas escolhas tecnicamente.
