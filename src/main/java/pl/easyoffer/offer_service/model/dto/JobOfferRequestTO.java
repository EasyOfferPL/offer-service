package pl.easyoffer.offer_service.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class JobOfferRequestTO {

    private String externalId;

    @NotBlank
    private String title;

    @NotBlank
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

    private Boolean active;

    private List<String> technologies;

}
