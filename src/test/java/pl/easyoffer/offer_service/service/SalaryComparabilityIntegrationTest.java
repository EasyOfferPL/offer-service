package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SalaryComparabilityIntegrationTest {

    private final AtomicInteger offerSequence = new AtomicInteger();

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanOffers() {
        offerRepository.deleteAll();
    }

    @Test
    void shouldPersistSalaryTypeAndExposeExclusiveComparabilityCounts() throws Exception {
        save("10000", "14000", "PLN", "MONTH", "B2B", SalaryType.GROSS);
        save(null, "120", "PLN", "HOUR", "B2B", SalaryType.NET);
        save(null, null, "PLN", "MONTH", "B2B", SalaryType.GROSS);
        save("0", "100", "PLN", "MONTH", "B2B", SalaryType.GROSS);
        save("200", "100", "PLN", "MONTH", "B2B", SalaryType.GROSS);
        save("3000", "4000", "EUR", "MONTH", "B2B", SalaryType.GROSS);
        save("100", "200", "PLN", "WEEK", "B2B", SalaryType.GROSS);
        save("10000", "14000", "PLN", "MONTH", null, SalaryType.GROSS);
        save("10000", "14000", "PLN", "MONTH", "B2B", SalaryType.UNKNOWN);

        mockMvc.perform(get("/v1.0/analytics/salary-comparability-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalizationVersion").value("v1"))
                .andExpect(jsonPath("$.rawOfferCount").value(9))
                .andExpect(jsonPath("$.salaryPresentOfferCount").value(8))
                .andExpect(jsonPath("$.usableOfferCount").value(2))
                .andExpect(jsonPath("$.excludedOfferCount").value(7))
                .andExpect(jsonPath("$.usableSingleBoundaryOfferCount").value(1))
                .andExpect(jsonPath("$.exclusionCounts.MISSING_SALARY").value(1))
                .andExpect(jsonPath("$.exclusionCounts.NON_POSITIVE_SALARY").value(1))
                .andExpect(jsonPath("$.exclusionCounts.INVERTED_RANGE").value(1))
                .andExpect(jsonPath("$.exclusionCounts.UNSUPPORTED_CURRENCY").value(1))
                .andExpect(jsonPath("$.exclusionCounts.UNSUPPORTED_SALARY_UNIT").value(1))
                .andExpect(jsonPath("$.exclusionCounts.MISSING_EMPLOYMENT_TYPE").value(1))
                .andExpect(jsonPath("$.exclusionCounts.UNKNOWN_SALARY_TYPE").value(1));
    }

    private void save(
            String minimum,
            String maximum,
            String currency,
            String salaryUnit,
            String employmentType,
            SalaryType salaryType
    ) {
        int sequence = offerSequence.incrementAndGet();
        OfferEntity offer = new OfferEntity();
        offer.setExternalId("salary-comparability-" + sequence);
        offer.setTitle("Offer " + sequence);
        offer.setCompanyName("Company");
        offer.setSalaryMin(decimal(minimum));
        offer.setSalaryMax(decimal(maximum));
        offer.setCurrency(currency);
        offer.setSalaryUnit(salaryUnit);
        offer.setEmploymentType(employmentType);
        offer.setSalaryType(salaryType);
        offerRepository.saveAndFlush(offer);
    }

    private static BigDecimal decimal(String value) {
        return value == null ? null : new BigDecimal(value);
    }
}
