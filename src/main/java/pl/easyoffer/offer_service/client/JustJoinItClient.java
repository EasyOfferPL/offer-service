package pl.easyoffer.offer_service.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItOfferData;

public interface JustJoinItClient {

    @RequestLine("GET api/candidate-api/offers?from={offset}&itemsCount={limit}&categories={category}&currency=pln&orderBy=descending&sortBy=publishedAt&keywordType=any")
    @Headers("Accept: application/json")
    JustJoinItOfferData getOffers(@Param String category, @Param int offset, @Param int limit);

}
