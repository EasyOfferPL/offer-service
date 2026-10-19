param(
    [string]$DatasetSizes = "1000,5000,10000",
    [int]$WarmupRequests = 2,
    [int]$MeasuredRequests = 10,
    [string]$ReportDirectory = "target/performance"
)

$ErrorActionPreference = "Stop"
$repositoryRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $repositoryRoot "compose.performance.yaml"
$mavenWrapper = Join-Path $repositoryRoot "mvnw.cmd"
$codeRevision = (git -C $repositoryRoot rev-parse HEAD).Trim()
$workingTreeState = if (git -C $repositoryRoot status --porcelain) { "dirty" } else { "clean" }
$originalLocation = Get-Location

$env:PERFORMANCE_DB_URL = "jdbc:postgresql://127.0.0.1:5434/offerdb_performance"
$env:PERFORMANCE_DB_USERNAME = "offer_performance"
$env:PERFORMANCE_DB_PASSWORD = "offer_performance"

try {
    Set-Location $repositoryRoot
    docker compose -f $composeFile up -d --wait
    & $mavenWrapper `
        "-Dtest=BoundedPerformanceExperimentIT" `
        "-Dperformance.enabled=true" `
        "-Dperformance.confirmDatabase=offerdb_performance" `
        "-Dperformance.codeRevision=$codeRevision" `
        "-Dperformance.workingTreeState=$workingTreeState" `
        "-Dperformance.datasetSizes=$DatasetSizes" `
        "-Dperformance.warmupRequests=$WarmupRequests" `
        "-Dperformance.measuredRequests=$MeasuredRequests" `
        "-Dperformance.reportDirectory=$ReportDirectory" `
        test
    if ($LASTEXITCODE -ne 0) {
        throw "F-016 Maven experiment failed with exit code $LASTEXITCODE"
    }
}
finally {
    docker compose -f $composeFile down
    Set-Location $originalLocation
}
