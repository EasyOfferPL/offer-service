package pl.easyoffer.offer_service.model.dto.justjoinit;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class JustJoinItOffer {

    private String guid;
    private String slug;
    private String title;
    private String workplaceType;
    private String workingTime;
    private String experienceLevel;
    private JustJoinItCategory category;
    private String city;
    private String street;
    private Double latitude;
    private Double longitude;
    private Boolean isRemoteInterview;
    private String companyName;
    private String companyLogoThumbUrl;
    private LocalDateTime publishedAt;
    private LocalDateTime lastPublishedAt;
    private LocalDateTime expiredAt;
    private Boolean isOpenToHireUkrainians;
    private List<JustJoinItLocation> locations;
    private List<JustJoinItEmploymentType> employmentTypes;
    private List<JustJoinItSkill> requiredSkills;
    private List<JustJoinItSkill> niceToHaveSkills;
    private List<JustJoinItLanguage> languages;
    private Boolean isPromoted;
    private Boolean isSuperOffer;
    private String applyMethod;

    public List<JustJoinItSkill> getAllSkills() {
        List<JustJoinItSkill> skills = new ArrayList<>(this.requiredSkills);
        skills.addAll(this.niceToHaveSkills);
        return skills;
    }

}
