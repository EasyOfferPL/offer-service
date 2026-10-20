package pl.easyoffer.offer_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.easyoffer.offer_service.config.DatasetExportProperties;
import pl.easyoffer.offer_service.config.OpenApiConfiguration;
import pl.easyoffer.offer_service.exception.DatasetExportForbiddenException;
import pl.easyoffer.offer_service.exception.ErrorResponse;
import pl.easyoffer.offer_service.model.to.DatasetExportRequestTO;
import pl.easyoffer.offer_service.model.to.DatasetManifestTO;
import pl.easyoffer.offer_service.service.dataset.DatasetExportPropertiesValidator;
import pl.easyoffer.offer_service.service.dataset.DatasetExportService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static pl.easyoffer.offer_service.config.SwaggerDescription.*;

@RestController
@RequestMapping("/v1.0/admin/datasets")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "dataset.export", name = "enabled", havingValue = "true")
@Tag(name = DATASET_TAG, description = DATASET_TAG_DESCRIPTION)
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
    @Operation(
            summary = DATASET_EXPORT_SUMMARY,
            description = DATASET_EXPORT_DESCRIPTION,
            security = @SecurityRequirement(name = OpenApiConfiguration.DATASET_EXPORT_API_KEY_SCHEME)
    )
    @ApiResponses({
            @ApiResponse(responseCode = RESPONSE_CREATED, description = DATASET_CREATED),
            @ApiResponse(responseCode = RESPONSE_BAD_REQUEST, description = INVALID_DATASET_REQUEST,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_FORBIDDEN, description = INVALID_DATASET_KEY,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_CONFLICT, description = DATASET_CONFLICT,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = RESPONSE_INTERNAL_SERVER_ERROR, description = DATASET_EXPORT_FAILURE,
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
