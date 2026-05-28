package pl.easyoffer.offer_service.client.nofluffjobs;

import feign.Body;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsPostingsWrapper;
import pl.easyoffer.offer_service.model.dto.nofluffjobs.NofluffjobsSearchCriteria;

public interface NofluffjobsClient {

    @RequestLine("POST  api/search/posting?pageTo={page}&pageSize=200&salaryCurrency=PLN&salaryPeriod=month")
    @Body("nofluffjobsSearchCriteria")
    @Headers("Content-Type: application/infiniteSearch+json")
    NofluffjobsPostingsWrapper getOffers(@Param int page, NofluffjobsSearchCriteria nofluffjobsSearchCriteria);

    @RequestLine("GET api/posting")
    @Headers("Content-Type: application/json")
    NofluffjobsPostingsWrapper getOffers();

}
