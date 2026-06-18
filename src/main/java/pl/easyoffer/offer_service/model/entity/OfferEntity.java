package pl.easyoffer.offer_service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import pl.easyoffer.offer_service.model.JobOfferSourceType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(
        name = "OFFERS",
        indexes = {
                @Index(name = "IDX_JOB_OFFER_EXTERNAL_ID", columnList = "EXTERNAL_ID"),
                @Index(name = "IDX_JOB_OFFER_TITLE_COMPANY", columnList = "TITLE, COMPANY_NAME"),
                @Index(name = "IDX_JOB_OFFER_LOCATION", columnList = "LOCATION")
        }
)
@FieldNameConstants
public class OfferEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EXTERNAL_ID")
    private String externalId;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "COMPANY_NAME", nullable = false, length = 255)
    private String companyName;

    @Column(name = "LOCATION", length = 255)
    private String location;

    @Column(name = "EXPERIENCE_LEVEL", length = 100)
    private String experienceLevel;

    @Column(name = "EMPLOYMENT_TYPE", length = 100)
    private String employmentType;

    @Column(name = "WORK_MODE", length = 50)
    private String workMode;

    @Column(name = "WORKING_TIME", length = 50)
    private String workingTime;

    @Column(name = "SALARY_MIN", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "SALARY_MAX", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "SALARY_UNIT")
    private String salaryUnit;

    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Column(name = "SOURCE", length = 100)
    private String source = JobOfferSourceType.MANUAL.name();

    @Column(name = "URL", length = 1000)
    private String url;

    @Column(name = "LANGUAGE", length = 255)
    private String language;

    @Column(name = "CATEGORY", length = 255)
    private String category;

    @Column(name = "PUBLISHED_AT")
    private LocalDateTime publishedAt;

    @Column(name = "EXPIRED_AT")
    private LocalDateTime expiredAt;

    @Column(name = "OPEN_TO_HIRE_UKRAINIANS")
    private Boolean openToHireUkrainians;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "JOB_OFFER_TECHNOLOGIES",
            joinColumns = @JoinColumn(name = "JOB_OFFER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TECHNOLOGY_ID")
    )
    private Set<TechnologyEntity> technologies = new HashSet<>();

}
