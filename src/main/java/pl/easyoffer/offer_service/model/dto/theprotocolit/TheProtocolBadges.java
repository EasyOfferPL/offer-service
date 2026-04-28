package pl.easyoffer.offer_service.model.dto.theprotocolit;

import lombok.Data;

@Data
public class TheProtocolBadges {

    private Boolean isNew;
    private Boolean lastCall;
    private Boolean immediateEmployment;
    private Boolean isSupportingUkraine;
    private Boolean isFromExternalLocations;
    private Boolean isQuickApply;

}
