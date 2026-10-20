package pl.easyoffer.offer_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.exception.ErrorResponse;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.model.to.PageResponseTO;
import pl.easyoffer.offer_service.service.OfferService;

import static pl.easyoffer.offer_service.config.SwaggerDescription.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
@Tag(name = OFFERS_TAG, description = OFFERS_TAG_DESCRIPTION)
public class OfferController {

    private final OfferService offerService;

    @GetMapping
    @Operation(summary = LIST_OFFERS_SUMMARY, description = LIST_OFFERS_DESCRIPTION)
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_OK, description = PAGE_RETURNED),
            @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = INVALID_PAGE_OR_SORT,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponseTO<OfferResponseTO> getOffers(
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return PageResponseTO.from(offerService.getOffers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = GET_OFFER_SUMMARY)
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_OK, description = OFFER_RETURNED),
            @ApiResponse(responseCode = RESPONSE_NOT_FOUND, description = OFFER_NOT_FOUND,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OfferResponseTO getOfferById(
            @Parameter(description = OFFER_ID_DESCRIPTION, example = EXAMPLE_OFFER_ID)
            @PathVariable Long id
    ) {
        return offerService.getById(id);
    }

    @GetMapping("/search")
    @Operation(
            summary = SEARCH_OFFERS_SUMMARY,
            description = SEARCH_OFFERS_DESCRIPTION
    )
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_OK, description = FILTERED_PAGE_RETURNED),
            @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = INVALID_FILTER_PAGE_OR_SORT,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PageResponseTO<OfferResponseTO> searchOffers(
            @ParameterObject OfferSearchRequest offerSearchRequest,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return PageResponseTO.from(offerService.search(offerSearchRequest, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = CREATE_OFFER_SUMMARY,
            description = CREATE_OFFER_DESCRIPTION
    )
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_CREATED, description = OFFER_PERSISTED),
            @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = OFFER_VALIDATION_FAILURE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = UNEXPECTED_SERVER_ERROR,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public OfferResponseTO createOffer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = OFFER_REQUEST_DESCRIPTION
            )
            @Valid @RequestBody OfferRequestTO request
    ) {
        return offerService.createOrUpdate(request);
    }

}
