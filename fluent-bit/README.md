# Application log collection

Compose sets Spring Boot's `LOGGING_FILE_NAME` for each service. Console logging
continues, and the shared Docker volume `microservice-logs` contains
`user-service.log`, `order-service.log`, and `notification-service.log` under
`/var/log/microservices`. Spring Boot's default rolling file policy applies.
No Windows host directory is needed for log storage.

Fluent Bit mounts the logs read-only and tails active `*-service.log` files.
Each line becomes a record with `log`, `source_file`, and `@timestamp` (collection
time). Multiline messages, including stack traces, remain separate records.
Daily indices are named `microservice-logs-YYYY.MM.DD`; `source_file` distinguishes
the services. The Kafka pipeline and `application-events` index are independent.
`Suppress_Type_Name On` enables Elasticsearch 8 compatibility.

The `fluent-bit-state` volume persists Tail offsets and filesystem chunks across
container recreation. Unlimited retries and generated document IDs help recover
from temporary Elasticsearch outages without duplicating retried batches.
The output queue is capped at 1 GB; oldest queued chunks are discarded if it fills.
This is best-effort delivery: long outages, rotated/compressed source files,
lines over 1 MB, disk exhaustion, or abrupt failures can still cause loss.
Do not delete either logging volume when retaining pending logs is important.

From the project root:

```powershell
docker compose config -q
docker compose up -d --no-deps user-service order-service notification-service fluent-bit
docker compose logs --tail 50 fluent-bit
docker compose exec -T user-service ls -lh /var/log/microservices
curl.exe "http://localhost:9200/_cat/indices/microservice-logs-*?v"
```

In Kibana at http://localhost:5601, open **Stack Management > Data Views** and
create `microservice-logs-*` with `@timestamp` as the time field. Open **Discover**,
select that data view and a suitable time range, and display `log` and
`source_file`. Filter with, for example:

```text
source_file : "/var/log/microservices/notification-service.log"
```

Keep a separate `application-events` data view using its `timestamp` field for
domain events. No existing Elasticsearch indices or data volumes need changing.
