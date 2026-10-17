package pl.easyoffer.offer_service.controller;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.config.DatasetExportProperties;
import pl.easyoffer.offer_service.exception.DatasetExportForbiddenException;
import pl.easyoffer.offer_service.model.to.DatasetExportRequestTO;
import pl.easyoffer.offer_service.model.to.DatasetManifestTO;
import pl.easyoffer.offer_service.service.dataset.DatasetExportPropertiesValidator;
import pl.easyoffer.offer_service.service.dataset.DatasetExportService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/v1.0/admin/datasets")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dataset.export", name = "enabled", havingValue = "true")
public class DatasetExportController {

    public static final String EXPORT_KEY_HEADER = "X-Dataset-Export-Key";

    private final DatasetExportService datasetExportService;
    private final DatasetExportProperties properties;
    private final DatasetExportPropertiesValidator datasetExportPropertiesValidator;

    @PostConstruct
    void validateConfiguration() {
        datasetExportPropertiesValidator.validate(properties.getApiKey());
    }

    @PostMapping
    public ResponseEntity<DatasetManifestTO> export(
            @RequestHeader(value = EXPORT_KEY_HEADER, required = false) String providedApiKey,
            @Valid @RequestBody DatasetExportRequestTO request
    ) {
        authorize(providedApiKey);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(datasetExportService.export(request));
    }

    private void authorize(String providedApiKey) {
        byte[] expected = properties.getApiKey().getBytes(StandardCharsets.UTF_8);
        byte[] provided = providedApiKey == null
                ? new byte[0]
                : providedApiKey.getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, provided)) {
            throw new DatasetExportForbiddenException();
        }
    }
}
