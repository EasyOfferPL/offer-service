package pl.easyoffer.offer_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.model.to.AnalyticsTO;
import pl.easyoffer.offer_service.model.to.TechnologiesAnalyticsTO;
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

}

