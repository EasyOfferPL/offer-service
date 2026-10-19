package pl.easyoffer.offer_service.performance;

import java.net.URI;

final class DedicatedPerformanceDatabaseGuard {

    static final String REQUIRED_DATABASE_NAME = "offerdb_performance";

    private DedicatedPerformanceDatabaseGuard() {
    }

    static void verify(String jdbcUrl, String confirmation) {
        if (!REQUIRED_DATABASE_NAME.equals(confirmation)) {
            throw new IllegalStateException(
                    "Set performance.confirmDatabase=" + REQUIRED_DATABASE_NAME
                            + " to acknowledge the dedicated disposable database"
            );
        }
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:postgresql:")) {
            throw new IllegalStateException("F-016 requires a PostgreSQL JDBC URL");
        }

        URI databaseUri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String databaseName = databaseUri.getPath();
        if (databaseName == null || databaseName.length() < 2
                || !REQUIRED_DATABASE_NAME.equals(databaseName.substring(1))) {
            throw new IllegalStateException(
                    "F-016 refuses to use any database except " + REQUIRED_DATABASE_NAME
            );
        }
    }
}
