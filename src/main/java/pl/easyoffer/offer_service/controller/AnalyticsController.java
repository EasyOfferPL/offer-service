package pl.easyoffer.offer_service.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.to.*;
import pl.easyoffer.offer_service.service.AnalyticsDateRange;
import pl.easyoffer.offer_service.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1.0/analytics")
@RequiredArgsConstructor
@Validated
public class AnalyticsController {

    private static final String FIRST_GROUP_PAGE = "0";
    private static final String DEFAULT_GROUP_PAGE_SIZE = "50";
    private static final int MAX_GROUP_PAGE_SIZE = 100;
    private static final String FIRST_TECHNOLOGY_PAGE = "0";
    private static final String DEFAULT_TECHNOLOGY_PAGE_SIZE = "50";
    private static final int MAX_TECHNOLOGY_PAGE_SIZE = 500;

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsTO> getShortAnalytics() {
        return ResponseEntity.ok(AnalyticsTO.builder()
                        .categoriesStatistic(analyticsService.getCategoryAnalytics())
                        .newestJobOffers(analyticsService.retrieveNewestOffers())
                        .build());
    }

    @GetMapping("/technologies")
    public ResponseEntity<List<TechnologiesAnalyticsTO>> getTechnologiesAnalytics(
            @RequestParam(required = false) String categoryName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologiesAnalytics(
                categoryName, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/technologies/popularity")
    public ResponseEntity<TechnologyPopularityTO> getTechnologyPopularity(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<String> sources,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(defaultValue = FIRST_TECHNOLOGY_PAGE) @Min(0) int page,
            @RequestParam(defaultValue = DEFAULT_TECHNOLOGY_PAGE_SIZE)
            @Min(1) @Max(MAX_TECHNOLOGY_PAGE_SIZE) int size
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologyPopularity(
                categoryName, sources, AnalyticsDateRange.inclusive(dateFrom, dateTo), page, size
        ));
    }

    @GetMapping("/technologies/trend")
    public ResponseEntity<TechnologyTrendTO> getTechnologyTrend(
            @RequestParam String technologyName,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) List<String> sources,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologyTrend(
                technologyName, categoryName, sources,
                AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/salaries")
    public ResponseEntity<SalaryAnalyticsTO> getSalaryAnalytics(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String experienceLevel
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryAnalytics(categoryName, experienceLevel));
    }

    @GetMapping("/deduplication-audit")
    public ResponseEntity<DeduplicationAuditTO> getDeduplicationAudit(
            @RequestParam(defaultValue = FIRST_GROUP_PAGE) @Min(0) int groupPage,
            @RequestParam(defaultValue = DEFAULT_GROUP_PAGE_SIZE)
            @Min(1) @Max(MAX_GROUP_PAGE_SIZE) int groupSize
    ) {
        return ResponseEntity.ok(analyticsService.getDeduplicationAudit(groupPage, groupSize));
    }

    @GetMapping("/technology-normalization-audit")
    public ResponseEntity<TechnologyNormalizationAuditTO> getTechnologyNormalizationAudit() {
        return ResponseEntity.ok(analyticsService.getTechnologyNormalizationAudit());
    }

    @GetMapping("/salary-comparability-audit")
    public ResponseEntity<SalaryComparabilityAuditTO> getSalaryComparabilityAudit() {
        return ResponseEntity.ok(analyticsService.getSalaryComparabilityAudit());
    }

    @GetMapping("/data-completeness")
    public ResponseEntity<DataCompletenessTO> getDataCompleteness(
            @RequestParam(required = false) List<String> sources,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getDataCompleteness(
                sources, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/salaries/trend")
    public ResponseEntity<SalaryTrendTO> getSalaryTrend(
            @RequestParam String categoryName,
            @RequestParam String currency,
            @RequestParam String employmentType,
            @RequestParam(defaultValue = "GROSS") SalaryType salaryType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryTrend(
                categoryName, currency, employmentType, salaryType,
                experienceLevel, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

}

