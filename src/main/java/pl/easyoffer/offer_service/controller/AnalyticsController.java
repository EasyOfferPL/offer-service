package pl.easyoffer.offer_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import pl.easyoffer.offer_service.exception.ErrorResponse;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.to.*;
import pl.easyoffer.offer_service.service.AnalyticsDateRange;
import pl.easyoffer.offer_service.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

import static pl.easyoffer.offer_service.config.SwaggerDescription.*;

@RestController
@RequestMapping("/v1.0/analytics")
@RequiredArgsConstructor
@Validated
@Tag(name = ANALYTICS_TAG, description = ANALYTICS_TAG_DESCRIPTION)
@ApiResponses({
        @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = INVALID_ANALYTICS_PARAMETER,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public class AnalyticsController {

    private static final String FIRST_GROUP_PAGE = "0";
    private static final String DEFAULT_GROUP_PAGE_SIZE = "50";
    private static final int MAX_GROUP_PAGE_SIZE = 100;
    private static final String FIRST_TECHNOLOGY_PAGE = "0";
    private static final String DEFAULT_TECHNOLOGY_PAGE_SIZE = "50";
    private static final int MAX_TECHNOLOGY_PAGE_SIZE = 500;

    private final AnalyticsService analyticsService;

    @GetMapping
    @Operation(summary = DASHBOARD_SUMMARY, description = DASHBOARD_DESCRIPTION)
    public ResponseEntity<AnalyticsTO> getShortAnalytics() {
        return ResponseEntity.ok(AnalyticsTO.builder()
                        .categoriesStatistic(analyticsService.getCategoryAnalytics())
                        .newestJobOffers(analyticsService.retrieveNewestOffers())
                        .build());
    }

    @GetMapping("/technologies")
    @Operation(
            summary = LEGACY_TECHNOLOGIES_SUMMARY,
            description = LEGACY_TECHNOLOGIES_DESCRIPTION
    )
    public ResponseEntity<List<TechnologiesAnalyticsTO>> getTechnologiesAnalytics(
            @Parameter(description = OPTIONAL_CATEGORY, example = EXAMPLE_CATEGORY_JAVA)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = PUBLICATION_DATE_FROM, example = EXAMPLE_DATE_FROM)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = PUBLICATION_DATE_TO, example = EXAMPLE_DATE_TO)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologiesAnalytics(
                categoryName, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/technologies/popularity")
    @Operation(summary = TECHNOLOGY_POPULARITY_SUMMARY, description = TECHNOLOGY_POPULARITY_DESCRIPTION)
    public ResponseEntity<TechnologyPopularityTO> getTechnologyPopularity(
            @Parameter(description = OPTIONAL_CATEGORY, example = EXAMPLE_CATEGORY_JAVA)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = OPTIONAL_SOURCE_FILTER, example = EXAMPLE_JUST_JOIN_IT)
            @RequestParam(required = false) List<String> sources,
            @Parameter(description = PUBLICATION_DATE_FROM, example = EXAMPLE_DATE_FROM)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = PUBLICATION_DATE_TO, example = EXAMPLE_DATE_TO)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @Parameter(description = RESULT_PAGE, example = EXAMPLE_FIRST_PAGE)
            @RequestParam(defaultValue = FIRST_TECHNOLOGY_PAGE) @Min(0) int page,
            @Parameter(description = RESULT_PAGE_SIZE, example = EXAMPLE_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_TECHNOLOGY_PAGE_SIZE)
            @Min(1) @Max(MAX_TECHNOLOGY_PAGE_SIZE) int size
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologyPopularity(
                categoryName, sources, AnalyticsDateRange.inclusive(dateFrom, dateTo), page, size
        ));
    }

    @GetMapping("/technologies/trend")
    @Operation(summary = TECHNOLOGY_TREND_SUMMARY, description = TECHNOLOGY_TREND_DESCRIPTION)
    public ResponseEntity<TechnologyTrendTO> getTechnologyTrend(
            @Parameter(description = TECHNOLOGY_NAME, example = EXAMPLE_TECHNOLOGY_JAVA)
            @RequestParam String technologyName,
            @Parameter(description = OPTIONAL_CATEGORY, example = EXAMPLE_CATEGORY_JAVA)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = OPTIONAL_SOURCE_FILTER, example = EXAMPLE_THE_PROTOCOL_IT)
            @RequestParam(required = false) List<String> sources,
            @Parameter(description = PUBLICATION_DATE_FROM, example = EXAMPLE_DATE_FROM_TREND)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = PUBLICATION_DATE_TO_LIMITED, example = EXAMPLE_DATE_TO)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologyTrend(
                technologyName, categoryName, sources,
                AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/salaries")
    @Operation(summary = SALARY_ANALYTICS_SUMMARY)
    public ResponseEntity<SalaryAnalyticsTO> getSalaryAnalytics(
            @Parameter(description = OPTIONAL_CATEGORY, example = EXAMPLE_CATEGORY_JAVA)
            @RequestParam(required = false) String categoryName,
            @Parameter(description = OPTIONAL_EXPERIENCE_LEVEL, example = EXAMPLE_EXPERIENCE_SENIOR)
            @RequestParam(required = false) String experienceLevel
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryAnalytics(categoryName, experienceLevel));
    }

    @GetMapping("/deduplication-audit")
    @Operation(summary = DEDUPLICATION_AUDIT_SUMMARY)
    public ResponseEntity<DeduplicationAuditTO> getDeduplicationAudit(
            @Parameter(description = DUPLICATE_GROUP_PAGE, example = EXAMPLE_FIRST_PAGE)
            @RequestParam(defaultValue = FIRST_GROUP_PAGE) @Min(0) int groupPage,
            @Parameter(description = DUPLICATE_GROUP_PAGE_SIZE, example = EXAMPLE_PAGE_SIZE)
            @RequestParam(defaultValue = DEFAULT_GROUP_PAGE_SIZE)
            @Min(1) @Max(MAX_GROUP_PAGE_SIZE) int groupSize
    ) {
        return ResponseEntity.ok(analyticsService.getDeduplicationAudit(groupPage, groupSize));
    }

    @GetMapping("/technology-normalization-audit")
    @Operation(summary = TECHNOLOGY_NORMALIZATION_AUDIT_SUMMARY)
    public ResponseEntity<TechnologyNormalizationAuditTO> getTechnologyNormalizationAudit() {
        return ResponseEntity.ok(analyticsService.getTechnologyNormalizationAudit());
    }

    @GetMapping("/salary-comparability-audit")
    @Operation(summary = SALARY_COMPARABILITY_AUDIT_SUMMARY)
    public ResponseEntity<SalaryComparabilityAuditTO> getSalaryComparabilityAudit() {
        return ResponseEntity.ok(analyticsService.getSalaryComparabilityAudit());
    }

    @GetMapping("/data-completeness")
    @Operation(summary = DATA_COMPLETENESS_SUMMARY, description = DATA_COMPLETENESS_DESCRIPTION)
    public ResponseEntity<DataCompletenessTO> getDataCompleteness(
            @Parameter(description = OPTIONAL_SOURCE_FILTER, example = EXAMPLE_JUST_JOIN_IT)
            @RequestParam(required = false) List<String> sources,
            @Parameter(description = ACQUISITION_DATE_FROM, example = EXAMPLE_DATE_FROM)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = ACQUISITION_DATE_TO, example = EXAMPLE_DATE_TO)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getDataCompleteness(
                sources, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

    @GetMapping("/salaries/trend")
    @Operation(summary = SALARY_TREND_SUMMARY)
    public ResponseEntity<SalaryTrendTO> getSalaryTrend(
            @Parameter(description = REQUIRED_CATEGORY, example = EXAMPLE_CATEGORY_JAVA)
            @RequestParam String categoryName,
            @Parameter(description = CURRENCY_DESCRIPTION, example = EXAMPLE_CURRENCY_PLN)
            @RequestParam String currency,
            @Parameter(description = EMPLOYMENT_TYPE_DESCRIPTION, example = EXAMPLE_EMPLOYMENT_B2B)
            @RequestParam String employmentType,
            @Parameter(description = SALARY_TYPE_DESCRIPTION, example = EXAMPLE_SALARY_TYPE_NET)
            @RequestParam(defaultValue = "GROSS") SalaryType salaryType,
            @Parameter(description = OPTIONAL_EXPERIENCE_LEVEL, example = EXAMPLE_EXPERIENCE_SENIOR)
            @RequestParam(required = false) String experienceLevel,
            @Parameter(description = PUBLICATION_DATE_FROM, example = EXAMPLE_DATE_FROM)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @Parameter(description = PUBLICATION_DATE_TO, example = EXAMPLE_DATE_TO)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryTrend(
                categoryName, currency, employmentType, salaryType,
                experienceLevel, AnalyticsDateRange.inclusive(dateFrom, dateTo)
        ));
    }

}

