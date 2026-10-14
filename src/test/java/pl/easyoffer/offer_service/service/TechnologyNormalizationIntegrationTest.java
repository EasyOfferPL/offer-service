package pl.easyoffer.offer_service.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.model.to.TechnologyTO;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TechnologyNormalizationIntegrationTest {

    @Autowired
    private OfferService offerService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void cleanDatabase() {
        offerRepository.deleteAll();
        technologyRepository.deleteAll();
    }

    @Test
    void shouldPreserveSourceValuesAndUseCanonicalNamesForResponseSearchAndAudit() throws Exception {
        OfferResponseTO saved = offerService.createOrUpdate(request(List.of(
                new TechnologyTO(".Net", null),
                new TechnologyTO(".NET", null),
                new TechnologyTO("  Domain   Specific  ", null),
                new TechnologyTO(" ", null)
        )));

        assertThat(saved.getTechnologies()).containsExactly(".NET", "Domain Specific");
        assertThat(offerService.search(OfferSearchRequest.builder()
                .technologies(List.of("dotnet"))
                .build())).extracting(OfferResponseTO::getId).containsExactly(saved.getId());

        List<TechnologyEntity> technologies = technologyRepository.findAll();
        assertThat(technologies).hasSize(3);
        assertThat(technologies)
                .filteredOn(technology -> technology.getName().equals(".Net"))
                .singleElement()
                .satisfies(technology -> {
                    assertThat(technology.getCanonicalName()).isEqualTo(".NET");
                    assertThat(technology.getNormalizationVersion()).isEqualTo("v1");
                });
        assertThat(technologies)
                .filteredOn(technology -> technology.getName().equals("Domain Specific"))
                .singleElement()
                .extracting(TechnologyEntity::getCanonicalName)
                .isEqualTo("Domain Specific");

        mockMvc.perform(get("/v1.0/analytics/technology-normalization-audit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.normalizationVersion").value("v1"))
                .andExpect(jsonPath("$.technologyValueCount").value(3))
                .andExpect(jsonPath("$.changedTechnologyValueCount").value(1))
                .andExpect(jsonPath("$.offerTechnologyAssociationCount").value(3))
                .andExpect(jsonPath("$.changedOfferTechnologyAssociationCount").value(1));
    }

    private static OfferRequestTO request(List<TechnologyTO> technologies) {
        OfferRequestTO request = new OfferRequestTO();
        request.setSource(JobOfferSourceType.JUST_JOIN_IT.name());
        request.setExternalId("technology-normalization-offer");
        request.setTitle("Backend Developer");
        request.setCompanyName("Example Company");
        request.setLocation("Warsaw");
        request.setTechnologies(technologies);
        return request;
    }
}
