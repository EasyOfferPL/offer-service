package pl.easyoffer.offer_service.service.synchronizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.easyoffer.offer_service.client.justjoinit.JustJoinItClient;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOffer;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOfferData;
import pl.easyoffer.offer_service.service.OfferService;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JustJoinItOfferSynchronizerTest {

    @Mock
    private OfferService offerService;

    @Mock
    private JustJoinItClient justJoinItClient;

    @Test
    void shouldBuildSynchronizationCountsFromPersistenceOutcomes() {
        JustJoinItOfferSynchronizer synchronizer = new JustJoinItOfferSynchronizer(offerService, justJoinItClient);
        ReflectionTestUtils.setField(synchronizer, "sourceUrl", "https://justjoin.it/");
        JustJoinItOfferData offers = offerData(List.of(offer("created"), offer("updated")));
        JustJoinItOfferData noOffers = offerData(List.of());
        when(justJoinItClient.getOffers(anyString(), anyInt(), anyInt()))
                .thenReturn(offers, noOffers);
        when(offerService.createOrUpdateForSynchronization(any()))
                .thenReturn(OfferPersistenceOutcome.CREATED, OfferPersistenceOutcome.UPDATED);

        SynchronizationResult result = synchronizer.synchronize();

        assertThat(result).isEqualTo(new SynchronizationResult(2, 1, 1, 0));
        ArgumentCaptor<OfferRequestTO> requestCaptor = ArgumentCaptor.forClass(OfferRequestTO.class);
        verify(offerService, times(2)).createOrUpdateForSynchronization(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .extracting(OfferRequestTO::getSource)
                .containsOnly(JobOfferSourceType.JUST_JOIN_IT.name());
    }

    private static JustJoinItOfferData offerData(List<JustJoinItOffer> offers) {
        JustJoinItOfferData data = new JustJoinItOfferData();
        data.setData(offers);
        return data;
    }

    private static JustJoinItOffer offer(String id) {
        JustJoinItOffer offer = new JustJoinItOffer();
        offer.setGuid(id);
        offer.setSlug(id);
        offer.setTitle("Java Developer");
        offer.setCompanyName("Acme");
        offer.setCity("Warsaw");
        offer.setRequiredSkills(List.of());
        offer.setNiceToHaveSkills(List.of());
        offer.setLanguages(List.of());
        offer.setEmploymentTypes(List.of());
        return offer;
    }
}
