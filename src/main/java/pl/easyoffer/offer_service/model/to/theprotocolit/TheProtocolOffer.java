package pl.easyoffer.offer_service.model.to.theprotocolit;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TheProtocolOffer {

    private String id;
    private String groupId;
    private String title;
    private String employer;
    private String employerId;
    private String logoUrl;
    private String offerUrlName;
    private List<String> aboutProject = new ArrayList<>();
    private List<TheProtocolWorkplace> workplace = new ArrayList<>();
    private List<TheProtocolPositionLevel> positionLevels = new ArrayList<>();
    private List<TheProtocolContract> typesOfContracts = new ArrayList<>();
    private List<String> technologies = new ArrayList<>();
    private List<String> workModes = new ArrayList<>();
    private Boolean isNew;
    private LocalDateTime publicationDateUtc;
    private Boolean lastCall;
    private String language;
    private TheProtocolSalary salary;
    private Boolean immediateEmployment;
    private Boolean isSupportingUkraine;
    private TheProtocolAddons addons;
    private Boolean isFromExternalLocations;
    private TheProtocolBadges badges;
    private Object alpha;

}