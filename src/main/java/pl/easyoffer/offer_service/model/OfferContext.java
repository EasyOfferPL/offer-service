package pl.easyoffer.offer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class OfferContext {

    private LocalDate publishDate;
    private String title;
    private String url;
    private String description;
    private String companyName;
    private String companyAddress;
    private WorkConditions workConditions;
    private Set<TechSkill> techSkills;

}
