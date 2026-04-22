package pl.easyoffer.offer_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;
import pl.easyoffer.offer_service.service.JobOfferService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final JobOfferService jobOfferService;

    @GetMapping
    public Page<JobOfferResponseTO> getOffers(@PageableDefault(size = 20) Pageable pageable) {
        return jobOfferService.getOffers(pageable);
    }

    @GetMapping("/{id}")
    public JobOfferResponseTO getOfferById(@PathVariable Long id) {
        return jobOfferService.getById(id);
    }

    @GetMapping("/search")
    public Page<JobOfferResponseTO> searchOffers(
            @RequestParam(required = false) String technology,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return jobOfferService.search(technology, location, experienceLevel, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobOfferResponseTO createOffer(@Valid @RequestBody JobOfferRequestTO request) {
        return jobOfferService.createOrUpdate(request);
    }

}
