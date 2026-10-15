package pl.easyoffer.offer_service.repository.projection;

import pl.easyoffer.offer_service.model.SalaryType;

import java.math.BigDecimal;

public interface SalaryComparabilityInputProjection {

    BigDecimal getSalaryMin();

    BigDecimal getSalaryMax();

    String getCurrency();

    String getSalaryUnit();

    String getEmploymentType();

    SalaryType getSalaryType();
}
