package pl.easyoffer.offer_service.model.entity;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SynchronizationRunStatus;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizationRunEntityTest {

    private static final Instant STARTED_AT = Instant.parse("2026-10-12T08:00:00Z");

    @Test
    void shouldRecordSuccessfulRunCounts() {
        UUID cycleId = UUID.randomUUID();
        SynchronizationRunEntity run = SynchronizationRunEntity.start(
                cycleId, JobOfferSourceType.JUST_JOIN_IT, STARTED_AT
        );

        run.succeed(new SynchronizationResult(15, 14), STARTED_AT.plus(2, ChronoUnit.MINUTES));

        assertThat(run.getCycleId()).isEqualTo(cycleId);
        assertThat(run.getSource()).isEqualTo(JobOfferSourceType.JUST_JOIN_IT);
        assertThat(run.getStatus()).isEqualTo(SynchronizationRunStatus.SUCCEEDED);
        assertThat(run.getFetchedOffers()).isEqualTo(15);
        assertThat(run.getPersistedOffers()).isEqualTo(14);
        assertThat(run.getFinishedAt()).isEqualTo(STARTED_AT.plus(2, ChronoUnit.MINUTES));
        assertThat(run.getErrorType()).isNull();
        assertThat(run.getErrorMessage()).isNull();
    }

    @Test
    void shouldRecordFailureAndBoundErrorMessage() {
        SynchronizationRunEntity run = SynchronizationRunEntity.start(
                UUID.randomUUID(), JobOfferSourceType.THE_PROTOCOL_IT, STARTED_AT
        );
        IllegalStateException failure = new IllegalStateException("x".repeat(2500));

        run.fail(failure, STARTED_AT.plusSeconds(30));

        assertThat(run.getStatus()).isEqualTo(SynchronizationRunStatus.FAILED);
        assertThat(run.getErrorType()).isEqualTo(IllegalStateException.class.getName());
        assertThat(run.getErrorMessage()).hasSize(SynchronizationRunEntity.MAX_ERROR_MESSAGE_LENGTH);
        assertThat(run.getFetchedOffers()).isNull();
        assertThat(run.getPersistedOffers()).isNull();
    }

    @Test
    void shouldRejectSecondCompletion() {
        SynchronizationRunEntity run = SynchronizationRunEntity.start(
                UUID.randomUUID(), JobOfferSourceType.JUST_JOIN_IT, STARTED_AT
        );
        run.succeed(new SynchronizationResult(1, 1), STARTED_AT.plusSeconds(1));

        assertThatThrownBy(() -> run.fail(
                new IllegalStateException("late failure"), STARTED_AT.plusSeconds(2)
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Synchronization run is already finished");
    }
}
