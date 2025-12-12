package pl.easyoffer.offer_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferContext implements Serializable {

    private LocalDate publishDate;
    private String title;
    private String url;
    private String description;
    private String companyName;
    private String companyAddress;
    private WorkConditions workConditions;
    private Set<TechSkill> techSkills;

}
