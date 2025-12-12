package pl.easyoffer.offer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.util.Set;

@Builder
@Data
@AllArgsConstructor
@EqualsAndHashCode
@FieldNameConstants
public class WorkConditions {

    private String workSchedule;
    private Set<String> contractTypes;
    private PositionGrade grade;
    private String workMode;
    private WorkSalary salary;

}
