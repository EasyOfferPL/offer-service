package pl.easyoffer.offer_service.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItOffer;

import static pl.easyoffer.offer_service.model.dto.justjoinit.CurrencySourceType.ORIGINAL;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JustJoinItOfferMapper {

    JustJoinItOfferMapper INSTANCE = Mappers.getMapper(JustJoinItOfferMapper.class);

    @Mapping(source = "guid", target = "externalId")
    @Mapping(source = "workplaceType", target = "workMode", qualifiedByName = "toUpperCase")
    @Mapping(source = "category.key", target = "category", qualifiedByName = "toUpperCase")
    @Mapping(source = "experienceLevel", target = "experienceLevel", qualifiedByName = "toUpperCase")
    @Mapping(source = "workingTime", target = "workingTime", qualifiedByName = "toUpperCase")
    @Mapping(source = "city", target = "location", qualifiedByName = "toUpperCase")
    @Mapping(source = "isOpenToHireUkrainians", target = "openToHireUkrainians")
    JobOfferRequestTO map(JustJoinItOffer src);

    @Named("toUpperCase")
    default String toUpperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    @AfterMapping
    default void determineSalaryParameters(JustJoinItOffer src, @MappingTarget JobOfferRequestTO target) {
        src.getEmploymentTypes().stream()
                .filter(employmentType ->
                        ORIGINAL.name().equalsIgnoreCase(employmentType.getCurrencySource()))
                .findFirst()
                .ifPresent(originalEmploymentType -> {
                    target.setCurrency(originalEmploymentType.getCurrency());
                    target.setSalaryMin(originalEmploymentType.getFromPerUnit());
                    target.setSalaryMax(originalEmploymentType.getToPerUnit());
                    target.setEmploymentType(originalEmploymentType.getType());
                    target.setSalaryUnit(originalEmploymentType.getUnit().toUpperCase());
                });
    }

}
