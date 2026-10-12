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
