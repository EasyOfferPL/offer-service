package pl.easyoffer.offer_service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.service.synchronizer.OfferSynchronizer;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfferSyncScheduler {

    private final List<OfferSynchronizer> offerSynchronizers;

    @Scheduled(initialDelay = 0, fixedRate = 30 * 60 * 1000)
    private void synchronizeOffers() {
        log.info("Synchronizing offers - start");
        offerSynchronizers.forEach(OfferSynchronizer::synchronize);
        log.info("Synchronizing offers - end");
    }

}
