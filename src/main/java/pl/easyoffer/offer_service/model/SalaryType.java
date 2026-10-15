package pl.easyoffer.offer_service.model;

import java.util.Locale;
import java.util.Set;

public enum SalaryType {
    GROSS,
    NET,
    UNKNOWN;

    private static final Set<String> GROSS_SOURCE_VALUES = Set.of(
            "brutto",
            "gross"
    );
    private static final Set<String> NET_SOURCE_VALUES = Set.of(
            "netto (+ vat)",
            "net (+ vat)",
            "netto",
            "net"
    );

    public static SalaryType fromGross(Boolean gross) {
        if (gross == null) {
            return UNKNOWN;
        }
        return gross ? GROSS : NET;
    }

    public static SalaryType fromSourceValue(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (GROSS_SOURCE_VALUES.contains(normalized)) {
            return GROSS;
        }
        if (NET_SOURCE_VALUES.contains(normalized)) {
            return NET;
        }
        return UNKNOWN;
    }
}
