package pl.easyoffer.offer_service.service.dataset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class DatasetExportPropertiesValidator {

    private static final String ERROR_MESSAGE = "Dataset export API key must contain at least 32 characters";
    private static final int MINIMUM_API_KEY_LENGTH = 32;

    public void validate(String apiKey) {
        if (!StringUtils.hasText(apiKey) || apiKey.length() < MINIMUM_API_KEY_LENGTH) {
            throw new IllegalStateException(ERROR_MESSAGE);
        }
    }

}
