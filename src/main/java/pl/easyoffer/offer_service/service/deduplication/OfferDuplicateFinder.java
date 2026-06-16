package pl.easyoffer.offer_service.service.deduplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferDuplicateFinder {

    private static final String FOUND_LOG_MESSAGE = "Duplicate found {title={}, company={}, category={}, strategy={}}";
    private static final String NOT_FOUND_LOG_MESSAGE = "New offer will be created {title={}, company={}, category={}}";

    private final List<DuplicateSearchStrategy> strategies;

    public Optional<OfferEntity> find(OfferRequestTO request) {
        List<DuplicateSearchStrategy> sortedStrategies = strategies.stream()
                .sorted(Comparator.comparingInt(DuplicateSearchStrategy::priority))
                .toList();
        for (DuplicateSearchStrategy strategy : sortedStrategies) {
            Optional<OfferEntity> result = strategy.find(request);
            if (result.isPresent()) {
                log.info(FOUND_LOG_MESSAGE, request.getTitle(), request.getCompanyName(), request.getCategory(), strategy.getClassName());
                return result;
            }
        }
        log.info(NOT_FOUND_LOG_MESSAGE, request.getTitle(), request.getCompanyName(), request.getCategory());
        return Optional.empty();
    }

}
