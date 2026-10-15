package pl.easyoffer.offer_service.model;

import java.util.Locale;

public enum SalaryType {
    GROSS,
    NET,
    UNKNOWN;

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
        if (normalized.contains("brutto") || normalized.contains("gross")) {
            return GROSS;
        }
        if (normalized.contains("netto") || normalized.equals("net")) {
            return NET;
        }
        return UNKNOWN;
    }
}
