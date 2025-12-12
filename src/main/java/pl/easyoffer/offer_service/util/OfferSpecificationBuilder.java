package pl.easyoffer.offer_service.util;

import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import pl.easyoffer.offer_service.model.OfferEntity;

import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public class OfferSpecificationBuilder {

    private Specification<OfferEntity> spec;

    public static OfferSpecificationBuilder builder() {
        return new OfferSpecificationBuilder();
    }

    public OfferSpecificationBuilder withIds(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            Specification<OfferEntity> idSpec = (root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.id.toUpperCase()).in(ids);
            spec = Objects.nonNull(spec) ? spec.and(idSpec) : idSpec;
        }
        return this;
    }

    public Specification<OfferEntity> build() {
        return spec;
    }

}
