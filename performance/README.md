# F-016 bounded performance experiment

This experiment measures a bounded synthetic workload against a dedicated,
disposable PostgreSQL database. It does not use or modify the development,
production, or research dataset.

The default workload grows one database cumulatively through 1,000, 5,000 and
10,000 offers. Every offer is persisted through
`OfferService.createOrUpdateForSynchronization`. At every scale the runner sends
sequential HTTP requests to a paged offer endpoint and the technology
popularity, technology trend, salary distribution and completeness analytics.
Each endpoint receives two warm-up requests and ten measured requests.

The JSON report records the Git revision and whether the working tree was clean,
Java/OS/CPU/heap metadata, PostgreSQL and driver versions, workload parameters,
incremental and cumulative import throughput, response sizes and
single-client sequential request throughput plus
minimum/P50/P95/maximum/mean response latency. Percentiles use the nearest-rank
method. No machine-dependent latency threshold is asserted, and the experiment
does not claim concurrent-user capacity. A thesis evidence run should use a
clean committed revision; dirty runs remain useful for local verification but
are labelled accordingly.

## Run on Windows

```powershell
.\scripts\run-f016-performance.ps1
```

Optional bounded parameters:

```powershell
.\scripts\run-f016-performance.ps1 `
  -DatasetSizes "1000,5000,10000" `
  -WarmupRequests 2 `
  -MeasuredRequests 10
```

## Run on Linux

```shell
sh ./scripts/run-f016-performance.sh
```

The shell runner accepts `DATASET_SIZES`, `WARMUP_REQUESTS`,
`MEASURED_REQUESTS` and `REPORT_DIRECTORY` environment variables.

Both runners start `compose.performance.yaml`, whose PostgreSQL data directory
is a temporary in-memory filesystem, and stop the dedicated container after the
experiment. The Java guard also requires the exact database name
`offerdb_performance`, explicit confirmation and empty application tables before
inserting any data. The workload is capped at 100,000 offers and 50 warm-up or
measured requests per endpoint.

Reports are written to `target/performance/` as a timestamped JSON file and as
`f016-results-latest.json`. These outputs are technical measurement artifacts;
they become thesis evidence only after the environment, workload and result are
reviewed and registered. They must not be presented as production capacity or
as a result derived from the research dataset.
