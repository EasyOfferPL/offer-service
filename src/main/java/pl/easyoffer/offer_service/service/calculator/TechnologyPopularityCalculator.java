package pl.easyoffer.offer_service.service.calculator;

import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Component
public class TechnologyPopularityCalculator {

    public TechnologyPopularityCalculation calculate(List<OfferEntity> offers) {
        Set<String> crossSourceFingerprints = findCrossSourceFingerprints(offers);
        Map<String, List<OfferEntity>> analyticalOffers = groupAnalyticalOffers(
                offers, crossSourceFingerprints
        );

        List<Set<String>> eligibleOfferTechnologies = analyticalOffers.values().stream()
                .map(this::canonicalTechnologies)
                .filter(technologies -> !technologies.isEmpty())
                .toList();
        Map<String, Long> offerCountsByTechnology = eligibleOfferTechnologies.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return new TechnologyPopularityCalculation(
                offers.size(),
                analyticalOffers.size(),
                eligibleOfferTechnologies.size(),
                Map.copyOf(offerCountsByTechnology)
        );
    }

    private Set<String> findCrossSourceFingerprints(List<OfferEntity> offers) {
        return offers.stream()
                .filter(offer -> hasText(offer.getDeduplicationFingerprint()))
                .collect(Collectors.groupingBy(
                        OfferEntity::getDeduplicationFingerprint,
                        Collectors.mapping(OfferEntity::getSource, Collectors.toSet())
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private Map<String, List<OfferEntity>> groupAnalyticalOffers(
            List<OfferEntity> offers,
            Set<String> crossSourceFingerprints
    ) {
        Map<String, List<OfferEntity>> analyticalOffers = new LinkedHashMap<>();
        for (int index = 0; index < offers.size(); index++) {
            OfferEntity offer = offers.get(index);
            String fingerprint = offer.getDeduplicationFingerprint();
            String analyticalOfferKey = crossSourceFingerprints.contains(fingerprint)
                    ? "fingerprint:" + fingerprint
                    : "offer:" + index;
            analyticalOffers.computeIfAbsent(analyticalOfferKey, ignored -> new ArrayList<>())
                    .add(offer);
        }
        return analyticalOffers;
    }

    private Set<String> canonicalTechnologies(List<OfferEntity> analyticalOfferRows) {
        return analyticalOfferRows.stream()
                .map(OfferEntity::getTechnologies)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(TechnologyEntity::getCanonicalName)
                .filter(TechnologyPopularityCalculator::hasTechnologyName)
                .collect(Collectors.toSet());
    }

    private static boolean hasTechnologyName(String technologyName) {
        return hasText(technologyName);
    }
}
