package pl.easyoffer.offer_service.model.to.nofluffjobs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NofluffjobsCategoryType {
    JAVA("Java"),
    PYTHON("Python"),
    CSHAPR("C#"),
    SQL("SQL"),
    CPP("C++"),
    GOLANG("Golang"),
    JAVASCRIPT("JavaScript"),
    REACT("React"),
    ANGULAR("Angular"),
    TYPESCRIPT("TypeScript"),
    HTML("HTML");

    private final String technologyName;
}
