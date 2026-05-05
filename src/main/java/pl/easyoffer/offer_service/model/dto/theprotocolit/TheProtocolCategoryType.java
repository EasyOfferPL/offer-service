package pl.easyoffer.offer_service.model.dto.theprotocolit;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TheProtocolCategoryType {
    JAVA("Java"),
    DOCKER("Docker", "DEVOPS"),
    C("C"),
    CSharp("C#"),
    Cpp("C++"),
    AWS("AWS", "DEVOPS"),
    ANGULAR("Angular"),
    ANDROID("Android", "MOBILE"),
    DOTNET(".NET"),
    GO("Go"),
    IOS("iOS", "MOBILE"),
    JAVASCRIPT("JavaScript"),
    KUBERNETES("Kubernetes", "DEVOPS"),
    AZURE("Microsoft Azure", "DEVOPS"),
    PHP("PHP"),
    PYTHON("Python"),
    REACT("React.js"),
    TYPESCRIPT("TypeScript");

    private final String technologyName;
    private String categoryName;

    TheProtocolCategoryType(String technologyName) {
        this.technologyName = technologyName;
    }

    TheProtocolCategoryType(String technologyName, String categoryName) {
        this.technologyName = technologyName;
        this.categoryName = categoryName;
    }

    public static TheProtocolCategoryType getValueByTechnologyName(String technologyName) {
        return Arrays.stream(TheProtocolCategoryType.values())
                .filter(t -> t.getTechnologyName().equalsIgnoreCase(technologyName))
                .findFirst()
                .orElse(null);
    }
}
