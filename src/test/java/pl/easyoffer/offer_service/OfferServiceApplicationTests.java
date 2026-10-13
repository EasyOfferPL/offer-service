package pl.easyoffer.offer_service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SynchronizationRunStatus;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationRunService;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OfferServiceApplicationTests {

	@Autowired
	private Flyway flyway;

	@Autowired
	private SynchronizationRunService synchronizationRunService;

	@Autowired
	private SynchronizationRunRepository synchronizationRunRepository;

	@AfterEach
	void cleanSynchronizationRuns() {
		synchronizationRunRepository.deleteAll();
	}

	@Test
	void contextLoadsWithMigratedSchema() {
		assertThat(flyway.info().current())
				.isNotNull()
				.extracting(migrationInfo -> migrationInfo.getVersion().getVersion())
				.isEqualTo("4");
	}

	@Test
	void persistsSynchronizationRunLifecycle() {
		Instant startedAt = Instant.parse("2026-10-12T08:00:00Z");
		Instant finishedAt = Instant.parse("2026-10-12T08:01:00Z");
		UUID cycleId = UUID.randomUUID();

		Long runId = synchronizationRunService.startRun(
				cycleId, JobOfferSourceType.JUST_JOIN_IT, startedAt
		);
		synchronizationRunService.markSucceeded(
				runId, new SynchronizationResult(20, 18), finishedAt
		);

		SynchronizationRunEntity persistedRun = synchronizationRunRepository.findById(runId).orElseThrow();
		assertThat(persistedRun.getCycleId()).isEqualTo(cycleId);
		assertThat(persistedRun.getStatus()).isEqualTo(SynchronizationRunStatus.SUCCEEDED);
		assertThat(persistedRun.getStartedAt()).isEqualTo(startedAt);
		assertThat(persistedRun.getFinishedAt()).isEqualTo(finishedAt);
		assertThat(persistedRun.getFetchedOffers()).isEqualTo(20);
		assertThat(persistedRun.getPersistedOffers()).isEqualTo(18);
	}

}
