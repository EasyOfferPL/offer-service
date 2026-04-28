package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClient;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolCategoryType;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolOffer;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TheProtocolItSynchronizer implements OfferSynchronizer {

    private final TheProtocolClient theProtocolClient;

    @Override
    public void synchronize() {
        log.info("TheProtocolIt synchronizer - start");
        List<TheProtocolOffer> offersJson = fetchOffers(Arrays.stream(TheProtocolCategoryType.values())
                .map(TheProtocolCategoryType::getCategoryName)
                .toList());
        saveAll(offersJson);
        log.info("TheProtocolIt synchronizer - end size: {}", offersJson.size());
    }

    private List<TheProtocolOffer> fetchOffers(List<String> categories) {
        return theProtocolClient.getAllOffers(categories);
    }

    private void saveAll(List<TheProtocolOffer> offers) {
        //todo
    }

}