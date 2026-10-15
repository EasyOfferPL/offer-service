package pl.easyoffer.offer_service.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SalaryTypeTest {

    @Test
    void shouldMapSourceSalaryTypeWithoutGuessingUnknownValues() {
        assertThat(SalaryType.fromGross(true)).isEqualTo(SalaryType.GROSS);
        assertThat(SalaryType.fromGross(false)).isEqualTo(SalaryType.NET);
        assertThat(SalaryType.fromGross(null)).isEqualTo(SalaryType.UNKNOWN);
        assertThat(SalaryType.fromSourceValue(" brutto ")).isEqualTo(SalaryType.GROSS);
        assertThat(SalaryType.fromSourceValue("NETTO")).isEqualTo(SalaryType.NET);
        assertThat(SalaryType.fromSourceValue("including VAT")).isEqualTo(SalaryType.UNKNOWN);
        assertThat(SalaryType.fromSourceValue(null)).isEqualTo(SalaryType.UNKNOWN);
    }
}
