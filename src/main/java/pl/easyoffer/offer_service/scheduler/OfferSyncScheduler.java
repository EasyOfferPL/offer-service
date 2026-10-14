package pl.easyoffer.offer_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationRunService;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferSyncScheduler {

    private final List<OfferSynchronizer> offerSynchronizers;
    private final SynchronizationRunService synchronizationRunService;

    @Scheduled(
            cron = "${scheduler.offer-synchronization.cron}",
            zone = "${scheduler.offer-synchronization.zone}"
    )
    void synchronizeOffers() {
        log.info("Synchronizing offers - start");
        long synchronizationStartedAt = System.nanoTime();
        UUID cycleId = UUID.randomUUID();
        int successfulSynchronizations = 0;
        int failedSynchronizations = 0;

        for (OfferSynchronizer offerSynchronizer : offerSynchronizers) {
            JobOfferSourceType source = offerSynchronizer.getSource();
            String sourceName = source.name();
            long sourceSynchronizationStartedAt = System.nanoTime();
            Long runId;

            try {
                runId = synchronizationRunService.startRun(
                        cycleId, source, Instant.now()
                );
            } catch (RuntimeException exception) {
                failedSynchronizations++;
                log.error("{} - audit start failed; synchronization skipped", sourceName, exception);
                continue;
            }

            log.info("{} - start, runId={}, cycleId={}", sourceName, runId, cycleId);
            try {
                SynchronizationResult result = offerSynchronizer.synchronize();
                synchronizationRunService.markSucceeded(runId, result, Instant.now());
                successfulSynchronizations++;
                log.info("{} - end, runId={}, fetchedOffers={}, createdOffers={}, updatedOffers={}, "
                                + "skippedOffers={}, persistedOffers={}, duration={} ms",
                        sourceName, runId, result.fetchedOffers(), result.createdOffers(), result.updatedOffers(),
                        result.skippedOffers(), result.persistedOffers(),
                        elapsedMillis(sourceSynchronizationStartedAt));
            } catch (RuntimeException exception) {
                failedSynchronizations++;
                markRunFailed(runId, exception);
                log.error("{} - failed, runId={}, duration={} ms", sourceName, runId,
                        elapsedMillis(sourceSynchronizationStartedAt), exception);
            }
        }

        log.info("Synchronizing offers - end, successfulSources={}, failedSources={}, totalDuration={} ms",
                successfulSynchronizations, failedSynchronizations, elapsedMillis(synchronizationStartedAt));
    }

    private void markRunFailed(Long runId, RuntimeException synchronizationException) {
        try {
            synchronizationRunService.markFailed(runId, synchronizationException, Instant.now());
        } catch (RuntimeException auditException) {
            synchronizationException.addSuppressed(auditException);
        }
    }

    private long elapsedMillis(long startedAt) {
        return NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
