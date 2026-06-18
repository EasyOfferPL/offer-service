package pl.easyoffer.offer_service.model.to.theprotocolit;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TheProtocolOfferData {

    private List<TheProtocolOffer> offers = new ArrayList<>();

}
