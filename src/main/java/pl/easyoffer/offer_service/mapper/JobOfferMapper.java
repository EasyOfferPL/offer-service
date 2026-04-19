package pl.easyoffer.offer_service.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItOffer;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobOfferMapper {

    JobOfferMapper INSTANCE = Mappers.getMapper(JobOfferMapper.class);

    @Mapping(target = "technologies", expression = "java(mapTechnologies(entity.getTechnologies()))")
    JobOfferResponseTO toResponse(JobOfferEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "technologies", ignore = true)
    @Mapping(source = "isOpenToHireUkrainians", target = "isOpenToHireUkrainians")
    void updateEntity(JobOfferRequestTO src, @MappingTarget JobOfferEntity dst);

    @Mapping(source = "guid", target = "externalId")
    @Mapping(source = "workplaceType", target = "workMode")
    @Mapping(source = "category.key", target = "category")
    @Mapping(source = "city", target = "location")
    JobOfferRequestTO map(JustJoinItOffer src);

    default List<String> mapTechnologies(Set<TechnologyEntity> technologies) {
        return technologies.stream()
                .map(TechnologyEntity::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
