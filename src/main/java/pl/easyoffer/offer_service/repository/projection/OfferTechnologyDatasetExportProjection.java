package pl.easyoffer.offer_service.repository.projection;

public interface OfferTechnologyDatasetExportProjection extends DatasetExportRow {

    Long getOfferId();
    Long getTechnologyId();

    @Override
    default Object[] values() {
        return new Object[]{getOfferId(), getTechnologyId()};
    }
}
