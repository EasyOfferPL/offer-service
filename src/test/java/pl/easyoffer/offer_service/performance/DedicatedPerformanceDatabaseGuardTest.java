package pl.easyoffer.offer_service.performance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DedicatedPerformanceDatabaseGuardTest {

    @Test
    void acceptsDedicatedDatabaseWithExplicitConfirmation() {
        assertThatCode(() -> DedicatedPerformanceDatabaseGuard.verify(
                "jdbc:postgresql://127.0.0.1:5434/offerdb_performance",
                DedicatedPerformanceDatabaseGuard.REQUIRED_DATABASE_NAME
        )).doesNotThrowAnyException();
    }

    @Test
    void rejectsOrdinaryApplicationDatabase() {
        assertThatThrownBy(() -> DedicatedPerformanceDatabaseGuard.verify(
                "jdbc:postgresql://127.0.0.1:5433/offerdb",
                DedicatedPerformanceDatabaseGuard.REQUIRED_DATABASE_NAME
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("refuses");
    }

    @Test
    void rejectsMissingConfirmation() {
        assertThatThrownBy(() -> DedicatedPerformanceDatabaseGuard.verify(
                "jdbc:postgresql://127.0.0.1:5434/offerdb_performance",
                null
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("confirmDatabase");
    }
}
