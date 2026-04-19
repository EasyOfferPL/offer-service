package pl.easyoffer.offer_service.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobOfferResponseTO {

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
    private String currency;
    private String source;
    private String url;
    private String language;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> technologies;

}
