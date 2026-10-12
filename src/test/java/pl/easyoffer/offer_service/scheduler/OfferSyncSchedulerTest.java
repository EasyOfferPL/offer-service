package pl.easyoffer.offer_service.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferSyncSchedulerTest {

    @Mock
    private OfferSynchronizer firstSynchronizer;

    @Mock
    private OfferSynchronizer secondSynchronizer;

    @Test
    void shouldSynchronizeEveryEnabledSource() {
        // given
        when(firstSynchronizer.getClassName()).thenReturn("FirstSynchronizer");
        when(secondSynchronizer.getClassName()).thenReturn("SecondSynchronizer");
        OfferSyncScheduler scheduler = new OfferSyncScheduler(List.of(firstSynchronizer, secondSynchronizer));

        // when
        scheduler.synchronizeOffers();

        // then
        verify(firstSynchronizer).synchronize();
        verify(secondSynchronizer).synchronize();
    }

    @Test
    void shouldContinueWithRemainingSourcesWhenOneSynchronizationFails() {
        // given
        when(firstSynchronizer.getClassName()).thenReturn("FirstSynchronizer");
        when(secondSynchronizer.getClassName()).thenReturn("SecondSynchronizer");
        doThrow(new IllegalStateException("Source unavailable"))
                .when(firstSynchronizer).synchronize();
        OfferSyncScheduler scheduler = new OfferSyncScheduler(List.of(firstSynchronizer, secondSynchronizer));

        // when
        assertThatCode(scheduler::synchronizeOffers).doesNotThrowAnyException();

        // then
        InOrder synchronizationOrder = inOrder(firstSynchronizer, secondSynchronizer);
        synchronizationOrder.verify(firstSynchronizer).synchronize();
        synchronizationOrder.verify(secondSynchronizer).synchronize();
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
}
