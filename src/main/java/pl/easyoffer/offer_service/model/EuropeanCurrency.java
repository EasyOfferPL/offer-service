package pl.easyoffer.offer_service.model;

public enum EuropeanCurrency {
    EUR("EUR", "€"),
    PLN("PLN", "zł"),
    GBP("GBP", "£"),
    CHF("CHF", "CHF"),
    CZK("CZK", "Kč"),
    HUF("HUF", "Ft"),
    RON("RON", "lei"),
    SEK("SEK", "kr"),
    NOK("NOK", "kr"),
    DKK("DKK", "kr"),
    BGN("BGN", "лв"),
    HRK("HRK", "kn");

    private final String code;
    private final String symbol;

    EuropeanCurrency(String code, String symbol) {
        this.code = code;
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public String getSymbol() {
        return symbol;
    }

    public static String resolveCode(String value) {
        if (value == null || value.isBlank()) {
            return PLN.code;
        }

        for (EuropeanCurrency currency : values()) {
            if (currency.code.equalsIgnoreCase(value) || currency.symbol.equals(value)) {
                return currency.code;
            }
        }

        return value;
    }
}
