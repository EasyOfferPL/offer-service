package pl.easyoffer.offer_service.model.to;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DatasetManifestTO(
        String manifestVersion,
        String datasetId,
        String datasetFingerprint,
        Instant exportedAt,
        Instant collectionStartedAt,
        Instant collectionEndedAt,
        String codeRevision,
        String schemaVersion,
        DatasetNormalizationVersionsTO normalizationVersions,
        Map<String, Long> recordCounts,
        Map<String, Long> offerCountsBySource,
        List<DatasetExportFileTO> files
) {
}
