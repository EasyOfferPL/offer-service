package pl.easyoffer.offer_service.model.dto.theprotocolit;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
@AllArgsConstructor
public enum TheProtocolItWorkMode {
    REMOTE(Set.of("zdalna", "remote")),
    HYBRID(Set.of("hybrydowa","hybrid")),
    OFFICE(Set.of("stacjonarna", "office"));


    private final Set<String> values;

    public static String getNameByValue(String value) {
        TheProtocolItWorkMode type = fromValue(value);
        return type != null ? type.name() : null;
    }

    public static TheProtocolItWorkMode fromValue(String value) {
        return Arrays.stream(values())
                .filter(type -> type.values.contains(value))
                .findFirst()
                .orElse(null);
    }

}
