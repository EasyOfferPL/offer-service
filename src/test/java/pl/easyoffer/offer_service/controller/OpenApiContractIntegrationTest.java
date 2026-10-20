package pl.easyoffer.offer_service.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.config.SwaggerDescription;

import java.util.Set;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "dataset.export.enabled=true",
        "dataset.export.api-key=0123456789abcdef0123456789abcdef",
        "dataset.export.directory=target/f038-contract-datasets"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractIntegrationTest {

    private static final Set<String> EXPECTED_PATHS = Set.of(
            "/offers",
            "/offers/{id}",
            "/offers/search",
            "/v1.0/categories",
            "/v1.0/analytics",
            "/v1.0/analytics/technologies",
            "/v1.0/analytics/technologies/popularity",
            "/v1.0/analytics/technologies/trend",
            "/v1.0/analytics/salaries",
            "/v1.0/analytics/deduplication-audit",
            "/v1.0/analytics/technology-normalization-audit",
            "/v1.0/analytics/salary-comparability-audit",
            "/v1.0/analytics/data-completeness",
            "/v1.0/analytics/salaries/trend",
            "/v1.0/admin/datasets"
    );

    private static final Set<String> EXPECTED_OPERATIONS = Set.of(
            "GET /offers",
            "POST /offers",
            "GET /offers/{id}",
            "GET /offers/search",
            "GET /v1.0/categories",
            "GET /v1.0/analytics",
            "GET /v1.0/analytics/technologies",
            "GET /v1.0/analytics/technologies/popularity",
            "GET /v1.0/analytics/technologies/trend",
            "GET /v1.0/analytics/salaries",
            "GET /v1.0/analytics/deduplication-audit",
            "GET /v1.0/analytics/technology-normalization-audit",
            "GET /v1.0/analytics/salary-comparability-audit",
            "GET /v1.0/analytics/data-completeness",
            "GET /v1.0/analytics/salaries/trend",
            "POST /v1.0/admin/datasets"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldPublishCompleteVersionedContract() throws Exception {
        String response = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode contract = objectMapper.readTree(response);
        assertThat(contract.path("info").path("version").asText())
                .isEqualTo(SwaggerDescription.API_VERSION);
        assertThat(contract.path("info").has("x-code-revision")).isFalse();
        assertThat(iterableFieldNames(contract.path("paths")))
                .containsExactlyInAnyOrderElementsOf(EXPECTED_PATHS);
        assertThat(operations(contract.path("paths")))
                .containsExactlyInAnyOrderElementsOf(EXPECTED_OPERATIONS);

        JsonNode securityScheme = contract.path("components")
                .path("securitySchemes")
                .path("datasetExportApiKey");
        assertThat(securityScheme.path("type").asText()).isEqualTo("apiKey");
        assertThat(securityScheme.path("in").asText()).isEqualTo("header");
        assertThat(securityScheme.path("name").asText())
                .isEqualTo(DatasetExportController.EXPORT_KEY_HEADER);

        JsonNode datasetExport = contract.path("paths")
                .path("/v1.0/admin/datasets")
                .path("post");
        assertThat(datasetExport.path("security").toString())
                .contains("datasetExportApiKey");
        assertThat(datasetExport.path("responses").path("403").toString())
                .contains("ErrorResponse");
        assertThat(datasetExport.toString())
                .doesNotContain("0123456789abcdef0123456789abcdef");

        JsonNode dateFrom = parameter(
                contract.path("paths")
                        .path("/v1.0/analytics/technologies/popularity")
                        .path("get"),
                "dateFrom"
        );
        assertThat(dateFrom.path("schema").path("type").asText()).isEqualTo("string");
        assertThat(dateFrom.path("schema").path("format").asText()).isEqualTo("date");
        assertThat(dateFrom.path("example").asText()).isEqualTo("2026-10-01");

        JsonNode pageSchema = StreamSupport.stream(
                        contract.path("components").path("schemas").spliterator(), false
                )
                .filter(schema -> schema.path("properties").has("totalElements"))
                .filter(schema -> schema.path("properties").has("content"))
                .findFirst()
                .orElseThrow();
        assertThat(iterableFieldNames(pageSchema.path("properties")))
                .containsExactlyInAnyOrder(
                        "content", "page", "size", "totalElements", "totalPages", "first", "last"
                );
    }

    @Test
    void shouldReturnStableBoundedOfferPage() throws Exception {
        mockMvc.perform(get("/offers").param("size", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(200))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.totalPages").isNumber())
                .andExpect(jsonPath("$.first").isBoolean())
                .andExpect(jsonPath("$.last").isBoolean())
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist())
                .andExpect(jsonPath("$.number").doesNotExist());
    }

    private static JsonNode parameter(JsonNode operation, String name) {
        return StreamSupport.stream(operation.path("parameters").spliterator(), false)
                .filter(parameter -> name.equals(parameter.path("name").asText()))
                .findFirst()
                .orElseThrow();
    }

    private static Iterable<String> iterableFieldNames(JsonNode node) {
        return node::fieldNames;
    }

    private static Set<String> operations(JsonNode paths) {
        Set<String> operations = new java.util.HashSet<>();
        paths.fields().forEachRemaining(path ->
                path.getValue().fieldNames().forEachRemaining(method ->
                        operations.add(method.toUpperCase() + " " + path.getKey())
                )
        );
        return operations;
    }
}
