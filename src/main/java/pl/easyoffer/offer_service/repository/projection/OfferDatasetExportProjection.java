package pl.easyoffer.offer_service.repository.projection;

import pl.easyoffer.offer_service.model.SalaryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OfferDatasetExportProjection extends DatasetExportRow {

    Long getId();
    String getExternalId();
    String getTitle();
    String getCompanyName();
    String getLocation();
    String getExperienceLevel();
    String getEmploymentType();
    String getWorkMode();
    String getWorkingTime();
    BigDecimal getSalaryMin();
    BigDecimal getSalaryMax();
    String getSalaryUnit();
    String getCurrency();
    SalaryType getSalaryType();
    String getSource();
    String getDeduplicationFingerprint();
    String getUrl();
    String getLanguage();
    String getCategory();
    LocalDateTime getPublishedAt();
    LocalDateTime getExpiredAt();
    Boolean getOpenToHireUkrainians();
    Long getVersion();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    @Override
    default Object[] values() {
        return new Object[]{
                getId(), getExternalId(), getTitle(), getCompanyName(), getLocation(),
                getExperienceLevel(), getEmploymentType(), getWorkMode(), getWorkingTime(),
                getSalaryMin(), getSalaryMax(), getSalaryUnit(), getCurrency(), getSalaryType(),
                getSource(), getDeduplicationFingerprint(), getUrl(), getLanguage(), getCategory(),
                getPublishedAt(), getExpiredAt(), getOpenToHireUkrainians(), getVersion(),
                getCreatedAt(), getUpdatedAt()
        };
    }
}
