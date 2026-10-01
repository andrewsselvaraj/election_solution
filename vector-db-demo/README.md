# Vector DB Demo (Spring Boot)

A small, standalone Spring Boot 4 project for testing a vector database.
It turns text into embeddings, stores them, and finds documents by **meaning** instead of exact words.

- **Embedding model:** `all-MiniLM-L6-v2` from LangChain4j. It runs locally inside the JVM, so you don't
  need an API key or network access after the Maven download. It produces 384-dimension vectors.
- **Vector store:** chosen with `vector.store`
  - `memory` (default): LangChain4j `InMemoryEmbeddingStore`, nothing to install
  - `pgvector`: PostgreSQL with the [pgvector](https://github.com/pgvector/pgvector) extension

## Requirements

- Java 21+
- Docker (only for pgvector mode and the pgvector test)

## Run (in-memory)

```bash
cd vector-db-demo
./mvnw spring-boot:run
```

Open http://localhost:8081 for a simple search page. At startup it loads 12 sample documents
from `src/main/resources/sample-docs.txt`.

## Run with PostgreSQL + pgvector

```bash
cd vector-db-demo
docker compose up -d                                   # pgvector/pgvector:pg17 on localhost:5432
SPRING_PROFILES_ACTIVE=pgvector ./mvnw spring-boot:run
```

The `documents` table is created automatically. Data is kept between restarts, and sample data is
loaded only when the table is empty. To inspect the data:

```bash
docker compose exec pgvector psql -U vector -d vectordb -c "select text, metadata from documents;"
```

Connection settings are under `vector.pgvector.*` in `application.properties`.

## REST API

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/documents` | Add a document. Body: `{"text": "...", "category": "optional"}`. Returns `{"id": "..."}` |
| `GET` | `/api/search?q=...&k=3&minScore=0.0&category=...` | Return the `k` closest documents with their similarity scores |
| `DELETE` | `/api/documents/{id}` | Delete one document |
| `DELETE` | `/api/documents` | Delete all documents |

```bash
curl "localhost:8081/api/search?q=fast%20animal&k=2"
curl -X POST localhost:8081/api/documents -H 'Content-Type: application/json' \
     -d '{"text":"Tea is brewed from the leaves of the Camellia sinensis plant.","category":"food"}'
curl "localhost:8081/api/search?q=hot%20drink&category=food"
```

## Test

```bash
./mvnw test
```

- `VectorServiceTest` tests semantic search, ordering, the category filter, `minScore`, and delete,
  using the real embedding model and an in-memory store.
- `VectorControllerTest` starts the full app and tests the REST API.
- `PgVectorStoreTest` starts a real `pgvector/pgvector:pg17` container with Testcontainers.
  It is **skipped automatically** when Docker isn't available.

## Structure

```
src/main/java/com/example/vectordb
├── VectorDbDemoApplication.java
├── config/
│   ├── VectorProperties.java     # vector.* settings
│   └── VectorStoreConfig.java    # embedding model + in-memory / pgvector store beans
├── service/
│   ├── VectorService.java        # add, search, delete
│   └── SampleDataLoader.java     # loads sample-docs.txt at startup
└── controller/
    └── VectorController.java     # REST API under /api
```
