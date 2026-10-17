package pl.easyoffer.offer_service.model.to;

public record DatasetExportFileTO(
        String fileName,
        long recordCount,
        long sizeBytes,
        String sha256
) {
}
