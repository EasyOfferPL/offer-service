package pl.easyoffer.offer_service.model.dto;

import java.util.List;

public record OfferStatsResponseTO(long totalOffers, List<TopTechnologyTO> topTechnologies) {
}
