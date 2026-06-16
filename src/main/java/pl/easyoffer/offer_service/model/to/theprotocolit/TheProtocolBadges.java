package pl.easyoffer.offer_service.model.to.theprotocolit;

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
