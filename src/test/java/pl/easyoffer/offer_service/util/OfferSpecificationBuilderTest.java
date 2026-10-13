package pl.easyoffer.offer_service.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OfferSpecificationBuilderTest {

    private static final String MATCHING_TITLE = "Java Backend Developer";

    @Autowired
    private OfferRepository offerRepository;

    @BeforeEach
    void persistOffers() {
        offerRepository.saveAll(List.of(
                offer(MATCHING_TITLE, "Acme", "Java", "12000", "18000"),
                offer("Kotlin Backend Developer", "Acme", "Kotlin", "12000", "18000"),
                offer("Senior Java Developer", "Acme", "Java", "12000", "22000"),
                offer("Java Developer", "Beta", "Java", "12000", "18000"),
                offer("Python Developer", "Gamma", "Python", "12000", "18000")
        ));
        offerRepository.flush();
    }

    @Test
    void shouldApplySingleCriterionWithMultipleAcceptedValues() {
        var specification = OfferSpecificationBuilder.builder()
                .withCompanyNames(List.of("Acme", "Beta"))
                .build();

        assertThat(offerRepository.findAll(specification))
                .extracting(OfferEntity::getTitle)
                .containsExactlyInAnyOrder(
                        MATCHING_TITLE,
                        "Kotlin Backend Developer",
                        "Senior Java Developer",
                        "Java Developer"
                );
    }

    @Test
    void shouldCombineAllProvidedCriteriaUsingAnd() {
        var specification = OfferSpecificationBuilder.builder()
                .withCompanyNames(List.of("Acme"))
                .withCategoryNames(List.of("Java"))
                .withSalaryBetween(new BigDecimal("10000"), new BigDecimal("20000"))
                .build();

        assertThat(offerRepository.findAll(specification))
                .extracting(OfferEntity::getTitle)
                .containsExactly(MATCHING_TITLE);
    }

    @Test
    void shouldIgnoreEmptyCriteriaWithoutBreakingSubsequentFilters() {
        var specification = OfferSpecificationBuilder.builder()
                .withTitles(null)
                .withCompanyNames(List.of())
                .withCategoryNames(List.of("Kotlin"))
                .build();

        assertThat(offerRepository.findAll(specification))
                .extracting(OfferEntity::getTitle)
                .containsExactly("Kotlin Backend Developer");
    }

    @Test
    void shouldBuildNoSpecificationWhenAllCriteriaAreAbsent() {
        var specification = OfferSpecificationBuilder.builder()
                .withTitles(null)
                .withCompanyNames(List.of())
                .withSalaryBetween(null, null)
                .build();

        assertThat(specification).isNull();
    }

    private static OfferEntity offer(
            String title,
            String companyName,
            String category,
            String salaryMin,
            String salaryMax
    ) {
        OfferEntity offer = new OfferEntity();
        offer.setTitle(title);
        offer.setCompanyName(companyName);
        offer.setCategory(category);
        offer.setSalaryMin(new BigDecimal(salaryMin));
        offer.setSalaryMax(new BigDecimal(salaryMax));
        return offer;
    }
}
