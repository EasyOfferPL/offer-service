package pl.easyoffer.offer_service.util;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import org.apache.logging.log4j.util.Strings;

import java.util.stream.Stream;

@UtilityClass
public class EnumUtils {

    public static <T extends Enum<T>> T getEnum(@NotNull Class<T> clazz, String value, T fallbackValue) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException("Clazz %s should be enum".formatted(clazz.getSimpleName()));
        }
        if (Strings.isBlank(value)) {
            return fallbackValue;
        }
        try {
            return Stream.of(clazz.getEnumConstants())
                    .filter(enumValue -> value.equalsIgnoreCase(enumValue.name()))
                    .findFirst()
                    .orElse(fallbackValue);
        } catch (Exception e) {
            return fallbackValue;
        }
    }

}
