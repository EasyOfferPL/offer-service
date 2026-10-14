package pl.easyoffer.offer_service.service.synchronizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClient;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolOffer;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolPositionLevel;
import pl.easyoffer.offer_service.model.to.theprotocolit.TheProtocolWorkplace;
import pl.easyoffer.offer_service.service.OfferService;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TheProtocolItSynchronizerTest {

    @Mock
    private TheProtocolClient theProtocolClient;

    @Mock
    private OfferService offerService;

    @Test
    void shouldBuildSynchronizationCountsFromPersistenceOutcomes() {
        TheProtocolItSynchronizer synchronizer = new TheProtocolItSynchronizer(theProtocolClient, offerService);
        ReflectionTestUtils.setField(synchronizer, "sourceUrl", "https://theprotocol.it/");
        when(theProtocolClient.getAllOffers(anyList()))
                .thenReturn(List.of(offer("created"), offer("updated")));
        when(offerService.createOrUpdateForSynchronization(any()))
                .thenReturn(OfferPersistenceOutcome.CREATED, OfferPersistenceOutcome.UPDATED);

        SynchronizationResult result = synchronizer.synchronize();

        assertThat(result).isEqualTo(new SynchronizationResult(2, 1, 1, 0));
        ArgumentCaptor<OfferRequestTO> requestCaptor = ArgumentCaptor.forClass(OfferRequestTO.class);
        verify(offerService, times(2)).createOrUpdateForSynchronization(requestCaptor.capture());
        assertThat(requestCaptor.getAllValues())
                .extracting(OfferRequestTO::getSource)
                .containsOnly(JobOfferSourceType.THE_PROTOCOL_IT.name());
    }

    private static TheProtocolOffer offer(String id) {
        TheProtocolWorkplace workplace = new TheProtocolWorkplace();
        workplace.setCity("Warsaw");
        TheProtocolPositionLevel positionLevel = new TheProtocolPositionLevel();
        positionLevel.setValue("mid");

        TheProtocolOffer offer = new TheProtocolOffer();
        offer.setId(id);
        offer.setOfferUrlName(id);
        offer.setTitle("Java Developer");
        offer.setEmployer("Acme");
        offer.setWorkplace(List.of(workplace));
        offer.setPositionLevels(List.of(positionLevel));
        offer.setWorkModes(List.of("remote"));
        return offer;
    }
}
