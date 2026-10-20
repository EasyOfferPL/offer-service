# EasyOffer backend — local setup

This setup starts only the EasyOffer backend and an isolated PostgreSQL
database. The frontend is not part of this Compose project and should be run
separately by the frontend developer.

External job-portal synchronizers and administrative dataset export are disabled
in this local environment.

## Requirements

- Docker Desktop or Docker Engine with Docker Compose;
- free local port `8080`;
- a clone of the `offer-service` repository.

Java and Maven do not need to be installed on the host. Docker compiles the
backend during the image build.

## Start backend and database

From the `offer-service` directory run:

```shell
docker compose -f compose.backend-local.yaml up --build -d
```

The first build downloads the Java and Maven images and project dependencies,
so it takes longer than later starts.

Check container state:

```shell
docker compose -f compose.backend-local.yaml ps
```

Check the backend contract:

```shell
curl http://localhost:8080/v3/api-docs
```

Useful addresses:

- backend: `http://localhost:8080`;
- Swagger UI: `http://localhost:8080/swagger-ui.html`;
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`;
- OpenAPI YAML: `http://localhost:8080/v3/api-docs.yaml`;
- PostgreSQL is available only inside the Compose network to the backend.

These database credentials are local development values. Do not reuse them in
production and do not copy production `.env` files into this setup.

If database inspection is needed, open `psql` inside the PostgreSQL container:

```shell
docker compose -f compose.backend-local.yaml exec postgres \
  psql -U offer_user -d offerdb
```

## Connect a separately running frontend

Requests made by curl, Postman or another server process can use
`http://localhost:8080` directly. A browser frontend running on another local
port should use its development-server proxy because the backend does not enable
permissive CORS.

Keep the frontend API base URL as `/api` and configure its dev server to remove
that prefix before forwarding requests to `http://localhost:8080`. For example,
with Vite:

```ts
// vite.config.ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api/, ''),
      },
    },
  },
})
```

The frontend calls `/api/offers`, and the backend receives `/offers`. Do not put
`DATASET_EXPORT_API_KEY` or any production secret in the frontend project.

## Add a local demo offer

The database starts empty. This PowerShell request creates one record for offer
list, details and basic analytics development:

```powershell
$body = @{
  externalId = "frontend-demo-001"
  title = "Senior Java Developer"
  companyName = "EasyOffer Demo"
  location = "Warszawa"
  experienceLevel = "SENIOR"
  employmentType = "B2B"
  workMode = "REMOTE"
  workingTime = "FULL_TIME"
  salaryMin = 20000
  salaryMax = 28000
  salaryUnit = "MONTH"
  currency = "PLN"
  salaryType = "NET"
  source = "FRONTEND_DEMO"
  url = "https://example.invalid/offers/frontend-demo-001"
  category = "JAVA"
  technologies = @(
    @{ name = "Java"; level = 5 },
    @{ name = "Spring"; level = 4 }
  )
  language = "English"
  publishedAt = "2026-10-20T10:00:00"
  openToHireUkrainians = $false
} | ConvertTo-Json -Depth 4

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8080/offers" `
  -ContentType "application/json" `
  -Body $body
```

The same `externalId` and `source` update this local record instead of creating
another source-scoped identity.

## Logs, stop and reset

Follow backend logs:

```shell
docker compose -f compose.backend-local.yaml logs -f app
```

Stop containers and retain the database:

```shell
docker compose -f compose.backend-local.yaml down
```

Start the existing containers again:

```shell
docker compose -f compose.backend-local.yaml up -d
```

Delete only this local database and start from an empty state:

```shell
docker compose -f compose.backend-local.yaml down -v
docker compose -f compose.backend-local.yaml up --build -d
```

The `down -v` command permanently removes the volume belonging to this local
Compose project. Never use it with the production Compose file.

## Troubleshooting

Show recent backend and database logs:

```shell
docker compose -f compose.backend-local.yaml logs --tail=200 app postgres
```

If a port is occupied, stop the conflicting local service or change only the
host side of the mapping in `compose.backend-local.yaml`, for example
`127.0.0.1:8081:8080`. Keep the container-side ports and datasource hostname
unchanged.
