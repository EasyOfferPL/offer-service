package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DataCompletenessIntegrationTest {

    private static final LocalDate DATE_FROM = LocalDate.of(2000, 1, 1);
    private static final LocalDate DATE_TO = LocalDate.of(2100, 1, 1);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @AfterEach
    void cleanDatabase() {
        offerRepository.deleteAll();
        technologyRepository.deleteAll();
    }

    @Test
    void shouldExposeCompletenessBySourceForAcquisitionPeriod() throws Exception {
        TechnologyEntity java = technologyRepository.saveAndFlush(technology("Java"));
        saveCompleteOffer(JobOfferSourceType.JUST_JOIN_IT, java);
        saveIncompleteOffer(JobOfferSourceType.JUST_JOIN_IT);
        saveCompleteOffer(JobOfferSourceType.THE_PROTOCOL_IT, java);

        mockMvc.perform(get("/v1.0/analytics/data-completeness")
                        .param("dateFrom", DATE_FROM.toString())
                        .param("dateTo", DATE_TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completenessVersion").value("v1"))
                .andExpect(jsonPath("$.salaryNormalizationVersion").value("v1"))
                .andExpect(jsonPath("$.periodField").value("CREATED_AT"))
                .andExpect(jsonPath("$.unitOfAnalysis").value("SOURCE_OFFER_ROW"))
                .andExpect(jsonPath("$.dateFrom").value("2000-01-01"))
                .andExpect(jsonPath("$.dateTo").value("2100-01-01"))
                .andExpect(jsonPath("$.sources[0]").value("JUST_JOIN_IT"))
                .andExpect(jsonPath("$.sources[1]").value("THE_PROTOCOL_IT"))
                .andExpect(jsonPath("$.eligibleOfferCount").value(3))
                .andExpect(jsonPath("$.bySource[0].source").value("JUST_JOIN_IT"))
                .andExpect(jsonPath("$.bySource[0].eligibleOfferCount").value(2))
                .andExpect(jsonPath("$.bySource[0].fields[0].field").value("SALARY"))
                .andExpect(jsonPath("$.bySource[0].fields[0].usableOfferCount").value(1))
                .andExpect(jsonPath("$.bySource[0].fields[0].unusableOfferCount").value(1))
                .andExpect(jsonPath("$.bySource[0].fields[0].eligibleOfferCount").value(2))
                .andExpect(jsonPath("$.bySource[0].fields[0].completenessPercentage").value(50.0000))
                .andExpect(jsonPath("$.bySource[0].fields[1].field").value("TECHNOLOGIES"))
                .andExpect(jsonPath("$.bySource[0].fields[1].usableOfferCount").value(1))
                .andExpect(jsonPath("$.bySource[0].fields[4].field").value("WORK_MODE"))
                .andExpect(jsonPath("$.bySource[0].fields[4].usableOfferCount").value(2))
                .andExpect(jsonPath("$.bySource[0].fields[5].field").value("PUBLISHED_AT"))
                .andExpect(jsonPath("$.bySource[0].fields[5].usableOfferCount").value(1))
                .andExpect(jsonPath("$.bySource[1].source").value("THE_PROTOCOL_IT"))
                .andExpect(jsonPath("$.bySource[1].eligibleOfferCount").value(1))
                .andExpect(jsonPath("$.bySource[1].fields[0].completenessPercentage").value(100.0000));
    }

    @Test
    void shouldApplySourceFilterAndRejectInvertedDateRange() throws Exception {
        TechnologyEntity java = technologyRepository.saveAndFlush(technology("Java"));
        saveCompleteOffer(JobOfferSourceType.JUST_JOIN_IT, java);
        saveCompleteOffer(JobOfferSourceType.THE_PROTOCOL_IT, java);

        mockMvc.perform(get("/v1.0/analytics/data-completeness")
                        .param("sources", "just_join_it")
                        .param("dateFrom", DATE_FROM.toString())
                        .param("dateTo", DATE_TO.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sources.length()").value(1))
                .andExpect(jsonPath("$.sources[0]").value("JUST_JOIN_IT"))
                .andExpect(jsonPath("$.eligibleOfferCount").value(1))
                .andExpect(jsonPath("$.bySource.length()").value(1))
                .andExpect(jsonPath("$.bySource[0].source").value("JUST_JOIN_IT"));

        mockMvc.perform(get("/v1.0/analytics/data-completeness")
                        .param("dateFrom", DATE_TO.toString())
                        .param("dateTo", DATE_FROM.toString()))
                .andExpect(status().isBadRequest());
    }

    private void saveCompleteOffer(JobOfferSourceType source, TechnologyEntity technology) {
        OfferEntity offer = offer(source, "complete");
        offer.setSalaryMin(new BigDecimal("10000"));
        offer.setSalaryMax(new BigDecimal("14000"));
        offer.setCurrency("PLN");
        offer.setSalaryUnit("MONTH");
        offer.setEmploymentType("B2B");
        offer.setSalaryType(SalaryType.GROSS);
        offer.setExperienceLevel("SENIOR");
        offer.setLocation("WARSAW");
        offer.setWorkMode("REMOTE");
        offer.setPublishedAt(LocalDateTime.of(2026, 10, 19, 12, 0));
        offer.setTechnologies(Set.of(technology));
        offerRepository.saveAndFlush(offer);
    }

    private void saveIncompleteOffer(JobOfferSourceType source) {
        OfferEntity offer = offer(source, "incomplete");
        offer.setSalaryMin(new BigDecimal("3000"));
        offer.setSalaryMax(new BigDecimal("4000"));
        offer.setCurrency("EUR");
        offer.setSalaryUnit("MONTH");
        offer.setEmploymentType("B2B");
        offer.setSalaryType(SalaryType.GROSS);
        offer.setExperienceLevel(" ");
        offer.setWorkMode("HYBRID");
        offerRepository.saveAndFlush(offer);
    }

    private static OfferEntity offer(JobOfferSourceType source, String suffix) {
        OfferEntity offer = new OfferEntity();
        offer.setExternalId(source.name() + "-" + suffix);
        offer.setSource(source.name());
        offer.setTitle("Backend Developer");
        offer.setCompanyName("Acme");
        return offer;
    }

    private static TechnologyEntity technology(String name) {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName(name);
        technology.setCanonicalName(name);
        technology.setNormalizationVersion("v1");
        return technology;
    }
}
