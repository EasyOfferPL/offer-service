package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.justjoinit.JustJoinItClient;
import pl.easyoffer.offer_service.mapper.JustJoinItOfferMapper;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItCategoryType;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOffer;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOfferData;
import pl.easyoffer.offer_service.service.OfferService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.just-join-it-synchronizer-enabled", havingValue = "true")
public class JustJoinItOfferSynchronizer implements OfferSynchronizer {

    private static final String JOB_OFFER_PATH = "job-offer/";

    private final OfferService offerService;
    private final JustJoinItClient justJoinItClient;

    @Value("${service.justJoinIt-service}")
    private String sourceUrl;

    @Override
    public void synchronize() {
        List<JustJoinItOffer> offers = Arrays.stream(JustJoinItCategoryType.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(this::fetchOffers)
                .flatMap(Collection::stream)
                .toList();
        save(offers);
    }

    private List<JustJoinItOffer> fetchOffers(String category) {
        int limit = 100;
        int offset = 0;
        List<JustJoinItOffer> offers = new ArrayList<>();
        JustJoinItOfferData offerData;
        do {
            offerData = justJoinItClient.getOffers(category, offset, limit);
            offers.addAll(offerData.getData());
            offset += limit;
        } while (offerData.getData().size() == limit);
        return offers;
    }

    private void save(List<JustJoinItOffer> offers) {
        offers.stream()
                .map(offer -> {
                    OfferRequestTO mappedOffer = JustJoinItOfferMapper.INSTANCE.map(offer);
                    mappedOffer.setUrl(sourceUrl + JOB_OFFER_PATH + offer.getSlug());
                    mappedOffer.setSource(JobOfferSourceType.JUST_JOIN_IT.name());
                    return mappedOffer;
                })
                .forEach(offerService::createOrUpdate);
    }

}
