package pl.easyoffer.offer_service.client.theprotocolit;

import pl.easyoffer.offer_service.model.dto.theprotocolit.TheProtocolOffer;

import java.util.List;

public interface TheProtocolClient {

    List<TheProtocolOffer> getAllOffers(List<String> categories);

}
