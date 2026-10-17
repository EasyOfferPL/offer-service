package pl.easyoffer.offer_service.model.to;

import java.util.List;

public record DatasetNormalizationVersionsTO(
        List<String> technology,
        String deduplicationFingerprint,
        String salaryComparability
) {
}
