package pl.easyoffer.offer_service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class OfferServiceApplicationTests {

	@Autowired
	private Flyway flyway;

	@Test
	void contextLoadsWithMigratedSchema() {
		assertThat(flyway.info().current())
				.isNotNull()
				.extracting(migrationInfo -> migrationInfo.getVersion().getVersion())
				.isEqualTo("1");
	}

}
