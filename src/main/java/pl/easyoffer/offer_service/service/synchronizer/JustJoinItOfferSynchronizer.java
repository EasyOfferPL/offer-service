package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.JustJoinItClient;
import pl.easyoffer.offer_service.mapper.JobOfferMapper;
import pl.easyoffer.offer_service.mapper.TechnologyMapper;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.justjoinit.*;
import pl.easyoffer.offer_service.service.JobOfferService;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JustJoinItOfferSynchronizer implements OfferSynchronizer {

    private static final char PATH_DELIMITER = '/';
    private static final String JOB_OFFER_PATH = "job-offer";

    private final JobOfferService jobOfferService;
    private final JustJoinItClient justJoinItClient;

    @Value("${service.justJoinIt-service}")
    private String sourceUrl;

    @Override
    public void synchronize() {
        log.info("JustJoinIt synchronizer - start");
        List<JustJoinItOffer> offers = Arrays.stream(CategoryType.values())
                .map(Enum::name)
                .map(String::toLowerCase)
                .map(this::scrapOffers)
                .flatMap(Collection::stream)
                .toList();
        save(offers);
        log.info("JustJoinIt synchronizer - end");
    }

    private List<JustJoinItOffer> scrapOffers(String category) {
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
                    JobOfferRequestTO mappedOffer = JobOfferMapper.INSTANCE.map(offer);
                    mappedOffer.setUrl(sourceUrl + JOB_OFFER_PATH + PATH_DELIMITER + offer.getSlug());
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
