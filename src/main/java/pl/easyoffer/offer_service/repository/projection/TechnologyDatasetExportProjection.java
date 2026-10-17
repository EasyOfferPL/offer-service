package pl.easyoffer.offer_service.repository.projection;

public interface TechnologyDatasetExportProjection extends DatasetExportRow {

    Long getId();
    String getName();
    String getCanonicalName();
    String getNormalizationVersion();
    Integer getLevel();

    @Override
    default Object[] values() {
        return new Object[]{
                getId(), getName(), getCanonicalName(), getNormalizationVersion(), getLevel()
        };
    }
}
