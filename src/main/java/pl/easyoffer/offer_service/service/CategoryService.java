package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.mapper.CategoryMapper;
import pl.easyoffer.offer_service.model.to.CategoryTO;
import pl.easyoffer.offer_service.repository.projection.CategoryProjection;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final OfferPersistenceService offerPersistenceService;

    public List<CategoryTO> getCategories() {
        List<CategoryProjection> categoryProjections = offerPersistenceService.getCategories();
        return CategoryMapper.INSTANCE.map(categoryProjections);
    }

}
