package pl.easyoffer.offer_service.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OfferSearchRequest {

    private List<Long> ids;
    private List<String> externalIds;
    private List<String> titles;
    private List<String> companyNames;
    private List<String> locations;
    private List<String> experienceLevels;
    private List<String> employmentTypes;
    private List<String> categoryNames;
    private List<String> workModes;
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private List<String> salaryUnits;
    private List<String> currencies;
    private List<String> sources;
    private List<String> urls;
    private List<String> languages;
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
    private LocalDateTime updatedAtFrom;
    private LocalDateTime updatedAtTo;
    private List<String> technologies;

}
