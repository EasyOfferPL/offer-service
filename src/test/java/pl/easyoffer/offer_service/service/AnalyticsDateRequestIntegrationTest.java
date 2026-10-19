package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalyticsDateRequestIntegrationTest {

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

    @ParameterizedTest
    @ValueSource(strings = {
            "/v1.0/analytics/technologies",
            "/v1.0/analytics/technologies/popularity",
            "/v1.0/analytics/technologies/trend?technologyName=Java",
            "/v1.0/analytics/salaries/trend?categoryName=JAVA&currency=PLN&employmentType=B2B",
            "/v1.0/analytics/data-completeness"
    })
    void shouldAcceptDateOnlyRange(String endpoint) throws Exception {
        mockMvc.perform(get(URI.create(endpoint))
                        .param("dateFrom", "2026-10-01")
                        .param("dateTo", "2026-10-31"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/v1.0/analytics/technologies",
            "/v1.0/analytics/technologies/popularity",
            "/v1.0/analytics/technologies/trend?technologyName=Java",
            "/v1.0/analytics/salaries/trend?categoryName=JAVA&currency=PLN&employmentType=B2B",
            "/v1.0/analytics/data-completeness"
    })
    void shouldRejectTimestampRequestFormat(String endpoint) throws Exception {
        mockMvc.perform(get(URI.create(endpoint))
                        .param("dateFrom", "2026-10-01T00:00:00")
                        .param("dateTo", "2026-10-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Request parameter 'dateFrom' must use format YYYY-MM-DD"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/v1.0/analytics/technologies",
            "/v1.0/analytics/technologies/popularity",
            "/v1.0/analytics/technologies/trend?technologyName=Java",
            "/v1.0/analytics/salaries/trend?categoryName=JAVA&currency=PLN&employmentType=B2B",
            "/v1.0/analytics/data-completeness"
    })
    void shouldRejectInvertedCalendarDateRange(String endpoint) throws Exception {
        mockMvc.perform(get(URI.create(endpoint))
                        .param("dateFrom", "2026-11-01")
                        .param("dateTo", "2026-10-31"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dateFrom must not be after dateTo"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-10-31T23:59:59.999999", "2026-11-01T00:00:00"})
    void shouldUseExclusiveNextDayBoundary(String publishedAt) throws Exception {
        TechnologyEntity java = technologyRepository.saveAndFlush(technology("Java"));
        offerRepository.saveAndFlush(offer(publishedAt, java));

        int expectedCount = publishedAt.startsWith("2026-10-31") ? 1 : 0;
        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("dateFrom", "2026-10-31")
                        .param("dateTo", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawOfferCount").value(expectedCount));
    }

    private static OfferEntity offer(String publishedAt, TechnologyEntity technology) {
        OfferEntity offer = new OfferEntity();
        offer.setExternalId("offer-" + publishedAt);
        offer.setSource(JobOfferSourceType.JUST_JOIN_IT.name());
        offer.setTitle("Java Developer");
        offer.setCompanyName("Acme");
        offer.setLocation("Warsaw");
        offer.setCategory("BACKEND");
        offer.setPublishedAt(LocalDateTime.parse(publishedAt));
        offer.setTechnologies(Set.of(technology));
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
