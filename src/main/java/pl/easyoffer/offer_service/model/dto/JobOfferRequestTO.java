package pl.easyoffer.offer_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class JobOfferRequestTO {

    private String externalId;

    @NotBlank
    private String title;

    @NotBlank
    private String companyName;

    @NotBlank
    private String location;

    @NotBlank
    private String description;

    @NotBlank
    private String experienceLevel;

    @NotBlank
    private String employmentType;

    @NotBlank
    private String workMode;

    private String workingTime;

    private BigDecimal salaryMin;

    private BigDecimal salaryMax;

    private String salaryUnit;

    private String currency;

    private String source;

    private String url;

    private String category;

    private List<TechnologyTO> technologies;

    private String language;

    private LocalDateTime publishedAt;

    private LocalDateTime lastPublishedAt;

    private LocalDateTime expiredAt;

    private boolean openToHireUkrainians;

}
