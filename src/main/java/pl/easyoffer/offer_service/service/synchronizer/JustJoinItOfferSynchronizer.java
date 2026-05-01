package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.justjoinit.JustJoinItClient;
import pl.easyoffer.offer_service.mapper.JustJoinItOfferMapper;
import pl.easyoffer.offer_service.mapper.TechnologyMapper;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItCategoryType;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItLanguage;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItOffer;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItOfferData;
import pl.easyoffer.offer_service.service.JobOfferService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.just-join-it-synchronizer-enabled", havingValue = "true")
public class JustJoinItOfferSynchronizer implements OfferSynchronizer {

    private static final String JOB_OFFER_PATH = "job-offer/";

    private final JobOfferService jobOfferService;
    private final JustJoinItClient justJoinItClient;

    @Value("${service.justJoinIt-service}")
    private String sourceUrl;

    @Override
    public void synchronize() {
        log.info("JustJoinIt synchronizer - start");
        List<JustJoinItOffer> offers = Arrays.stream(JustJoinItCategoryType.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(this::fetchOffers)
                .flatMap(Collection::stream)
                .toList();
        save(offers);
        log.info("JustJoinIt synchronizer - end");
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
                    JobOfferRequestTO mappedOffer = JustJoinItOfferMapper.INSTANCE.map(offer);
                    //todo category???
                    mappedOffer.setUrl(sourceUrl + JOB_OFFER_PATH + offer.getSlug());
                    mappedOffer.setTechnologies(TechnologyMapper.INSTANCE.map(offer.getAllSkills()));
                    mappedOffer.setLanguage(offer.getLanguages().stream()
                            .max(Comparator.comparing(JustJoinItLanguage::getLevel))
                            .map(JustJoinItLanguage::getCode)
                            .orElse(null)
                    );
                    mappedOffer.setSource(JobOfferSourceType.JUST_JOIN_IT.getName());
                    return mappedOffer;
                })
                .forEach(jobOfferService::createOrUpdate);
    }

}
