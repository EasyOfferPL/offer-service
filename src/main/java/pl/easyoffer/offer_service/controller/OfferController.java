package pl.easyoffer.offer_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.service.OfferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    public Page<OfferResponseTO> getOffers(@PageableDefault(size = 20) Pageable pageable) {
        return offerService.getOffers(pageable);
    }

    @GetMapping("/{id}")
    public OfferResponseTO getOfferById(@PathVariable Long id) {
        return offerService.getById(id);
    }

    @GetMapping("/search")
    public Page<OfferResponseTO> searchOffers(
            @RequestParam(required = false) String technology,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return offerService.search(technology, location, experienceLevel, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OfferResponseTO createOffer(@Valid @RequestBody OfferRequestTO request) {
        return offerService.createOrUpdate(request);
    }

}
