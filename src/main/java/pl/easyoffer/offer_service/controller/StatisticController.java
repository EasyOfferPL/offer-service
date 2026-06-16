package pl.easyoffer.offer_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.easyoffer.offer_service.model.to.StatisticTO;
import pl.easyoffer.offer_service.service.StatisticService;

@RestController
@RequestMapping("/v1.0/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping
    public ResponseEntity<StatisticTO> getStatistics() {
        return ResponseEntity.ok(statisticService.getStatistics());
    }

}

