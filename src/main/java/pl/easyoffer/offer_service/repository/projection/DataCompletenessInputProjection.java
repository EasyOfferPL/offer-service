package pl.easyoffer.offer_service.repository.projection;

import pl.easyoffer.offer_service.model.SalaryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface DataCompletenessInputProjection {

    String getSource();

    BigDecimal getSalaryMin();

    BigDecimal getSalaryMax();

    String getCurrency();

    String getSalaryUnit();

    String getEmploymentType();

    SalaryType getSalaryType();

    long getTechnologyCount();

    String getExperienceLevel();

    String getLocation();

    String getWorkMode();

    LocalDateTime getPublishedAt();
}
