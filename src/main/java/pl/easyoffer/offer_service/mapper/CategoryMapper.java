package pl.easyoffer.offer_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.to.CategoryTO;
import pl.easyoffer.offer_service.repository.projection.CategoryProjection;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    List<CategoryTO> map(List<CategoryProjection> categoryProjections);

    CategoryTO map(CategoryProjection categoryProjection);

}
