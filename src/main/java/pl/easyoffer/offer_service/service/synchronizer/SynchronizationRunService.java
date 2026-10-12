package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SynchronizationRunService {

    private final SynchronizationRunRepository synchronizationRunRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long startRun(UUID cycleId, JobOfferSourceType source, Instant startedAt) {
        SynchronizationRunEntity run = SynchronizationRunEntity.start(cycleId, source, startedAt);
        return synchronizationRunRepository.save(run).getId();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSucceeded(Long runId, SynchronizationResult result, Instant finishedAt) {
        SynchronizationRunEntity run = findRun(runId);
        run.succeed(result, finishedAt);
        synchronizationRunRepository.save(run);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long runId, RuntimeException exception, Instant finishedAt) {
        SynchronizationRunEntity run = findRun(runId);
        run.fail(exception, finishedAt);
        synchronizationRunRepository.save(run);
    }

    private SynchronizationRunEntity findRun(Long runId) {
        return synchronizationRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalStateException("Synchronization run not found: " + runId));
    }
}
