package pl.easyoffer.offer_service.model.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DatasetExportRequestTO(

        @NotBlank
        @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9._-]{2,63}")
        String datasetId,

        @NotBlank
        @Pattern(regexp = "[0-9a-fA-F]{7,64}")
        String codeRevision
) {
}
