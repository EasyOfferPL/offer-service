package pl.easyoffer.offer_service.service.synchronizer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.client.nofluffjobs.NofluffjobsClient;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsCategoryType;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsPosting;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsPostingsWrapper;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsSearchCriteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class NofluffjobsSynchronizer implements OfferSynchronizer {

    private final NofluffjobsClient nofluffjobsClient;

    @Override
    public void synchronize() {
        Set<NofluffjobsPosting> postings = fetchPostings();
//        NofluffjobsPostingsWrapper test = nofluffjobsClient.getOffers();
        var te = 1;
//        Set<NofluffjobsOffer> offers = fetchOffers()
    }

    private Set<NofluffjobsPosting> fetchPostings() {
        return Arrays.stream(NofluffjobsCategoryType.values())
                .map(NofluffjobsCategoryType::getTechnologyName)
                .map(this::fetchPostingsForCategory)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<NofluffjobsPosting> fetchPostingsForCategory(String categoryName) {
        int page = 1;
        int totalPages = 0;
        Set<NofluffjobsPosting> postings = new HashSet<>();
        do {
            NofluffjobsPostingsWrapper postingsWrapper =
                    nofluffjobsClient.getOffers(page, buildSearchCriteria(categoryName));
            postings.addAll(postingsWrapper.getPostings());
            totalPages = totalPages == 0 ? postingsWrapper.getTotalPages() : totalPages;
            page++;
        }
        while (page < totalPages);
        return postings;
    }

    private static NofluffjobsSearchCriteria buildSearchCriteria(String technology) {
        return NofluffjobsSearchCriteria.builder()
                .url(NofluffjobsSearchCriteria.Url.builder()
                        .searchParam(technology)
                        .build())
                .pageSize(20)
                .rawSearch(technology)
                .withSalaryMatch(true)
                .build();
    }

}
