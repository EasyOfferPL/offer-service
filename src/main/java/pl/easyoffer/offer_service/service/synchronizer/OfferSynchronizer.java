package pl.easyoffer.offer_service.service.synchronizer;

public interface OfferSynchronizer {

    void synchronize();

    default String getClassName() {
        return this.getClass().getSimpleName();
    }

}
