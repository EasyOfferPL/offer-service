package pl.easyoffer.offer_service.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SynchronizationRunStatus;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "SYNCHRONIZATION_RUNS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SynchronizationRunEntity {

    public static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CYCLE_ID", nullable = false)
    private UUID cycleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "SOURCE", nullable = false, length = 100)
    private JobOfferSourceType source;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private SynchronizationRunStatus status;

    @Column(name = "STARTED_AT", nullable = false)
    private Instant startedAt;

    @Column(name = "FINISHED_AT")
    private Instant finishedAt;

    @Column(name = "FETCHED_OFFERS")
    private Integer fetchedOffers;

    @Column(name = "PERSISTED_OFFERS")
    private Integer persistedOffers;

    @Column(name = "CREATED_OFFERS")
    private Integer createdOffers;

    @Column(name = "UPDATED_OFFERS")
    private Integer updatedOffers;

    @Column(name = "SKIPPED_OFFERS")
    private Integer skippedOffers;

    @Column(name = "ERROR_TYPE", length = 255)
    private String errorType;

    @Column(name = "ERROR_MESSAGE", length = MAX_ERROR_MESSAGE_LENGTH)
    private String errorMessage;

    public static SynchronizationRunEntity start(
            UUID cycleId,
            JobOfferSourceType source,
            Instant startedAt
    ) {
        SynchronizationRunEntity run = new SynchronizationRunEntity();
        run.cycleId = cycleId;
        run.source = source;
        run.startedAt = startedAt;
        run.status = SynchronizationRunStatus.RUNNING;
        return run;
    }

    public void succeed(SynchronizationResult result, Instant finishedAt) {
        finish(finishedAt);
        status = SynchronizationRunStatus.SUCCEEDED;
        fetchedOffers = result.fetchedOffers();
        persistedOffers = result.persistedOffers();
        createdOffers = result.createdOffers();
        updatedOffers = result.updatedOffers();
        skippedOffers = result.skippedOffers();
    }

    public void fail(RuntimeException exception, Instant finishedAt) {
        finish(finishedAt);
        status = SynchronizationRunStatus.FAILED;
        errorType = exception.getClass().getName();
        errorMessage = truncate(exception.getMessage());
    }

    private void finish(Instant finishedAt) {
        if (status != SynchronizationRunStatus.RUNNING) {
            throw new IllegalStateException("Synchronization run is already finished");
        }
        if (finishedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Synchronization run cannot finish before it starts");
        }
        this.finishedAt = finishedAt;
    }

    private String truncate(String message) {
        if (message == null || message.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
