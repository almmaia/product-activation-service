# ADR 0001 — Duplicidade e integração externa

## Contexto

O sistema de origem pode reenviar uma solicitação e duas chamadas podem chegar quase simultaneamente. Além disso, uma chamada HTTP externa não deve manter uma transação de banco aberta.

## Decisão

Usei `external_id` com constraint `UNIQUE` como fonte de verdade para idempotência. A aplicação verifica o registro previamente para o caminho comum, mas sempre depende da constraint para a corrida concorrente. Retry com payload idêntico retorna o recurso existente; payload diferente retorna conflito.

A criação é persistida em uma transação independente. Só depois do commit a orquestração muda o estado para `PROCESSING` e aciona o provedor, usando o UUID interno como chave de idempotência. O cliente possui timeouts de conexão e leitura configuráveis.

## Consequências

Evito duplicidade entre instâncias sem lock distribuído e não prendo recursos do banco durante I/O. Ainda existe uma janela entre persistir e publicar a chamada externa em caso de queda do processo; para produção, o próximo passo é uma outbox transacional processada por worker, com reconciliação e retentativas seguras.
