package pl.easyoffer.offer_service.mapper;

import com.easyoffer.offer_client.to.OfferTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.OfferEntity;

import java.util.List;

@Mapper
public interface OfferMapper {

    OfferMapper INSTANCE = Mappers.getMapper(OfferMapper.class);

    OfferEntity map(OfferTO offerTO);
    OfferTO map(OfferEntity offerEntity);
    List<OfferTO> map(List<OfferEntity> offerEntity);
    void update(OfferTO source, @MappingTarget OfferEntity target);

}
