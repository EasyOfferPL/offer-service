package pl.easyoffer.offer_service.repository.projection;

public interface DeduplicationAuditOfferProjection {

    Long getOfferId();

    String getSource();

    String getExternalId();

    String getDeduplicationFingerprint();

}
