package pl.easyoffer.offer_service.service.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pl.easyoffer.offer_service.exception.DatasetExportConflictException;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.to.DatasetExportFileTO;
import pl.easyoffer.offer_service.model.to.DatasetExportRequestTO;
import pl.easyoffer.offer_service.model.to.DatasetManifestTO;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;
import pl.easyoffer.offer_service.service.synchronizer.SynchronizationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "dataset.export.directory=target/f015-test-datasets")
@ActiveProfiles("test")
class DatasetExportServiceIntegrationTest {

    private static final Path EXPORT_ROOT = Path.of("target", "f015-test-datasets")
            .toAbsolutePath();
    private static final String DATASET_ID = "DATASET-TEST-001";
    private static final String CODE_REVISION = "37a1345f5fed745b7135b66d2a35331ab126ebbd";

    @Autowired
    private DatasetExportService datasetExportService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private TechnologyRepository technologyRepository;

    @Autowired
    private SynchronizationRunRepository synchronizationRunRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void prepare() throws IOException {
        cleanDatabase();
        deleteDirectory(EXPORT_ROOT);
        seedDataset();
    }

    @AfterEach
    void cleanUp() throws IOException {
        cleanDatabase();
        deleteDirectory(EXPORT_ROOT);
    }

    @Test
    void shouldCreateImmutableSnapshotWithChecksumsManifestAndCounts() throws Exception {
        DatasetManifestTO manifest = datasetExportService.export(
                new DatasetExportRequestTO(DATASET_ID, CODE_REVISION)
        );

        Path datasetDirectory = EXPORT_ROOT.resolve(DATASET_ID);
        assertThat(datasetDirectory).isDirectory();
        assertThat(manifest.datasetId()).isEqualTo(DATASET_ID);
        assertThat(manifest.codeRevision()).isEqualTo(CODE_REVISION);
        assertThat(manifest.schemaVersion()).isEqualTo("8");
        assertThat(manifest.recordCounts()).containsExactly(
                org.assertj.core.data.MapEntry.entry("offers", 1L),
                org.assertj.core.data.MapEntry.entry("technologies", 1L),
                org.assertj.core.data.MapEntry.entry("offerTechnologies", 1L),
                org.assertj.core.data.MapEntry.entry("synchronizationRuns", 1L)
        );
        assertThat(manifest.offerCountsBySource()).containsEntry("JUST_JOIN_IT", 1L);
        assertThat(manifest.normalizationVersions().technology()).containsExactly("v1");
        assertThat(manifest.datasetFingerprint()).matches("[0-9a-f]{64}");
        assertThat(manifest.collectionStartedAt()).isNotNull();
        assertThat(manifest.collectionEndedAt()).isNotNull();

        for (DatasetExportFileTO file : manifest.files()) {
            Path exportedFile = datasetDirectory.resolve(file.fileName());
            assertThat(exportedFile).isRegularFile();
            assertThat(file.sha256()).isEqualTo(sha256(exportedFile));
            assertThat(file.sizeBytes()).isEqualTo(Files.size(exportedFile));
        }

        String offersCsv = Files.readString(datasetDirectory.resolve("offers.csv"));
        assertThat(offersCsv).contains("\"Senior \"\"Java\"\", Developer\"");

        Path manifestFile = datasetDirectory.resolve("manifest.json");
        JsonNode savedManifest = objectMapper.readTree(manifestFile.toFile());
        assertThat(savedManifest.get("datasetFingerprint").asText())
                .isEqualTo(manifest.datasetFingerprint());
        assertThat(Files.readString(datasetDirectory.resolve("manifest.sha256")))
                .isEqualTo(sha256(manifestFile) + "  manifest.json\n");

        byte[] originalManifest = Files.readAllBytes(manifestFile);
        assertThatThrownBy(() -> datasetExportService.export(
                new DatasetExportRequestTO(DATASET_ID, CODE_REVISION)
        )).isInstanceOf(DatasetExportConflictException.class)
                .hasMessageContaining(DATASET_ID);
        assertThat(Files.readAllBytes(manifestFile)).isEqualTo(originalManifest);
        try (var paths = Files.list(EXPORT_ROOT)) {
            assertThat(paths.toList()).containsExactly(datasetDirectory);
        }
    }

    private void seedDataset() {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName("java");
        technology.setCanonicalName("Java");
        technology.setNormalizationVersion("v1");
        technology.setLevel(4);
        technologyRepository.saveAndFlush(technology);

        OfferEntity offer = new OfferEntity();
        offer.setExternalId("offer-1");
        offer.setTitle("Senior \"Java\", Developer");
        offer.setCompanyName("Example");
        offer.setSource(JobOfferSourceType.JUST_JOIN_IT.name());
        offer.setSalaryType(SalaryType.GROSS);
        offer.setDeduplicationFingerprint(
                "v1:aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        );
        offer.getTechnologies().add(technology);
        offerRepository.saveAndFlush(offer);

        Instant startedAt = Instant.parse("2026-10-15T20:18:08Z");
        SynchronizationRunEntity run = SynchronizationRunEntity.start(
                UUID.fromString("cb2789c5-f2b7-410d-a73c-b99f42556004"),
                JobOfferSourceType.JUST_JOIN_IT,
                startedAt
        );
        run.succeed(new SynchronizationResult(1, 1, 0, 0), startedAt.plusSeconds(120));
        synchronizationRunRepository.saveAndFlush(run);
    }

    private void cleanDatabase() {
        offerRepository.deleteAll();
        technologyRepository.deleteAll();
        synchronizationRunRepository.deleteAll();
    }

    private static String sha256(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(Files.readAllBytes(file));
        return HexFormat.of().formatHex(digest.digest());
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
