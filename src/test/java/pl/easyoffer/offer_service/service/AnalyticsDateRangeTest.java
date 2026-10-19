package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyticsDateRangeTest {

    @Test
    void shouldCreateHalfOpenTimestampRangeFromInclusiveCalendarDates() {
        AnalyticsDateRange range = AnalyticsDateRange.inclusive(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31)
        );

        assertThat(range.startInclusive()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
        assertThat(range.endExclusive()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void shouldRejectInvertedRangeAndDateWithoutRepresentableFollowingDay() {
        assertThatThrownBy(() -> AnalyticsDateRange.inclusive(
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 1)
        )).isInstanceOf(ValidationException.class)
                .hasMessage("dateFrom must not be after dateTo");

        assertThatThrownBy(() -> AnalyticsDateRange.inclusive(LocalDate.MAX, LocalDate.MAX))
                .isInstanceOf(ValidationException.class)
                .hasMessage("dateTo must allow calculation of the following day");
    }
}
