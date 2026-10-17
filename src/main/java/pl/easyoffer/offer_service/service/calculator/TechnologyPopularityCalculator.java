package pl.easyoffer.offer_service.service.calculator;

import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

@Component
public class TechnologyPopularityCalculator {

    public TechnologyPopularityCalculation calculate(List<OfferEntity> offers) {
        List<AnalyticalTechnologyOffer> analyticalOffers = analyticalOffers(offers);
        List<Set<String>> eligibleOfferTechnologies = analyticalOffers.stream()
                .map(AnalyticalTechnologyOffer::canonicalTechnologies)
                .filter(TechnologyPopularityCalculator::hasTechnologies)
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

    public TechnologyTrendCalculation calculateMonthlyTrend(
            List<OfferEntity> offers,
            String canonicalTechnologyName,
            YearMonth monthFrom,
            YearMonth monthTo
    ) {
        List<AnalyticalTechnologyOffer> analyticalOffers = analyticalOffers(offers);
        Map<YearMonth, List<AnalyticalTechnologyOffer>> eligibleOffersByMonth = analyticalOffers.stream()
                .filter(offer -> offer.publishedAt() != null)
                .filter(offer -> hasTechnologies(offer.canonicalTechnologies()))
                .collect(Collectors.groupingBy(
                        offer -> YearMonth.from(offer.publishedAt()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<TechnologyTrendPointCalculation> points = new ArrayList<>();
        for (YearMonth month = monthFrom; !month.isAfter(monthTo); month = month.plusMonths(1)) {
            List<AnalyticalTechnologyOffer> eligibleOffers = eligibleOffersByMonth.getOrDefault(
                    month, List.of()
            );
            long technologyOfferCount = eligibleOffers.stream()
                    .filter(offer -> offer.canonicalTechnologies().contains(canonicalTechnologyName))
                    .count();
            points.add(new TechnologyTrendPointCalculation(
                    month, technologyOfferCount, eligibleOffers.size()
            ));
        }

        return new TechnologyTrendCalculation(
                offers.size(),
                analyticalOffers.size(),
                analyticalOffers.stream()
                        .filter(offer -> hasTechnologies(offer.canonicalTechnologies()))
                        .count(),
                List.copyOf(points)
        );
    }

    private List<AnalyticalTechnologyOffer> analyticalOffers(List<OfferEntity> offers) {
        Set<String> crossSourceFingerprints = findCrossSourceFingerprints(offers);
        return groupAnalyticalOffers(offers, crossSourceFingerprints).values().stream()
                .map(this::toAnalyticalTechnologyOffer)
                .toList();
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

    private AnalyticalTechnologyOffer toAnalyticalTechnologyOffer(
            List<OfferEntity> analyticalOfferRows
    ) {
        LocalDateTime earliestPublicationDate = analyticalOfferRows.stream()
                .map(OfferEntity::getPublishedAt)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);
        return new AnalyticalTechnologyOffer(
                earliestPublicationDate,
                canonicalTechnologies(analyticalOfferRows)
        );
    }

    private static boolean hasTechnologies(Set<String> technologies) {
        return !technologies.isEmpty();
    }

    private static boolean hasTechnologyName(String technologyName) {
        return hasText(technologyName);
    }

    private record AnalyticalTechnologyOffer(
            LocalDateTime publishedAt,
            Set<String> canonicalTechnologies
    ) {
    }
}
