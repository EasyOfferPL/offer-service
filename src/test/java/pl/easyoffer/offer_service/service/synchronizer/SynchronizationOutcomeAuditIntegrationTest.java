package pl.easyoffer.offer_service.service.synchronizer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SynchronizationOutcomeAuditIntegrationTest {

    @Autowired
    private SynchronizationRunService synchronizationRunService;

    @Autowired
    private SynchronizationRunRepository synchronizationRunRepository;

    @AfterEach
    void cleanSynchronizationRuns() {
        synchronizationRunRepository.deleteAll();
    }

    @Test
    void shouldPersistReconciledSynchronizationOutcomeCounts() {
        Instant startedAt = Instant.parse("2026-10-14T08:00:00Z");
        Long runId = synchronizationRunService.startRun(
                UUID.randomUUID(), JobOfferSourceType.JUST_JOIN_IT, startedAt
        );

        synchronizationRunService.markSucceeded(
                runId,
                new SynchronizationResult(4, 1, 2, 1),
                startedAt.plusSeconds(30)
        );

        SynchronizationRunEntity persistedRun = synchronizationRunRepository.findById(runId).orElseThrow();
        assertThat(persistedRun.getFetchedOffers()).isEqualTo(4);
        assertThat(persistedRun.getCreatedOffers()).isEqualTo(1);
        assertThat(persistedRun.getUpdatedOffers()).isEqualTo(2);
        assertThat(persistedRun.getSkippedOffers()).isEqualTo(1);
        assertThat(persistedRun.getPersistedOffers()).isEqualTo(3);
    }
}
