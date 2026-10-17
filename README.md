[EO-1] Add synchronizer based scheduler

[EO-2] Review parsed just join it parameters

[EO-3] Fix searching

[EO-4] Add TheProtocolIt integration

[EO-5] Add Docker file

[EO-6] Add Nofluffjobs integration | WIP

[EO-7] Implement POC of statistics api

## Local database

The application uses PostgreSQL and applies versioned Flyway migrations during startup.

Start the local database:

```shell
docker compose up -d postgres
```

The default local connection is `jdbc:postgresql://127.0.0.1:5433/offerdb` with user
`offer_user` and password `offer_password`. Override it with
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and
`SPRING_DATASOURCE_PASSWORD` outside local development.

Run tests with the isolated `test` profile; it uses an in-memory H2 database in
PostgreSQL compatibility mode and applies the same Flyway migrations.

## Immutable research dataset export

The dataset export endpoint is disabled by default. Enable it only for an
administrative export and store the output on a persistent volume:

```shell
DATASET_EXPORT_ENABLED=true
DATASET_EXPORT_DIRECTORY=/data/datasets
DATASET_EXPORT_API_KEY=<at-least-32-random-characters>
```

Create a dataset with an explicit unique identifier and the exact Git revision
used to build the running application:

```shell
curl -X POST http://127.0.0.1:8080/v1.0/admin/datasets \
  -H "Content-Type: application/json" \
  -H "X-Dataset-Export-Key: $DATASET_EXPORT_API_KEY" \
  -d '{"datasetId":"easyoffer-2026-10-final","codeRevision":"<git-sha>"}'
```

Each dataset is published as a new directory and cannot overwrite an existing
dataset ID. It contains deterministic CSV exports of offers, technologies,
offer-to-technology links and synchronization audit rows, plus `manifest.json`
and `manifest.sha256`. The manifest records row counts, per-file SHA-256 hashes,
the overall dataset fingerprint, source counts, collection window, Flyway schema
version, normalization versions and code revision. Synchronization error message
contents are intentionally excluded from the export.
