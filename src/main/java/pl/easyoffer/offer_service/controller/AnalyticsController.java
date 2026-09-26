package pl.easyoffer.offer_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.model.to.*;
import pl.easyoffer.offer_service.service.AnalyticsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1.0/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

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
            @RequestParam LocalDateTime dateFrom,
            @RequestParam LocalDateTime dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologiesAnalytics(categoryName, dateFrom, dateTo));
    }

    @GetMapping("/technologies/trend")
    public ResponseEntity<List<TechnologyTrendTO>> getTechnologyTrend(
            @RequestParam String categoryName,
            @RequestParam LocalDateTime dateFrom,
            @RequestParam LocalDateTime dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getTechnologyTrend(categoryName, dateFrom, dateTo));
    }

    @GetMapping("/salaries")
    public ResponseEntity<SalaryAnalyticsTO> getSalaryAnalytics(
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String experienceLevel
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryAnalytics(categoryName, experienceLevel));
    }

    @GetMapping("/salaries/trend")
    public ResponseEntity<SalaryTrendTO> getSalaryTrend(
            @RequestParam String categoryName,
            @RequestParam String currency,
            @RequestParam String employmentType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam LocalDateTime dateFrom,
            @RequestParam LocalDateTime dateTo
    ) {
        return ResponseEntity.ok(analyticsService.getSalaryTrend(
                categoryName, currency, employmentType, experienceLevel, dateFrom, dateTo
        ));
    }

}

