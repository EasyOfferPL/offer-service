package pl.easyoffer.offer_service.config;

public final class SwaggerDescription {

    public static final String API_TITLE = "EasyOffer backend API";
    public static final String API_VERSION = "v1";
    public static final String API_DESCRIPTION = "Runtime-generated contract for EasyOffer API v1. "
            + "Analytics date ranges use inclusive YYYY-MM-DD values. "
            + "Offer list endpoints return the stable EasyOffer page wrapper.";
    public static final String DATASET_EXPORT_KEY_DESCRIPTION = "Administrative dataset-export key. "
            + "Never embed it in a frontend bundle.";

    public static final String RESPONSE_OK = "200";
    public static final String RESPONSE_CREATED = "201";
    public static final String RESPONSE_BAD_REQUEST = "400";
    public static final String RESPONSE_FORBIDDEN = "403";
    public static final String RESPONSE_NOT_FOUND = "404";
    public static final String RESPONSE_CONFLICT = "409";
    public static final String RESPONSE_INTERNAL_SERVER_ERROR = "500";
    public static final String UNEXPECTED_SERVER_ERROR = "Unexpected server error";

    public static final String EXAMPLE_CATEGORY_JAVA = "JAVA";
    public static final String EXAMPLE_DATE_FROM = "2026-10-01";
    public static final String EXAMPLE_DATE_FROM_TREND = "2026-07-01";
    public static final String EXAMPLE_DATE_TO = "2026-10-31";
    public static final String EXAMPLE_FIRST_PAGE = "0";
    public static final String EXAMPLE_PAGE_SIZE = "50";
    public static final String EXAMPLE_JUST_JOIN_IT = "JUST_JOIN_IT";
    public static final String EXAMPLE_THE_PROTOCOL_IT = "THE_PROTOCOL_IT";

    public static final String OFFERS_TAG = "Offers";
    public static final String OFFERS_TAG_DESCRIPTION = "Offer retrieval, filtering and manual ingestion";
    public static final String LIST_OFFERS_SUMMARY = "List offers";
    public static final String LIST_OFFERS_DESCRIPTION = "Returns a stable zero-based page. Maximum page size is 200.";
    public static final String PAGE_RETURNED = "Page returned";
    public static final String INVALID_PAGE_OR_SORT = "Invalid page or sort parameter";
    public static final String GET_OFFER_SUMMARY = "Get an offer by internal ID";
    public static final String OFFER_RETURNED = "Offer returned";
    public static final String OFFER_NOT_FOUND = "Offer not found";
    public static final String OFFER_ID_DESCRIPTION = "Internal offer identifier";
    public static final String EXAMPLE_OFFER_ID = "123";
    public static final String SEARCH_OFFERS_SUMMARY = "Search offers";
    public static final String SEARCH_OFFERS_DESCRIPTION = "Combines supplied filters with logical AND and returns "
            + "a stable zero-based page. List-valued filters accept repeated query parameters. "
            + "Maximum page size is 200.";
    public static final String FILTERED_PAGE_RETURNED = "Filtered page returned";
    public static final String INVALID_FILTER_PAGE_OR_SORT = "Invalid filter, page or sort parameter";
    public static final String CREATE_OFFER_SUMMARY = "Create or update an offer";
    public static final String CREATE_OFFER_DESCRIPTION = "Manual ingestion path. Existing offers may be updated "
            + "according to the current deduplication rules.";
    public static final String OFFER_PERSISTED = "Offer persisted";
    public static final String OFFER_VALIDATION_FAILURE = "Validation or duplicate-processing failure";
    public static final String OFFER_REQUEST_DESCRIPTION = "Offer in the common EasyOffer input model";

    public static final String CATEGORIES_TAG = "Categories";
    public static final String CATEGORIES_TAG_DESCRIPTION = "Categories present in persisted offers";
    public static final String LIST_CATEGORIES_SUMMARY = "List available categories";
    public static final String CATEGORIES_RETURNED = "Categories returned";

