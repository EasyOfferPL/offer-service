package pl.easyoffer.offer_service.model.to;

import lombok.Builder;
import lombok.Data;
import pl.easyoffer.offer_service.model.SalaryType;

import java.util.List;

@Data
@Builder
public class SalaryTrendTO {

    private String categoryName;
    private String currency;
    private String employmentType;
    private String experienceLevel;
    private String salaryUnit;
    private SalaryType salaryType;
    private List<SalaryTrendPointTO> points;

}
