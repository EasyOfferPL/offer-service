package pl.easyoffer.offer_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;

import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferSyncScheduler {

    private final List<OfferSynchronizer> offerSynchronizers;

    @Scheduled(initialDelay = 0/*, cron = "0 0 4 * * *"*/)
    private void synchronizeOffers() {
        log.info("Synchronizing offers - start");
        StopWatch totalWatch = new StopWatch("offer-synchronization");
        totalWatch.start();
        for (OfferSynchronizer offerSynchronizer : offerSynchronizers) {
            log.info("{} - start", offerSynchronizer.getClassName());
            StopWatch stepWatch = new StopWatch();
            stepWatch.start();
            offerSynchronizer.synchronize();
            stepWatch.stop();
            log.info("{} - end, duration={} seconds", offerSynchronizer.getClassName(), (int) stepWatch.getTotalTime(SECONDS));
        }
        totalWatch.stop();
        log.info("Synchronizing offers - end, totalDuration={} seconds", (int) totalWatch.getTotalTime(SECONDS));
    }

}
