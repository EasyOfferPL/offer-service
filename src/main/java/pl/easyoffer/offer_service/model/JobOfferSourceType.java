package pl.easyoffer.offer_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JobOfferSourceType {
    MANUAL("manual"),
    JUST_JOIN_IT("justjoinit");

    private final String name;

}
