package pl.easyoffer.offer_service.model.dto.theprotocolit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TheProtocolCategoryType {
    JAVA("Java"),
    DOCKER("Docker"),
    C("C"),
    CSharp("C#"),
    Cpp("C++"),
    AWS("AWS"),
    ANGULAR("Angular"),
    ANDROID("Android"),
    DOTNET(".NET"),
    GO("Go"),
    IOS("iOS"),
    JAVASCRIPT("JavaScript"),
    KUBERNETES("Kubernetes"),
    AZURE("Microsoft Azure"),
    PHP("PHP"),
    PYTHON("Python"),
    REACT("React.js"),
    TYPESCRIPT("TypeScript");

    private final String categoryName;

}
