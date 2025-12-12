package pl.easyoffer.offer_service.controller;

import com.easyoffer.offer_client.to.OfferSearchRequest;
import com.easyoffer.offer_client.to.OfferTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.service.OfferService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1.0/offers")
public class OfferController {

    private final OfferService offerService;

    @GetMapping("/search")
    public List<OfferTO> search(@ModelAttribute OfferSearchRequest offerSearchRequest) {
        return offerService.searchOffers(offerSearchRequest);
    }

    @GetMapping
    public List<OfferTO> getAll() {
        return offerService.getAll();
    }

    @GetMapping("/{offerId}")
    public OfferTO getById(@PathVariable Long offerId) {
        return offerService.getById(offerId);
    }

    @PostMapping
    public OfferTO create(@RequestBody OfferTO offerTO) {
         return offerService.save(offerTO);
    }

    @PutMapping("/{offerId}")
    public OfferTO update(@PathVariable Long offerId, @RequestBody OfferTO offerTO) {
        return offerService.update(offerTO);
    }

}
