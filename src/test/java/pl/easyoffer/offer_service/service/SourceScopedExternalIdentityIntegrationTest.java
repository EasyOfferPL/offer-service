package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.repository.OfferRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class SourceScopedExternalIdentityIntegrationTest {

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferRepository offerRepository;

    @AfterEach
    void cleanOffers() {
        offerRepository.deleteAll();
    }

    @Test
    void shouldKeepSameExternalIdFromDifferentSourcesAsSeparateOffers() {
        OfferResponseTO justJoinItOffer = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "shared-id", "Java Developer"
        ));
        OfferResponseTO theProtocolOffer = offerService.createOrUpdate(request(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "shared-id", "Backend Engineer"
        ));

        assertThat(theProtocolOffer.getId()).isNotEqualTo(justJoinItOffer.getId());
        assertThat(offerRepository.count()).isEqualTo(2);
        assertThat(offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.JUST_JOIN_IT.name(), "shared-id"
        )).get().extracting(OfferEntity::getTitle).isEqualTo("Java Developer");
        assertThat(offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "shared-id"
        )).get().extracting(OfferEntity::getTitle).isEqualTo("Backend Engineer");
    }

    @Test
    void shouldPreserveBothSourceIdentitiesWhenCoreFieldsMatch() {
        OfferResponseTO justJoinItOffer = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "just-join-it-id", "Java Developer"
        ));
        OfferResponseTO theProtocolOffer = offerService.createOrUpdate(request(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "the-protocol-id", "Java Developer"
        ));

        assertThat(theProtocolOffer.getId()).isNotEqualTo(justJoinItOffer.getId());
        assertThat(offerRepository.count()).isEqualTo(2);
        assertThat(offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.JUST_JOIN_IT.name(), "just-join-it-id"
        )).isPresent();
        assertThat(offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "the-protocol-id"
        )).isPresent();
        OfferEntity justJoinItEntity = offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.JUST_JOIN_IT.name(), "just-join-it-id"
        ).orElseThrow();
        OfferEntity theProtocolEntity = offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.THE_PROTOCOL_IT.name(), "the-protocol-id"
        ).orElseThrow();
        assertThat(justJoinItEntity.getDeduplicationFingerprint())
                .isNotNull()
                .isEqualTo(theProtocolEntity.getDeduplicationFingerprint());
    }

    @Test
    void shouldUseSourceScopedFingerprintWhenExternalIdIsMissing() {
        OfferRequestTO originalRequest = request(
                JobOfferSourceType.JUST_JOIN_IT.name(), null, "Java Developer"
        );
        originalRequest.setCompanyName("ACME");
        OfferRequestTO repeatedRequest = request(
                JobOfferSourceType.JUST_JOIN_IT.name(), null, "  java   developer  "
        );
        repeatedRequest.setCompanyName(" acme ");
        repeatedRequest.setLocation(" WARSAW ");

        OfferResponseTO original = offerService.createOrUpdate(originalRequest);
        OfferResponseTO repeated = offerService.createOrUpdate(repeatedRequest);

        assertThat(repeated.getId()).isEqualTo(original.getId());
        assertThat(offerRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldUpdateOfferWhenSourceAndExternalIdAreUnchanged() {
        OfferResponseTO original = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "stable-id", "Original title"
        ));
        String originalFingerprint = offerRepository.findById(original.getId())
                .orElseThrow()
                .getDeduplicationFingerprint();
        OfferResponseTO updated = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "stable-id", "Updated title"
        ));
        String updatedFingerprint = offerRepository.findById(updated.getId())
                .orElseThrow()
                .getDeduplicationFingerprint();

        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getTitle()).isEqualTo("Updated title");
        assertThat(updatedFingerprint).isNotEqualTo(originalFingerprint);
        assertThat(offerRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldPreserveDistinctExternalIdentitiesWithinOneSource() {
        OfferResponseTO first = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "first-id", "Java Developer"
        ));
        OfferResponseTO second = offerService.createOrUpdate(request(
                JobOfferSourceType.JUST_JOIN_IT.name(), "second-id", "Java Developer"
        ));

        assertThat(second.getId()).isNotEqualTo(first.getId());
        assertThat(offerRepository.count()).isEqualTo(2);
    }

    @Test
    void shouldDefaultBlankSourceToManual() {
        OfferResponseTO saved = offerService.createOrUpdate(request("  ", "manual-id", "Manual offer"));

        assertThat(saved.getSource()).isEqualTo(JobOfferSourceType.MANUAL.name());
        assertThat(offerRepository.findBySourceAndExternalId(
                JobOfferSourceType.MANUAL.name(), "manual-id"
        )).isPresent();
    }

    @Test
    void shouldRejectDuplicateSourceAndExternalIdAtDatabaseBoundary() {
        offerRepository.saveAndFlush(entity(
                JobOfferSourceType.JUST_JOIN_IT.name(), "duplicate-id", "First offer"
        ));

        assertThatThrownBy(() -> offerRepository.saveAndFlush(entity(
                JobOfferSourceType.JUST_JOIN_IT.name(), "duplicate-id", "Second offer"
        ))).isInstanceOf(DataIntegrityViolationException.class);
    }

    private static OfferRequestTO request(String source, String externalId, String title) {
        OfferRequestTO request = new OfferRequestTO();
        request.setSource(source);
        request.setExternalId(externalId);
        request.setTitle(title);
        request.setCompanyName("Company for " + title);
        request.setLocation("Warsaw");
        request.setExperienceLevel("MID");
        request.setEmploymentType("B2B");
        request.setWorkMode("REMOTE");
        return request;
    }

    private static OfferEntity entity(String source, String externalId, String title) {
        OfferEntity entity = new OfferEntity();
        entity.setSource(source);
        entity.setExternalId(externalId);
        entity.setTitle(title);
        entity.setCompanyName("Company for " + title);
        return entity;
    }
}
