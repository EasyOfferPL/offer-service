package pl.easyoffer.offer_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;

import java.util.List;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferSyncScheduler {

    private final List<OfferSynchronizer> offerSynchronizers;

    @Scheduled(
            cron = "${scheduler.offer-synchronization.cron}",
            zone = "${scheduler.offer-synchronization.zone}"
    )
    void synchronizeOffers() {
        log.info("Synchronizing offers - start");
        long synchronizationStartedAt = System.nanoTime();
        int successfulSynchronizations = 0;
        int failedSynchronizations = 0;

        for (OfferSynchronizer offerSynchronizer : offerSynchronizers) {
            String synchronizerName = offerSynchronizer.getClassName();
            long sourceSynchronizationStartedAt = System.nanoTime();
            log.info("{} - start", synchronizerName);
            try {
                offerSynchronizer.synchronize();
                successfulSynchronizations++;
                log.info("{} - end, duration={} ms", synchronizerName,
                        elapsedMillis(sourceSynchronizationStartedAt));
            } catch (RuntimeException exception) {
                failedSynchronizations++;
                log.error("{} - failed, duration={} ms", synchronizerName,
                        elapsedMillis(sourceSynchronizationStartedAt), exception);
            }
        }

        log.info("Synchronizing offers - end, successfulSources={}, failedSources={}, totalDuration={} ms",
                successfulSynchronizations, failedSynchronizations, elapsedMillis(synchronizationStartedAt));
    }

    private long elapsedMillis(long startedAt) {
        return NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
