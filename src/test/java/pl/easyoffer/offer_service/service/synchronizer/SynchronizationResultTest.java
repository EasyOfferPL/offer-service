package pl.easyoffer.offer_service.service.synchronizer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizationResultTest {

    @Test
    void shouldRejectNegativeCounts() {
        assertThatThrownBy(() -> new SynchronizationResult(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Synchronization counts cannot be negative");
    }

    @Test
    void shouldRejectPersistedCountGreaterThanFetchedCount() {
        assertThatThrownBy(() -> new SynchronizationResult(1, 2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Persisted offer count cannot exceed fetched offer count");
    }
}
