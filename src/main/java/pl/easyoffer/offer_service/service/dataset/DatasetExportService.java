package pl.easyoffer.offer_service.service.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.config.DatasetExportProperties;
import pl.easyoffer.offer_service.exception.DatasetExportConflictException;
import pl.easyoffer.offer_service.exception.ValidationException;
import pl.easyoffer.offer_service.model.to.DatasetExportFileTO;
import pl.easyoffer.offer_service.model.to.DatasetExportRequestTO;
import pl.easyoffer.offer_service.model.to.DatasetManifestTO;
import pl.easyoffer.offer_service.model.to.DatasetNormalizationVersionsTO;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.SynchronizationRunRepository;
import pl.easyoffer.offer_service.repository.TechnologyRepository;
import pl.easyoffer.offer_service.repository.projection.DatasetExportRow;
import pl.easyoffer.offer_service.repository.projection.OfferSourceCountProjection;
import pl.easyoffer.offer_service.repository.projection.SynchronizationCollectionRangeProjection;
import pl.easyoffer.offer_service.service.deduplication.CrossSourceFingerprintV1;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DatasetExportService {

    private static final String MANIFEST_VERSION = "v1";
    private static final String MANIFEST_FILE_NAME = "manifest.json";
    private static final String MANIFEST_CHECKSUM_FILE_NAME = "manifest.sha256";
    private static final Pattern DATASET_ID_PATTERN = Pattern.compile(
            "[A-Za-z0-9][A-Za-z0-9._-]{2,63}"
    );
    private static final Pattern CODE_REVISION_PATTERN = Pattern.compile("[0-9a-fA-F]{7,64}");

    private final DatasetExportProperties properties;
    private final DatasetCsvWriter csvWriter;
    private final OfferRepository offerRepository;
    private final TechnologyRepository technologyRepository;
    private final SynchronizationRunRepository synchronizationRunRepository;
    private final Flyway flyway;
    private final ObjectMapper objectMapper;
    private final ReentrantLock exportLock = new ReentrantLock();

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DatasetManifestTO export(DatasetExportRequestTO request) {
        validate(request);
        if (!exportLock.tryLock()) {
            throw new DatasetExportConflictException("Another dataset export is already running");
        }

        Path stagingDirectory = null;
        try {
            Path exportRoot = prepareExportRoot();
            Path destination = resolveDestination(exportRoot, request.datasetId());
            if (Files.exists(destination)) {
                throw new DatasetExportConflictException(
                        "Dataset already exists: " + request.datasetId()
                );
            }

            stagingDirectory = Files.createDirectory(exportRoot.resolve(
                    "." + request.datasetId() + "-" + UUID.randomUUID() + ".tmp"
            ));
            DatasetManifestTO manifest = createManifest(stagingDirectory, request);
            writeManifest(stagingDirectory, manifest);
            publish(stagingDirectory, destination);
            stagingDirectory = null;
            return manifest;
        } catch (DatasetExportConflictException | ValidationException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not export dataset " + request.datasetId(), exception
            );
        } finally {
            deleteStagingDirectory(stagingDirectory);
            exportLock.unlock();
        }
    }

    private DatasetManifestTO createManifest(
            Path stagingDirectory,
            DatasetExportRequestTO request
    ) throws IOException {
        Instant exportedAt = Instant.now();
        List<DatasetExportFileTO> files = new ArrayList<>();
        Map<String, Long> recordCounts = new LinkedHashMap<>();

        for (DatasetExportTable table : DatasetExportTable.values()) {
            DatasetExportFileTO file = csvWriter.write(
                    stagingDirectory,
                    table,
                    datasetRows(table)
            );
            files.add(file);
            recordCounts.put(table.countName(), file.recordCount());
        }

        CollectionRange collectionRange = collectionRange();
        return new DatasetManifestTO(
                MANIFEST_VERSION,
                request.datasetId(),
                datasetFingerprint(files),
                exportedAt,
                collectionRange.startedAt(),
                collectionRange.finishedAt(),
                request.codeRevision().toLowerCase(Locale.ROOT),
                schemaVersion(),
                normalizationVersions(),
                Collections.unmodifiableMap(recordCounts),
                offerCountsBySource(),
                List.copyOf(files)
        );
    }

    private Path prepareExportRoot() throws IOException {
        Path exportRoot = properties.getDirectory().toAbsolutePath().normalize();
        Files.createDirectories(exportRoot);
        if (!Files.isDirectory(exportRoot)) {
            throw new IOException("Dataset export root is not a directory: " + exportRoot);
        }
        return exportRoot;
    }

    private Path resolveDestination(Path exportRoot, String datasetId) {
        Path destination = exportRoot.resolve(datasetId).normalize();
        if (!exportRoot.equals(destination.getParent())) {
            throw new ValidationException("Dataset ID resolves outside the export directory");
        }
        return destination;
    }

    private void writeManifest(Path stagingDirectory, DatasetManifestTO manifest) throws IOException {
        Path manifestFile = stagingDirectory.resolve(MANIFEST_FILE_NAME);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(manifestFile.toFile(), manifest);
        String manifestChecksum = DatasetCsvWriter.sha256(manifestFile);
        Files.writeString(
                stagingDirectory.resolve(MANIFEST_CHECKSUM_FILE_NAME),
                manifestChecksum + "  " + MANIFEST_FILE_NAME + "\n",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
    }

    private void publish(Path stagingDirectory, Path destination) throws IOException {
        try {
            Files.move(stagingDirectory, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            throw new IOException("Dataset export requires an atomic directory move", exception);
        } catch (FileAlreadyExistsException exception) {
            throw new DatasetExportConflictException(
                    "Dataset already exists: " + destination.getFileName()
            );
        }
    }

    private String datasetFingerprint(List<DatasetExportFileTO> files) {
        MessageDigest digest = DatasetCsvWriter.messageDigest();
        files.stream()
                .sorted(Comparator.comparing(DatasetExportFileTO::fileName))
                .forEach(file -> {
                    digest.update(file.fileName().getBytes(StandardCharsets.UTF_8));
                    digest.update((byte) 0);
                    digest.update(file.sha256().getBytes(StandardCharsets.US_ASCII));
                    digest.update((byte) '\n');
                });
        return HexFormat.of().formatHex(digest.digest());
    }

    private String schemaVersion() {
        MigrationInfo currentMigration = flyway.info().current();
        return currentMigration == null ? null : currentMigration.getVersion().getVersion();
    }

    private DatasetNormalizationVersionsTO normalizationVersions() {
        return new DatasetNormalizationVersionsTO(
                List.copyOf(technologyRepository.findDistinctNormalizationVersions()),
                CrossSourceFingerprintV1.version(),
                SalaryComparabilityPolicy.VERSION
        );
    }

    private Map<String, Long> offerCountsBySource() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (OfferSourceCountProjection sourceCount : offerRepository.countOffersBySource()) {
            counts.put(sourceCount.getSource(), sourceCount.getOfferCount());
        }
        return Collections.unmodifiableMap(counts);
    }

    private CollectionRange collectionRange() {
        SynchronizationCollectionRangeProjection range =
                synchronizationRunRepository.findCollectionRange();
        if (range == null) {
            return new CollectionRange(null, null);
        }
        return new CollectionRange(range.getStartedAt(), range.getEndedAt());
    }

    private Stream<? extends DatasetExportRow> datasetRows(DatasetExportTable table) {
        return switch (table) {
            case OFFERS -> offerRepository.streamDatasetExportRows();
            case TECHNOLOGIES -> technologyRepository.streamDatasetExportRows();
            case OFFER_TECHNOLOGIES -> offerRepository.streamTechnologyDatasetExportRows();
            case SYNCHRONIZATION_RUNS -> synchronizationRunRepository.streamDatasetExportRows();
        };
    }

    private void validate(DatasetExportRequestTO request) {
        if (request == null) {
            throw new ValidationException("Dataset export request is required");
        }
        if (request.datasetId() == null
                || !DATASET_ID_PATTERN.matcher(request.datasetId()).matches()) {
            throw new ValidationException("Dataset ID has an invalid format");
        }
        if (request.codeRevision() == null
                || !CODE_REVISION_PATTERN.matcher(request.codeRevision()).matches()) {
            throw new ValidationException("Code revision has an invalid format");
        }
    }

    private void deleteStagingDirectory(Path stagingDirectory) {
        if (stagingDirectory == null || !Files.exists(stagingDirectory)) {
            return;
        }
        try (var paths = Files.walk(stagingDirectory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // A failed cleanup must not hide the original export failure.
                }
            });
        } catch (IOException ignored) {
            // A failed cleanup must not hide the original export failure.
        }
    }

    private record CollectionRange(Instant startedAt, Instant finishedAt) {
    }
}
