package pl.easyoffer.offer_service.mapper;

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
}
