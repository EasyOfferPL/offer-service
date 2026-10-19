#!/usr/bin/env sh
set -eu

repository_root=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
compose_file="$repository_root/compose.performance.yaml"
dataset_sizes=${DATASET_SIZES:-1000,5000,10000}
warmup_requests=${WARMUP_REQUESTS:-2}
measured_requests=${MEASURED_REQUESTS:-10}
report_directory=${REPORT_DIRECTORY:-target/performance}
code_revision=$(git -C "$repository_root" rev-parse HEAD)
if [ -n "$(git -C "$repository_root" status --porcelain)" ]; then
  working_tree_state=dirty
else
  working_tree_state=clean
fi

export PERFORMANCE_DB_URL=jdbc:postgresql://127.0.0.1:5434/offerdb_performance
export PERFORMANCE_DB_USERNAME=offer_performance
export PERFORMANCE_DB_PASSWORD=offer_performance

cleanup() {
  docker compose -f "$compose_file" down
}
trap cleanup EXIT INT TERM

cd "$repository_root"
docker compose -f "$compose_file" up -d --wait
"$repository_root/mvnw" \
  -Dtest=BoundedPerformanceExperimentIT \
  -Dperformance.enabled=true \
  -Dperformance.confirmDatabase=offerdb_performance \
  -Dperformance.codeRevision="$code_revision" \
  -Dperformance.workingTreeState="$working_tree_state" \
  -Dperformance.datasetSizes="$dataset_sizes" \
  -Dperformance.warmupRequests="$warmup_requests" \
  -Dperformance.measuredRequests="$measured_requests" \
  -Dperformance.reportDirectory="$report_directory" \
  test
