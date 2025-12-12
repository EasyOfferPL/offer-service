package pl.easyoffer.offer_service.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class WorkConditions implements Serializable {

    private String workSchedule;
    private Set<String> contractTypes;
    private PositionGrade grade;
    private String workMode;
    private WorkSalary salary;

}
