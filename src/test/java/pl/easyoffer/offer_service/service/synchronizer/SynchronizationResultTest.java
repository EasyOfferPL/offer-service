package pl.easyoffer.offer_service.service.synchronizer;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizationResultTest {

    @Test
    void shouldRejectNegativeCounts() {
        assertThatThrownBy(() -> new SynchronizationResult(-1, 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Synchronization counts cannot be negative");
    }

    @Test
    void shouldRejectCountsThatDoNotReconcileWithFetchedOffers() {
        assertThatThrownBy(() -> new SynchronizationResult(3, 1, 1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Fetched offer count must equal created, updated and skipped offer counts");
    }

    @Test
    void shouldCountPersistenceOutcomesAndDerivePersistedOffers() {
        SynchronizationResult result = SynchronizationResult.fromOutcomes(List.of(
                OfferPersistenceOutcome.CREATED,
                OfferPersistenceOutcome.CREATED,
                OfferPersistenceOutcome.UPDATED,
                OfferPersistenceOutcome.SKIPPED
        ));

        assertThat(result.fetchedOffers()).isEqualTo(4);
        assertThat(result.createdOffers()).isEqualTo(2);
        assertThat(result.updatedOffers()).isEqualTo(1);
        assertThat(result.skippedOffers()).isEqualTo(1);
        assertThat(result.persistedOffers()).isEqualTo(3);
    }

    @Test
    void shouldMarkEveryFetchedOfferAsSkippedWhenPersistenceIsUnavailable() {
        SynchronizationResult result = SynchronizationResult.allSkipped(3);

        assertThat(result.fetchedOffers()).isEqualTo(3);
        assertThat(result.createdOffers()).isZero();
        assertThat(result.updatedOffers()).isZero();
        assertThat(result.skippedOffers()).isEqualTo(3);
        assertThat(result.persistedOffers()).isZero();
    }
}
