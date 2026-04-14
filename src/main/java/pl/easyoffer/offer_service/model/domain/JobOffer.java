package pl.easyoffer.offer_service.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import pl.easyoffer.offer_service.model.AbstractAuditingEntity;

@Getter
@Setter
@Entity
@Table(
        name = "JOB_OFFERS",
        indexes = {
                @Index(name = "IDX_JOB_OFFER_EXTERNAL_ID", columnList = "EXTERNAL_ID"),
                @Index(name = "IDX_JOB_OFFER_TITLE_COMPANY", columnList = "TITLE, COMPANY_NAME"),
                @Index(name = "IDX_JOB_OFFER_LOCATION", columnList = "LOCATION")
        }
)
public class JobOffer extends AbstractAuditingEntity<Long> {

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

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "EXPERIENCE_LEVEL", length = 100)
    private String experienceLevel;

    @Column(name = "EMPLOYMENT_TYPE", length = 100)
    private String employmentType;

    @Column(name = "WORK_MODE", length = 50)
    private String workMode;

    @Column(name = "SALARY_MIN", precision = 19, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "SALARY_MAX", precision = 19, scale = 2)
    private BigDecimal salaryMax;

    @Column(name = "CURRENCY", length = 10)
    private String currency;

    @Column(name = "SOURCE", length = 100)
    private String source;

    @Column(name = "URL", length = 1000)
    private String url;

    @Column(name = "ACTIVE", nullable = false)
    private boolean active = true;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "JOB_OFFER_TECHNOLOGIES",
            joinColumns = @JoinColumn(name = "JOB_OFFER_ID"),
            inverseJoinColumns = @JoinColumn(name = "TECHNOLOGY_ID")
    )
    private Set<Technology> technologies = new HashSet<>();

}
