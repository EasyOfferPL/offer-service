package pl.easyoffer.offer_service.model.to.theprotocolit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TheProtocolItEmploymentType {
    EMPLOYMENT(0),
    SPECIFIC(1),
    MANDATE(2),
    B2B(3),
    REPLACEMENT(4),
    INTERNSHIP(7);

    private final int value;

    public static String getNameByValue(Integer value) {
        TheProtocolItEmploymentType type = fromValue(value);
        return type != null ? type.name() : null;
    }

    public static TheProtocolItEmploymentType fromValue(int value) {
        return Arrays.stream(values())
                .filter(type -> type.value == value)
                .findFirst()
                .orElse(null);
    }

}
