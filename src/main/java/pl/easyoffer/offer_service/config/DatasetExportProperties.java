package pl.easyoffer.offer_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "dataset.export")
public class DatasetExportProperties {

    private Path directory = Path.of("data", "datasets");
    private String apiKey;

}
