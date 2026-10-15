package pl.easyoffer.offer_service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItEmploymentType;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOffer;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolOffer;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolSalary;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolTimeUnit;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalarySourceMappingTest {

    @Test
    void shouldMapJustJoinItGrossFlag() {
        JustJoinItEmploymentType employmentType = new JustJoinItEmploymentType();
        employmentType.setCurrencySource("ORIGINAL");
        employmentType.setCurrency("PLN");
        employmentType.setFromPerUnit(new BigDecimal("10000"));
        employmentType.setToPerUnit(new BigDecimal("14000"));
        employmentType.setType("b2b");
        employmentType.setUnit("month");
        employmentType.setGross(true);
        JustJoinItOffer source = new JustJoinItOffer();
        source.setEmploymentTypes(List.of(employmentType));
        OfferRequestTO target = new OfferRequestTO();

        JustJoinItOfferMapper.INSTANCE.determineSalaryParameters(source, target);

        assertThat(target.getSalaryType()).isEqualTo(SalaryType.GROSS);
        assertThat(target.getSalaryUnit()).isEqualTo("MONTH");
    }

    @Test
    void shouldMapTheProtocolSalaryKind() {
        TheProtocolTimeUnit timeUnit = new TheProtocolTimeUnit();
        timeUnit.setShortForm("mies.");
        TheProtocolSalary salary = new TheProtocolSalary();
        salary.setFrom(new BigDecimal("9000"));
        salary.setTo(new BigDecimal("12000"));
        salary.setCurrency("PLN");
        salary.setTimeUnit(timeUnit);
        salary.setKindName("netto");
        TheProtocolOffer source = new TheProtocolOffer();
        source.setSalary(salary);
        OfferRequestTO target = new OfferRequestTO();

        TheProtocolItMapper.INSTANCE.determineSalaryParameters(source, target);

        assertThat(target.getSalaryType()).isEqualTo(SalaryType.NET);
        assertThat(target.getSalaryUnit()).isEqualTo("MONTH");
    }

    @Test
    void shouldMapSalaryAndEmploymentTypeFromTheSameTheProtocolContract() throws Exception {
        String sourceJson = """
                {
                  "id": "01000000-183e-46c7-5a31-08df12fba9db",
                  "title": "Senior React Developer",
                  "employer": "Example Company",
                  "workplace": [{"city": "Warszawa"}],
                  "positionLevels": [{"value": "senior"}],
                  "typesOfContracts": [
                    {"id": 0, "salary": null},
                    {
                      "id": 3,
                      "salary": {
                        "from": 17500,
                        "to": 25000,
                        "currencySymbol": "zł",
                        "timeUnit": {"shortForm": "mth.", "longForm": "monthly"},
                        "kindName": "net (+ VAT)"
                      }
                    }
                  ],
                  "technologies": ["React.js"],
                  "salary": {
                    "to": 25000,
                    "currency": "zł",
                    "timeUnit": {"shortForm": "mth.", "longForm": "monthly"}
                  },
                  "workModes": ["zdalna"]
                }
                """;

        TheProtocolOffer source = new ObjectMapper().readValue(sourceJson, TheProtocolOffer.class);

        OfferRequestTO target = TheProtocolItMapper.INSTANCE.map(source);

        assertThat(target.getEmploymentType()).isEqualTo("B2B");
        assertThat(target.getSalaryMin()).isEqualByComparingTo("17500");
        assertThat(target.getSalaryMax()).isEqualByComparingTo("25000");
        assertThat(target.getCurrency()).isEqualTo("PLN");
        assertThat(target.getSalaryUnit()).isEqualTo("MONTH");
        assertThat(target.getSalaryType()).isEqualTo(SalaryType.NET);
    }

    @Test
    void shouldKeepConflictingTheProtocolContractSalaryOutOfAnalytics() throws Exception {
        String sourceJson = """
                {
                  "typesOfContracts": [
                    {
                      "id": 0,
                      "salary": {
                        "from": 11000,
                        "to": 16500,
                        "currencySymbol": "zł",
                        "timeUnit": {"shortForm": "mth."},
                        "kindName": "gross"
                      }
                    },
                    {
                      "id": 3,
                      "salary": {
                        "from": 17500,
                        "to": 25000,
                        "currencySymbol": "zł",
                        "timeUnit": {"shortForm": "mth."},
                        "kindName": "net (+ VAT)"
                      }
                    }
                  ],
                  "salary": {
                    "to": 25000,
                    "currency": "zł",
                    "timeUnit": {"shortForm": "mth."}
                  }
                }
                """;
        TheProtocolOffer source = new ObjectMapper().readValue(sourceJson, TheProtocolOffer.class);
        OfferRequestTO target = new OfferRequestTO();
        target.setEmploymentType("EMPLOYMENT");

        TheProtocolItMapper.INSTANCE.determineSalaryParameters(source, target);

        assertThat(target.getEmploymentType()).isNull();
        assertThat(target.getSalaryMin()).isNull();
        assertThat(target.getSalaryMax()).isEqualByComparingTo("25000");
        assertThat(target.getCurrency()).isEqualTo("PLN");
        assertThat(target.getSalaryUnit()).isEqualTo("MONTH");
        assertThat(target.getSalaryType()).isEqualTo(SalaryType.UNKNOWN);
    }
}
