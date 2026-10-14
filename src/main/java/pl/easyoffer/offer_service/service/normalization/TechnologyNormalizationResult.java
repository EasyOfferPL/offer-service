package pl.easyoffer.offer_service.service.normalization;

public record TechnologyNormalizationResult(
        String sourceName,
        String canonicalName,
        String version
) {

    public boolean changed() {
        return !sourceName.equals(canonicalName);
    }
}
