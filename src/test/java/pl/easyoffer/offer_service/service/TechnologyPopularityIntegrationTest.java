package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TechnologyPopularityIntegrationTest {

    private static final String SHARED_FINGERPRINT = fingerprint('a');
    private static final LocalDateTime PERIOD_START = LocalDateTime.of(2026, 10, 1, 0, 0);
    private static final LocalDateTime PERIOD_END = LocalDateTime.of(2026, 10, 31, 23, 59, 59);

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
    void shouldReturnDeduplicatedTechnologyCountsSharesAndScope() throws Exception {
        saveOffer("jji-1", JobOfferSourceType.JUST_JOIN_IT, SHARED_FINGERPRINT,
                PERIOD_START.plusDays(1), "Java", "React");
        saveOffer("protocol-1", JobOfferSourceType.THE_PROTOCOL_IT, SHARED_FINGERPRINT,
                PERIOD_START.plusDays(2), "Java", "Python");
        saveOffer("jji-2", JobOfferSourceType.JUST_JOIN_IT, null,
                PERIOD_START.plusDays(3), "Java");
        saveOffer("protocol-2", JobOfferSourceType.THE_PROTOCOL_IT, null,
                PERIOD_START.plusDays(4));
        saveOffer("outside-period", JobOfferSourceType.JUST_JOIN_IT, null,
                PERIOD_START.minusDays(1), "Java");

        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("dateFrom", PERIOD_START.toString())
                        .param("dateTo", PERIOD_END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technologyNormalizationVersion").value("v1"))
                .andExpect(jsonPath("$.deduplicationVersion").value("v1"))
                .andExpect(jsonPath("$.periodField").value("PUBLISHED_AT"))
                .andExpect(jsonPath("$.categoryName").doesNotExist())
                .andExpect(jsonPath("$.sources[0]").value("JUST_JOIN_IT"))
                .andExpect(jsonPath("$.sources[1]").value("THE_PROTOCOL_IT"))
                .andExpect(jsonPath("$.dateFrom").value("2026-10-01T00:00:00"))
                .andExpect(jsonPath("$.dateTo").value(PERIOD_END.toString()))
                .andExpect(jsonPath("$.rawOfferCount").value(4))
                .andExpect(jsonPath("$.uniqueOfferCount").value(3))
                .andExpect(jsonPath("$.eligibleOfferCount").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(50))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.technologies[0].technologyName").value("Java"))
                .andExpect(jsonPath("$.technologies[0].count").value(2))
                .andExpect(jsonPath("$.technologies[0].denominator").value(2))
                .andExpect(jsonPath("$.technologies[0].sharePercentage").value(100.0000))
                .andExpect(jsonPath("$.technologies[1].technologyName").value("Python"))
                .andExpect(jsonPath("$.technologies[1].count").value(1))
                .andExpect(jsonPath("$.technologies[1].sharePercentage").value(50.0000))
                .andExpect(jsonPath("$.technologies[2].technologyName").value("React"))
                .andExpect(jsonPath("$.technologies[2].count").value(1));
    }

    @Test
    void shouldApplyOptionalCategoryAndSourceFilters() throws Exception {
        saveOffer("jji-java", JobOfferSourceType.JUST_JOIN_IT, null,
                PERIOD_START.plusDays(1), "Java");
        saveOffer("protocol-java", JobOfferSourceType.THE_PROTOCOL_IT, null,
                PERIOD_START.plusDays(2), "Java");

        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("categoryName", "BACKEND")
                        .param("sources", "just_join_it")
                        .param("dateFrom", PERIOD_START.toString())
                        .param("dateTo", PERIOD_END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("BACKEND"))
                .andExpect(jsonPath("$.sources[0]").value("JUST_JOIN_IT"))
                .andExpect(jsonPath("$.rawOfferCount").value(1))
                .andExpect(jsonPath("$.uniqueOfferCount").value(1));
    }

    @Test
    void shouldReturnRequestedTechnologyPageWithoutChangingGlobalCounts() throws Exception {
        saveOffer("jji-1", JobOfferSourceType.JUST_JOIN_IT, null,
                PERIOD_START.plusDays(1), "Java", "Python", "React");

        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("dateFrom", PERIOD_START.toString())
                        .param("dateTo", PERIOD_END.toString())
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligibleOfferCount").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.technologies.length()").value(1))
                .andExpect(jsonPath("$.technologies[0].technologyName").value("React"))
                .andExpect(jsonPath("$.technologies[0].denominator").value(1));
    }

    @Test
    void shouldKeepLegacyTechnologyEndpointWorkingWithoutCategoryFilter() throws Exception {
        saveOffer("jji-java", JobOfferSourceType.JUST_JOIN_IT, null,
                PERIOD_START.plusDays(1), "Java");

        mockMvc.perform(get("/v1.0/analytics/technologies")
                        .param("dateFrom", PERIOD_START.toString())
                        .param("dateTo", PERIOD_END.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].technologyName").value("Java"))
                .andExpect(jsonPath("$[0].count").value(1));
    }

    @Test
    void shouldRejectInvertedDateRange() throws Exception {
        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("dateFrom", PERIOD_END.toString())
                        .param("dateTo", PERIOD_START.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectTechnologyPageLargerThanLimit() throws Exception {
        mockMvc.perform(get("/v1.0/analytics/technologies/popularity")
                        .param("dateFrom", PERIOD_START.toString())
                        .param("dateTo", PERIOD_END.toString())
                        .param("size", "501"))
                .andExpect(status().isBadRequest());
    }

    private void saveOffer(
            String externalId,
            JobOfferSourceType source,
            String fingerprint,
            LocalDateTime publishedAt,
            String... technologyNames
    ) {
        OfferEntity offer = new OfferEntity();
        offer.setExternalId(externalId);
        offer.setSource(source.name());
        offer.setTitle("Java Developer");
        offer.setCompanyName("Acme");
        offer.setLocation("Warsaw");
        offer.setCategory("BACKEND");
        offer.setPublishedAt(publishedAt);
        offer.setDeduplicationFingerprint(fingerprint);
        offer.setTechnologies(technologies(technologyNames));
        offerRepository.saveAndFlush(offer);
    }

    private Set<TechnologyEntity> technologies(String... names) {
        return Arrays.stream(names)
                .map(this::technology)
                .map(technologyRepository::saveAndFlush)
                .collect(Collectors.toSet());
    }

    private TechnologyEntity technology(String name) {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName(name);
        technology.setCanonicalName(name);
        technology.setNormalizationVersion("v1");
        return technology;
    }

    private static String fingerprint(char hashCharacter) {
        return "v1:" + String.valueOf(hashCharacter).repeat(64);
    }
}
