package pl.easyoffer.offer_service.model.to;

import lombok.Getter;
import lombok.Setter;
import pl.easyoffer.offer_service.model.SalaryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OfferResponseTO {

    private Long id;
    private String externalId;
    private String title;
    private String companyName;
    private String location;
    private String description;
    private String experienceLevel;
    private String employmentType;
    private String workMode;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String salaryUnit;
    private String currency;
    private SalaryType salaryType;
    private String source;
    private String url;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> technologies;
    private LocalDateTime publishedAt;

}
