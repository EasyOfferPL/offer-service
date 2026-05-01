package pl.easyoffer.offer_service.model.dto.theprotocolit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TheProtocolItTimeUnitType {
    HOUR(Set.of("hr.", "godz.")),
    MONTH(Set.of("mth.", "mies."));

    private final Set<String> value;

    public static String getNameByValue(String value) {
        TheProtocolItTimeUnitType type = fromValue(value);
        return type != null ? type.name() : null;
    }

    public static TheProtocolItTimeUnitType fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.contains(value))
                .findFirst()
                .orElse(null);
    }

}
