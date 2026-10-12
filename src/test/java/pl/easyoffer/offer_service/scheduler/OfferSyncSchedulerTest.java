package pl.easyoffer.offer_service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationRunService;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferSyncSchedulerTest {

    @Mock
    private OfferSynchronizer firstSynchronizer;

    @Mock
    private OfferSynchronizer secondSynchronizer;

    @Mock
    private SynchronizationRunService synchronizationRunService;

    @Test
    void shouldAuditSuccessfulSynchronizationForEveryEnabledSource() {
        // given
        SynchronizationResult firstResult = new SynchronizationResult(12, 12);
        SynchronizationResult secondResult = new SynchronizationResult(8, 8);
        when(firstSynchronizer.getSource()).thenReturn(JobOfferSourceType.JUST_JOIN_IT);
        when(secondSynchronizer.getSource()).thenReturn(JobOfferSourceType.THE_PROTOCOL_IT);
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.JUST_JOIN_IT), any(Instant.class)))
                .thenReturn(1L);
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.THE_PROTOCOL_IT), any(Instant.class)))
                .thenReturn(2L);
        when(firstSynchronizer.synchronize()).thenReturn(firstResult);
        when(secondSynchronizer.synchronize()).thenReturn(secondResult);
        OfferSyncScheduler scheduler = scheduler();

        // when
        scheduler.synchronizeOffers();

        // then
        verify(firstSynchronizer).synchronize();
        verify(secondSynchronizer).synchronize();
        verify(synchronizationRunService).markSucceeded(eq(1L), eq(firstResult), any(Instant.class));
        verify(synchronizationRunService).markSucceeded(eq(2L), eq(secondResult), any(Instant.class));
        verify(synchronizationRunService, never()).markFailed(anyLong(), any(), any());
        ArgumentCaptor<UUID> cycleIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(synchronizationRunService, times(2))
                .startRun(cycleIdCaptor.capture(), any(JobOfferSourceType.class), any(Instant.class));
        assertThat(cycleIdCaptor.getAllValues()).containsOnly(cycleIdCaptor.getValue());
    }

    @Test
    void shouldContinueWithRemainingSourcesWhenOneSynchronizationFails() {
        // given
        when(firstSynchronizer.getSource()).thenReturn(JobOfferSourceType.JUST_JOIN_IT);
        when(secondSynchronizer.getSource()).thenReturn(JobOfferSourceType.THE_PROTOCOL_IT);
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.JUST_JOIN_IT), any(Instant.class)))
                .thenReturn(1L);
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.THE_PROTOCOL_IT), any(Instant.class)))
                .thenReturn(2L);
        doThrow(new IllegalStateException("Source unavailable"))
                .when(firstSynchronizer).synchronize();
        when(secondSynchronizer.synchronize()).thenReturn(new SynchronizationResult(3, 3));
        OfferSyncScheduler scheduler = scheduler();

        // when
        assertThatCode(scheduler::synchronizeOffers).doesNotThrowAnyException();

        // then
        InOrder synchronizationOrder = inOrder(firstSynchronizer, secondSynchronizer);
        synchronizationOrder.verify(firstSynchronizer).synchronize();
        synchronizationOrder.verify(secondSynchronizer).synchronize();
        verify(synchronizationRunService).markFailed(eq(1L), any(IllegalStateException.class), any(Instant.class));
        verify(synchronizationRunService).markSucceeded(eq(2L), any(SynchronizationResult.class), any(Instant.class));
    }

    @Test
    void shouldSkipSourceWhenItsAuditRecordCannotBeStartedAndContinueWithRemainingSources() {
        // given
        when(firstSynchronizer.getSource()).thenReturn(JobOfferSourceType.JUST_JOIN_IT);
        when(secondSynchronizer.getSource()).thenReturn(JobOfferSourceType.THE_PROTOCOL_IT);
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.JUST_JOIN_IT), any(Instant.class)))
                .thenThrow(new IllegalStateException("Database unavailable"));
        when(synchronizationRunService.startRun(any(UUID.class), eq(JobOfferSourceType.THE_PROTOCOL_IT), any(Instant.class)))
                .thenReturn(2L);
        when(secondSynchronizer.synchronize()).thenReturn(new SynchronizationResult(3, 3));
        OfferSyncScheduler scheduler = scheduler();

        // when
        assertThatCode(scheduler::synchronizeOffers).doesNotThrowAnyException();

        // then
        verify(firstSynchronizer, never()).synchronize();
        verify(secondSynchronizer).synchronize();
        verify(synchronizationRunService).markSucceeded(eq(2L), any(SynchronizationResult.class), any(Instant.class));
    }

    @Test
    void shouldUseExternallyConfigurableCronAndTimeZone() throws NoSuchMethodException {
        // given
        Method scheduledMethod = OfferSyncScheduler.class.getDeclaredMethod("synchronizeOffers");

        // when
        Scheduled scheduled = scheduledMethod.getAnnotation(Scheduled.class);

        // then
        assertThat(scheduled).isNotNull();
        assertThat(scheduled.cron()).isEqualTo("${scheduler.offer-synchronization.cron}");
        assertThat(scheduled.zone()).isEqualTo("${scheduler.offer-synchronization.zone}");
    }

    private OfferSyncScheduler scheduler() {
        return new OfferSyncScheduler(
                List.of(firstSynchronizer, secondSynchronizer),
                synchronizationRunService
        );
    }
}
