package pl.easyoffer.offer_service.service.dataset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pl.easyoffer.offer_service.controller.DatasetExportController;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "dataset.export.enabled=true",
        "dataset.export.api-key=0123456789abcdef0123456789abcdef",
        "dataset.export.directory=target/f015-controller-datasets"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DatasetExportControllerTest {

    private static final Path EXPORT_ROOT = Path.of("target", "f015-controller-datasets")
            .toAbsolutePath();
    private static final String REQUEST = """
            {
              "datasetId": "DATASET-API-001",
              "codeRevision": "37a1345f5fed745b7135b66d2a35331ab126ebbd"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private SynchronizationRunRepository synchronizationRunRepository;

    @BeforeEach
    void prepare() throws IOException {
        cleanDatabase();
        deleteDirectory(EXPORT_ROOT);
    }

    @AfterEach
    void cleanUp() throws IOException {
        cleanDatabase();
        deleteDirectory(EXPORT_ROOT);
    }

    @Test
    void shouldRejectInvalidExportKey() throws Exception {
        mockMvc.perform(post("/v1.0/admin/datasets")
                        .header(DatasetExportController.EXPORT_KEY_HEADER, "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Dataset export is not authorized"));
    }

    @Test
    void shouldCreateSnapshotForAuthorizedRequestAndRejectOverwrite() throws Exception {
        mockMvc.perform(post("/v1.0/admin/datasets")
                        .header(
                                DatasetExportController.EXPORT_KEY_HEADER,
                                "0123456789abcdef0123456789abcdef"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.datasetId").value("DATASET-API-001"))
                .andExpect(jsonPath("$.schemaVersion").value("8"))
                .andExpect(jsonPath("$.recordCounts.offers").value(0));

        mockMvc.perform(post("/v1.0/admin/datasets")
                        .header(
                                DatasetExportController.EXPORT_KEY_HEADER,
                                "0123456789abcdef0123456789abcdef"
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST))
                .andExpect(status().isConflict());
    }

    private void cleanDatabase() {
        offerRepository.deleteAll();
        technologyRepository.deleteAll();
        synchronizationRunRepository.deleteAll();
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
