package pl.easyoffer.offer_service.performance;

import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.SalaryUnit;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.TechnologyTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;

final class SyntheticOfferFactory {

    private static final List<String> TECHNOLOGIES = List.of(
            "Java", "Spring", "SQL", "Docker", "Kubernetes", "AWS",
            "JavaScript", "TypeScript", "React", ".NET", "Python", "Git"
    );
    private static final List<String> CATEGORIES = List.of("JAVA", "JAVASCRIPT", "DEVOPS", "DATA");
    private static final List<String> EXPERIENCE_LEVELS = List.of("JUNIOR", "MID", "SENIOR");
    private static final List<String> LOCATIONS = List.of(
            "Warszawa", "Poznań", "Kraków", "Wrocław", "Gdańsk"
    );
    private static final int CROSS_SOURCE_DUPLICATE_INTERVAL = 20;

    private final LocalDate dateFrom;
    private final long publicationDateCount;
    private final int technologiesPerOffer;

    SyntheticOfferFactory(LocalDate dateFrom, LocalDate dateTo, int technologiesPerOffer) {
        this.dateFrom = dateFrom;
        this.publicationDateCount = ChronoUnit.DAYS.between(dateFrom, dateTo) + 1;
        this.technologiesPerOffer = technologiesPerOffer;
    }

    OfferRequestTO create(int offerIndex) {
        int duplicateGroupIndex = offerIndex / 2;
        boolean duplicatePair = offerIndex % CROSS_SOURCE_DUPLICATE_INTERVAL < 2;
        int contentIndex = duplicatePair ? duplicateGroupIndex : offerIndex;
        String source = offerIndex % 2 == 0
                ? JobOfferSourceType.JUST_JOIN_IT.name()
                : JobOfferSourceType.THE_PROTOCOL_IT.name();
        LocalDateTime publishedAt = dateFrom
                .plusDays(offerIndex % publicationDateCount)
                .atTime(12, 0);

        OfferRequestTO request = new OfferRequestTO();
        request.setExternalId("f016-" + offerIndex);
        request.setTitle("Software Engineer " + contentIndex);
        request.setCompanyName("Synthetic Company " + contentIndex % 200);
        request.setLocation(LOCATIONS.get(contentIndex % LOCATIONS.size()));
        request.setExperienceLevel(EXPERIENCE_LEVELS.get(offerIndex % EXPERIENCE_LEVELS.size()));
        request.setEmploymentType("B2B");
        request.setWorkMode(offerIndex % 3 == 0 ? "REMOTE" : "HYBRID");
        request.setWorkingTime("FULL_TIME");
        request.setSalaryMin(BigDecimal.valueOf(10_000L + offerIndex % 5_000));
        request.setSalaryMax(BigDecimal.valueOf(16_000L + offerIndex % 7_000));
        request.setSalaryUnit(SalaryUnit.MONTH.name());
        request.setCurrency("PLN");
        request.setSalaryType(SalaryType.NET);
        request.setSource(source);
        request.setUrl("https://performance.invalid/offers/" + offerIndex);
        request.setCategory(CATEGORIES.get(offerIndex % CATEGORIES.size()));
        request.setTechnologies(technologies(offerIndex));
        request.setLanguage("pl");
        request.setPublishedAt(publishedAt);
        return request;
    }

    private List<TechnologyTO> technologies(int offerIndex) {
        return IntStream.range(0, technologiesPerOffer)
                .mapToObj(offset -> new TechnologyTO(
                        TECHNOLOGIES.get((offerIndex + offset) % TECHNOLOGIES.size()),
                        (offerIndex + offset) % 5 + 1
                ))
                .toList();
    }
}
