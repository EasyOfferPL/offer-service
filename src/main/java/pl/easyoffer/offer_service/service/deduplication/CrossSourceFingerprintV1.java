package pl.easyoffer.offer_service.service.deduplication;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public final class CrossSourceFingerprintV1 {

    public static final int FINGERPRINT_LENGTH = 67;

    private static final String VERSION = "v1";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String COMPONENT_SEPARATOR = ":";
    private static final String PAYLOAD_FIELD_SEPARATOR = "\u001F";
    private static final String NORMALIZED_WHITESPACE = " ";
    private static final Pattern WHITESPACE = Pattern.compile(
            "\\s+", Pattern.UNICODE_CHARACTER_CLASS
    );

    private CrossSourceFingerprintV1() {
    }

    public static Optional<String> calculate(String title, String companyName, String location) {
        String canonicalTitle = canonicalize(title);
        String canonicalCompanyName = canonicalize(companyName);
        String canonicalLocation = canonicalize(location);
        if (canonicalTitle == null || canonicalCompanyName == null || canonicalLocation == null) {
            return Optional.empty();
        }

        String payload = String.join(
                PAYLOAD_FIELD_SEPARATOR,
                VERSION,
                lengthPrefixed(canonicalTitle),
                lengthPrefixed(canonicalCompanyName),
                lengthPrefixed(canonicalLocation)
        );
        return Optional.of(VERSION + COMPONENT_SEPARATOR + sha256(payload));
    }

    private static String canonicalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .strip()
                .toLowerCase(Locale.ROOT);
        return WHITESPACE.matcher(normalized).replaceAll(NORMALIZED_WHITESPACE);
    }

    private static String lengthPrefixed(String value) {
        return value.length() + COMPONENT_SEPARATOR + value;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(HASH_ALGORITHM + " is not available", exception);
        }
    }
}
