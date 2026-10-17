package pl.easyoffer.offer_service.service.dataset;

import java.util.List;

enum DatasetExportTable {

    OFFERS(
            "offers",
            "offers.csv",
            List.of(
                    "id", "external_id", "title", "company_name", "location",
                    "experience_level", "employment_type", "work_mode", "working_time",
                    "salary_min", "salary_max", "salary_unit", "currency", "salary_type",
                    "source", "deduplication_fingerprint", "url", "language", "category",
                    "published_at", "expired_at", "open_to_hire_ukrainians", "version",
                    "created_at", "updated_at"
            )
    ),
    TECHNOLOGIES(
            "technologies",
            "technologies.csv",
            List.of("id", "name", "canonical_name", "normalization_version", "level")
    ),
    OFFER_TECHNOLOGIES(
            "offerTechnologies",
            "offer-technologies.csv",
            List.of("job_offer_id", "technology_id")
    ),
    SYNCHRONIZATION_RUNS(
            "synchronizationRuns",
            "synchronization-runs.csv",
            List.of(
                    "id", "cycle_id", "source", "status", "started_at", "finished_at",
                    "fetched_offers", "persisted_offers", "created_offers", "updated_offers",
                    "skipped_offers", "error_type", "error_message_present"
            )
    );

    private final String countName;
    private final String fileName;
    private final List<String> headers;

    DatasetExportTable(String countName, String fileName, List<String> headers) {
        this.countName = countName;
        this.fileName = fileName;
        this.headers = headers;
    }

    String countName() {
        return countName;
    }

    String fileName() {
        return fileName;
    }

    List<String> headers() {
        return headers;
    }
}
