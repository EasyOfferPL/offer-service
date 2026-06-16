package pl.easyoffer.offer_service.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItLanguage;
import pl.easyoffer.offer_service.model.to.justjoinit.JustJoinItOffer;

import java.util.Comparator;
import java.util.List;

import static pl.easyoffer.offer_service.model.to.justjoinit.CurrencySourceType.ORIGINAL;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = TechnologyMapper.class)
public interface JustJoinItOfferMapper {

    JustJoinItOfferMapper INSTANCE = Mappers.getMapper(JustJoinItOfferMapper.class);

    @Mapping(source = "guid", target = "externalId")
    @Mapping(source = "workplaceType", target = "workMode", qualifiedByName = "toUpperCase")
    @Mapping(source = "category.key", target = "category", qualifiedByName = "toUpperCase")
    @Mapping(source = "experienceLevel", target = "experienceLevel", qualifiedByName = "toUpperCase")
    @Mapping(source = "workingTime", target = "workingTime", qualifiedByName = "toUpperCase")
    @Mapping(source = "city", target = "location", qualifiedByName = "toUpperCase")
    @Mapping(source = "isOpenToHireUkrainians", target = "openToHireUkrainians")
    @Mapping(source = "allSkills", target = "technologies")
    @Mapping(source = "languages", target = "language", qualifiedByName = "mapLanguage")
    OfferRequestTO map(JustJoinItOffer src);

    @Named("toUpperCase")
    default String toUpperCase(String value) {
        return value == null ? null : value.toUpperCase();
    }

    @Named("mapLanguage")
    default String mapLanguage(List<JustJoinItLanguage> languages) {
        return languages.stream()
                .max(Comparator.comparing(JustJoinItLanguage::getLevel))
                .map(JustJoinItLanguage::getCode)
                .map(String::toUpperCase)
                .orElse(null);
    }

    @AfterMapping
    default void determineSalaryParameters(JustJoinItOffer src, @MappingTarget OfferRequestTO target) {
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
