package pl.easyoffer.offer_service.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.easyoffer.offer_service.controller.DatasetExportController;

@Configuration
public class OpenApiConfiguration {

    public static final String DATASET_EXPORT_API_KEY_SCHEME = "datasetExportApiKey";

    @Bean
    OpenAPI easyOfferOpenApi() {
        Info apiInfo = new Info()
                .title(SwaggerDescription.API_TITLE)
                .version(SwaggerDescription.API_VERSION)
                .description(SwaggerDescription.API_DESCRIPTION);
        return new OpenAPI()
                .info(apiInfo)
                .components(new Components().addSecuritySchemes(
                        DATASET_EXPORT_API_KEY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(DatasetExportController.EXPORT_KEY_HEADER)
                                .description(SwaggerDescription.DATASET_EXPORT_KEY_DESCRIPTION)
                ));
    }
}
