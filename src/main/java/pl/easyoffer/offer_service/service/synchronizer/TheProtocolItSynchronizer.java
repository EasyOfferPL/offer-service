package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.theprotocolit.TheProtocolClient;
import pl.easyoffer.offer_service.mapper.TheProtocolItMapper;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolCategoryType;
import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolOffer;
import pl.easyoffer.offer_service.service.JobOfferService;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.the-protocol-it-synchronizer-enabled", havingValue = "true")
public class TheProtocolItSynchronizer implements OfferSynchronizer {

    private static final String JOB_OFFER_PATH = "szczegoly/praca/";

    private final TheProtocolClient theProtocolClient;
    private final JobOfferService jobOfferService;

    @Value("${service.theProtocolIt-service}")
    private String sourceUrl;

    @Override
    public void synchronize() {
        log.info("TheProtocolIt synchronizer - start");
        List<TheProtocolOffer> offersJson = fetchOffers(Arrays.stream(TheProtocolCategoryType.values())
                .map(TheProtocolCategoryType::getTechnologyName)
                .toList());
        saveAll(offersJson);
        log.info("TheProtocolIt synchronizer - end size={}", offersJson.size());
    }

    private List<TheProtocolOffer> fetchOffers(List<String> categories) {
        return theProtocolClient.getAllOffers(categories);
    }

    private void saveAll(List<TheProtocolOffer> offers) {
        offers.stream()
                .map(offer -> {
                    JobOfferRequestTO mappedOffer = TheProtocolItMapper.INSTANCE.map(offer);
                    mappedOffer.setUrl(sourceUrl + JOB_OFFER_PATH + offer.getOfferUrlName());
                    mappedOffer.setSource(JobOfferSourceType.THE_PROTOCOL_IT.name());
                    return mappedOffer;
                })
                .forEach(jobOfferService::createOrUpdate);
    }

}