package pl.easyoffer.offer_service.repository.projection;

public interface OfferSourceCountProjection {

    String getSource();
    long getOfferCount();
}