    public static final String ANALYTICS_TAG = "Analytics";
    public static final String ANALYTICS_TAG_DESCRIPTION = "Aggregated offer, technology, salary and data-quality views";
    public static final String INVALID_ANALYTICS_PARAMETER = "Invalid filter or request parameter";
    public static final String DASHBOARD_SUMMARY = "Get dashboard summary";
    public static final String DASHBOARD_DESCRIPTION = "Returns category counts and recently updated offers.";
    public static final String LEGACY_TECHNOLOGIES_SUMMARY = "Get legacy technology counts";
    public static final String LEGACY_TECHNOLOGIES_DESCRIPTION = "Returns canonical technology counts for an inclusive "
            + "publication-date range. Prefer /technologies/popularity for explicit denominators and pagination.";
    public static final String TECHNOLOGY_POPULARITY_SUMMARY = "Get technology popularity";
    public static final String TECHNOLOGY_POPULARITY_DESCRIPTION = "Returns count, denominator and share for canonical technologies.";
    public static final String TECHNOLOGY_TREND_SUMMARY = "Get monthly technology trend";
    public static final String TECHNOLOGY_TREND_DESCRIPTION = "Returns monthly count and share, including empty months, "
            + "for one canonicalized technology.";
    public static final String SALARY_ANALYTICS_SUMMARY = "Get comparable salary distribution statistics";
    public static final String DEDUPLICATION_AUDIT_SUMMARY = "Audit cross-source fingerprint groups";
    public static final String TECHNOLOGY_NORMALIZATION_AUDIT_SUMMARY = "Audit technology normalization changes";
    public static final String SALARY_COMPARABILITY_AUDIT_SUMMARY = "Audit salary eligibility and exclusion reasons";
    public static final String DATA_COMPLETENESS_SUMMARY = "Get source data completeness";
    public static final String DATA_COMPLETENESS_DESCRIPTION = "Uses acquisition dates and source-offer rows as the unit of analysis.";
    public static final String SALARY_TREND_SUMMARY = "Get monthly comparable salary trend";
    public static final String OPTIONAL_CATEGORY = "Optional offer category";
    public static final String REQUIRED_CATEGORY = "Required offer category";
    public static final String OPTIONAL_SOURCE_FILTER = "Optional repeated source filter";
    public static final String PUBLICATION_DATE_FROM = "Inclusive first publication date";
    public static final String PUBLICATION_DATE_TO = "Inclusive last publication date";
    public static final String PUBLICATION_DATE_TO_LIMITED = "Inclusive last publication date; range may not exceed 120 months";
    public static final String ACQUISITION_DATE_FROM = "Inclusive first acquisition date";
    public static final String ACQUISITION_DATE_TO = "Inclusive last acquisition date";
    public static final String RESULT_PAGE = "Zero-based result page";
    public static final String RESULT_PAGE_SIZE = "Page size from 1 to 500";
    public static final String DUPLICATE_GROUP_PAGE = "Zero-based duplicate-group page";
    public static final String DUPLICATE_GROUP_PAGE_SIZE = "Group page size from 1 to 100";
    public static final String TECHNOLOGY_NAME = "Technology name or supported alias";
    public static final String EXAMPLE_TECHNOLOGY_JAVA = "Java";
    public static final String OPTIONAL_EXPERIENCE_LEVEL = "Optional experience level";
    public static final String EXAMPLE_EXPERIENCE_SENIOR = "SENIOR";
    public static final String CURRENCY_DESCRIPTION = "Comparable currency; currently PLN only";
    public static final String EXAMPLE_CURRENCY_PLN = "PLN";
    public static final String EMPLOYMENT_TYPE_DESCRIPTION = "Employment type kept as a separate comparison group";
    public static final String EXAMPLE_EMPLOYMENT_B2B = "B2B";
    public static final String SALARY_TYPE_DESCRIPTION = "Gross/net semantics";
    public static final String EXAMPLE_SALARY_TYPE_NET = "NET";

    public static final String DATASET_TAG = "Dataset administration";
    public static final String DATASET_TAG_DESCRIPTION = "Conditionally enabled research-dataset export";
    public static final String DATASET_EXPORT_SUMMARY = "Create an immutable dataset export";
    public static final String DATASET_EXPORT_DESCRIPTION = "Available only when dataset.export.enabled=true. "
            + "The API key is a server-side administrative secret and must never be sent by a browser frontend.";
    public static final String DATASET_CREATED = "Dataset export created";
    public static final String INVALID_DATASET_REQUEST = "Invalid dataset ID or code revision";
    public static final String INVALID_DATASET_KEY = "Missing or invalid export key";
    public static final String DATASET_CONFLICT = "Dataset already exists or export is in progress";
    public static final String DATASET_EXPORT_FAILURE = "Export or server failure";
    public static final String DATASET_ID_DESCRIPTION = "Unique immutable dataset identifier";
    public static final String EXAMPLE_DATASET_ID = "easyoffer-2026-10-final";
    public static final String CODE_REVISION_DESCRIPTION = "Exact Git revision of the running application";
    public static final String EXAMPLE_CODE_REVISION = "f1cbc30ea0c345d80b5731d104001d63551fa5a2";

    private SwaggerDescription() {
    }
}
