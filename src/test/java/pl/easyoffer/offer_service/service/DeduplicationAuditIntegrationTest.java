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
import pl.easyoffer.offer_service.repository.OfferRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeduplicationAuditIntegrationTest {

    private static final String SHARED_FINGERPRINT = fingerprint('a');
    private static final String OTHER_FINGERPRINT = fingerprint('b');

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfferRepository offerRepository;

    @AfterEach
    void cleanOffers() {
        offerRepository.deleteAll();
    }

    @Test
    void shouldGroupEqualFingerprintFromDifferentSourcesAsOneAnalyticalOffer() throws Exception {
        OfferEntity justJoinItOffer = savedOffer(
                JobOfferSourceType.JUST_JOIN_IT.name(), "just-join-it-id", SHARED_FINGERPRINT
        );
        OfferEntity theProtocolOffer = savedOffer(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "the-protocol-id", SHARED_FINGERPRINT
        );

        mockMvc.perform(get("/v1.0/analytics/deduplication-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawOfferCount").value(2))
                .andExpect(jsonPath("$.uniqueOfferCount").value(1))
                .andExpect(jsonPath("$.crossSourceDuplicateGroupCount").value(1))
                .andExpect(jsonPath("$.groupPage").value(0))
                .andExpect(jsonPath("$.groupSize").value(50))
                .andExpect(jsonPath("$.totalGroupPages").value(1))
                .andExpect(jsonPath("$.groups[0].deduplicationFingerprint").value(SHARED_FINGERPRINT))
                .andExpect(jsonPath("$.groups[0].offers[0].offerId").value(justJoinItOffer.getId()))
                .andExpect(jsonPath("$.groups[0].offers[0].source").value(JobOfferSourceType.JUST_JOIN_IT.name()))
                .andExpect(jsonPath("$.groups[0].offers[0].externalId").value("just-join-it-id"))
                .andExpect(jsonPath("$.groups[0].offers[1].offerId").value(theProtocolOffer.getId()))
                .andExpect(jsonPath("$.groups[0].offers[1].source").value(JobOfferSourceType.THE_PROTOCOL_IT.name()))
                .andExpect(jsonPath("$.groups[0].offers[1].externalId").value("the-protocol-id"));
    }

    @Test
    void shouldKeepDifferentFingerprintsAsDifferentAnalyticalOffers() throws Exception {
        savedOffer(JobOfferSourceType.JUST_JOIN_IT.name(), "first-id", SHARED_FINGERPRINT);
        savedOffer(JobOfferSourceType.THE_PROTOCOL_IT.name(), "second-id", OTHER_FINGERPRINT);

        mockMvc.perform(get("/v1.0/analytics/deduplication-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawOfferCount").value(2))
                .andExpect(jsonPath("$.uniqueOfferCount").value(2))
                .andExpect(jsonPath("$.crossSourceDuplicateGroupCount").value(0))
                .andExpect(jsonPath("$.groups").isEmpty());
    }

    @Test
    void shouldRejectUnboundedGroupPageSize() throws Exception {
        mockMvc.perform(get("/v1.0/analytics/deduplication-audit")
                        .param("groupSize", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotDeleteSourceOffersWhenBuildingAudit() throws Exception {
        OfferEntity justJoinItOffer = savedOffer(
                JobOfferSourceType.JUST_JOIN_IT.name(), "just-join-it-id", SHARED_FINGERPRINT
        );
        OfferEntity theProtocolOffer = savedOffer(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "the-protocol-id", SHARED_FINGERPRINT
        );

        mockMvc.perform(get("/v1.0/analytics/deduplication-audit"))
                .andExpect(status().isOk());

        assertThat(offerRepository.findAll())
                .extracting(OfferEntity::getId)
                .containsExactlyInAnyOrder(justJoinItOffer.getId(), theProtocolOffer.getId());
    }

    @Test
    void shouldTreatNullFingerprintAsStandaloneAnalyticalOffer() throws Exception {
        savedOffer(JobOfferSourceType.JUST_JOIN_IT.name(), "first-id", null);
        savedOffer(JobOfferSourceType.THE_PROTOCOL_IT.name(), "second-id", null);

        mockMvc.perform(get("/v1.0/analytics/deduplication-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawOfferCount").value(2))
                .andExpect(jsonPath("$.uniqueOfferCount").value(2))
                .andExpect(jsonPath("$.crossSourceDuplicateGroupCount").value(0))
                .andExpect(jsonPath("$.groups").isEmpty());
    }

    @Test
    void shouldNotTreatSameSourceFingerprintAsCrossSourceDuplicateGroup() throws Exception {
        savedOffer(JobOfferSourceType.JUST_JOIN_IT.name(), "first-id", SHARED_FINGERPRINT);
        savedOffer(JobOfferSourceType.JUST_JOIN_IT.name(), "second-id", SHARED_FINGERPRINT);

        mockMvc.perform(get("/v1.0/analytics/deduplication-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rawOfferCount").value(2))
                .andExpect(jsonPath("$.uniqueOfferCount").value(2))
                .andExpect(jsonPath("$.crossSourceDuplicateGroupCount").value(0))
                .andExpect(jsonPath("$.groups").isEmpty());
    }

    private OfferEntity savedOffer(String source, String externalId, String fingerprint) {
        OfferEntity offer = new OfferEntity();
        offer.setSource(source);
        offer.setExternalId(externalId);
        offer.setTitle("Java Developer");
        offer.setCompanyName("Acme");
        offer.setLocation("Warsaw");
        offer.setDeduplicationFingerprint(fingerprint);
        return offerRepository.saveAndFlush(offer);
    }

    private static String fingerprint(char hashCharacter) {
        return "v1:" + String.valueOf(hashCharacter).repeat(64);
    }
}
