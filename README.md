# MBA Hexagonal Architecture - Cancelamento de Evento

Plataforma de ingressos do curso (Clean Architecture, multi-módulo: `domain`, `application`, `infrastructure`), estendida com a feature de **cancelamento de evento**.

## Requisitos

- Java 17
- Docker (MySQL da aplicação)

## Como subir o projeto

```bash
docker compose up -d              # MySQL na porta 3306
./gradlew :infrastructure:bootRun # API em http://localhost:8080
```

Exemplos:

```bash
curl -X POST localhost:8080/events/{id}/cancel   # cancela o evento
curl localhost:8080/events/{id}                  # consulta (X-Public: true para a versão reduzida)
```

GraphQL disponível em `POST /graphql` (`cancelEvent`, `eventOfId`).

## Como rodar os testes

```bash
./gradlew test
```

Não precisa de Docker: os testes de use case usam repositórios in-memory e os de integração usam H2.

## Onde acontece a cascata de cancelamento

Ao cancelar, o agregado `Event` registra o evento de domínio **`EventCancelled`** (type `event.cancelled`), que é gravado na tabela **outbox** na mesma transação da mudança de status. O `OutboxRelay` publica o evento na fila via `QueueGateway`, e o `ConsumerQueueGateway` o roteia para o **`CancelEventTicketsUseCase`**, que busca os ingressos do evento e os cancela — de forma assíncrona e idempotente, sem os agregados `Event` e `Ticket` se conhecerem.
