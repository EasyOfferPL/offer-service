package pl.easyoffer.offer_service.service.dataset;

import org.springframework.stereotype.Component;
import pl.easyoffer.offer_service.model.to.DatasetExportFileTO;
import pl.easyoffer.offer_service.repository.projection.DatasetExportRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.stream.Stream;

@Component
class DatasetCsvWriter {

    private static final String HASH_ALGORITHM = "SHA-256";

    DatasetExportFileTO write(Path directory, DatasetExportTable table, Stream<? extends DatasetExportRow> rows)
            throws IOException {
        Path output = directory.resolve(table.fileName());
        long recordCount = 0;

        try (rows; BufferedWriter writer = Files.newBufferedWriter(
                output,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        )) {
            writeRow(writer, table.headers().toArray(String[]::new));
            Iterator<? extends DatasetExportRow> iterator = rows.iterator();
            while (iterator.hasNext()) {
                writeRow(writer, iterator.next().values());
                recordCount++;
            }
        }

        return new DatasetExportFileTO(
                table.fileName(),
                recordCount,
                Files.size(output),
                sha256(output)
        );
    }

    private void writeRow(BufferedWriter writer, Object[] values) throws IOException {
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                writer.write(',');
            }
            writer.write(escape(format(values[index])));
        }
        writer.write('\n');
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toPlainString();
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (value instanceof OffsetDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        if (value instanceof ZonedDateTime dateTime) {
            return dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
        if (value instanceof Instant instant) {
            return DateTimeFormatter.ISO_INSTANT.format(instant);
        }
        return value.toString();
    }

    private String escape(String value) {
        if (value.indexOf(',') < 0
                && value.indexOf('"') < 0
                && value.indexOf('\n') < 0
                && value.indexOf('\r') < 0) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    static String sha256(Path file) throws IOException {
        MessageDigest digest = messageDigest();
        try (var input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8_192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    static MessageDigest messageDigest() {
        try {
            return MessageDigest.getInstance(HASH_ALGORITHM);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(HASH_ALGORITHM + " is not available", exception);
        }
    }
}
