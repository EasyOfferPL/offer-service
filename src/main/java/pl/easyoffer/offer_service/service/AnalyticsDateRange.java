package pl.easyoffer.offer_service.service;

import pl.easyoffer.offer_service.exception.ValidationException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inclusive calendar-date range exposed by analytics HTTP endpoints.
 *
 * <p>The persistence interval is half-open so every timestamp from {@code dateTo}
 * is included without relying on timestamp precision.</p>
 */
public final class AnalyticsDateRange {

    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private final LocalDateTime startInclusive;
    private final LocalDateTime endExclusive;

    private AnalyticsDateRange(
            LocalDate dateFrom,
            LocalDate dateTo,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    ) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    public static AnalyticsDateRange inclusive(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            throw new ValidationException("dateFrom and dateTo are required");
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new ValidationException("dateFrom must not be after dateTo");
        }

        try {
            return new AnalyticsDateRange(
                    dateFrom,
                    dateTo,
                    dateFrom.atStartOfDay(),
                    dateTo.plusDays(1).atStartOfDay()
            );
        } catch (DateTimeException exception) {
            throw new ValidationException("dateTo must allow calculation of the following day");
        }
    }

    public LocalDate dateFrom() {
        return dateFrom;
    }

    public LocalDate dateTo() {
        return dateTo;
    }

    public LocalDateTime startInclusive() {
        return startInclusive;
    }

    public LocalDateTime endExclusive() {
        return endExclusive;
    }
}
