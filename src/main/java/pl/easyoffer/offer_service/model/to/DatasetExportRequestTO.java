package pl.easyoffer.offer_service.model.to;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static pl.easyoffer.offer_service.config.SwaggerDescription.*;

public record DatasetExportRequestTO(

        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{2,63}")
        @Schema(description = DATASET_ID_DESCRIPTION, example = EXAMPLE_DATASET_ID)
        String datasetId,

        @NotBlank
        @Pattern(regexp = "[0-9a-fA-F]{7,64}")
        @Schema(description = CODE_REVISION_DESCRIPTION, example = EXAMPLE_CODE_REVISION)
        String codeRevision
) {
}
