package pl.easyoffer.offer_service.service.normalization;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class TechnologyNormalizer {

    public static final String NORMALIZATION_VERSION = "v1";

    private static final Pattern WHITESPACE = Pattern.compile(
            "\\s+", Pattern.UNICODE_CHARACTER_CLASS
    );
    private static final Map<String, String> CANONICAL_NAMES_BY_ALIAS = buildAliases();

    public Optional<TechnologyNormalizationResult> normalize(String name) {
        String sourceName = normalizeWhitespace(name);
        if (sourceName == null) {
            return Optional.empty();
        }

        String canonicalName = CANONICAL_NAMES_BY_ALIAS.getOrDefault(
                aliasKey(sourceName), sourceName
        );
        return Optional.of(new TechnologyNormalizationResult(
                sourceName, canonicalName, NORMALIZATION_VERSION
        ));
    }

    public List<String> normalizeCanonicalNames(Collection<String> names) {
        if (names == null) {
            return List.of();
        }
        return names.stream()
                .map(this::normalize)
                .flatMap(Optional::stream)
                .map(TechnologyNormalizationResult::canonicalName)
                .distinct()
                .toList();
    }

    public String version() {
        return NORMALIZATION_VERSION;
    }

    private static Map<String, String> buildAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        register(aliases, ".NET", "dotnet");
        register(aliases, ".NET Core", "dotnet core");
        register(aliases, ".NET Framework", "dotnet framework");
        register(aliases, "Angular");
        register(aliases, "AWS", "Amazon Web Services");
        register(aliases, "Bash");
        register(aliases, "C#", "C Sharp", "CSharp");
        register(aliases, "C++", "CPP", "C Plus Plus");
        register(aliases, "CI/CD", "CICD", "CI CD");
        register(aliases, "CSS");
        register(aliases, "DevOps");
        register(aliases, "Docker");
        register(aliases, "ETL");
        register(aliases, "FastAPI", "Fast API");
        register(aliases, "Git");
        register(aliases, "GitHub Actions");
        register(aliases, "GitLab CI/CD", "GitLab CI");
        register(aliases, "Google Cloud Platform", "GCP");
        register(aliases, "HTML");
        register(aliases, "Java");
        register(aliases, "JavaScript", "Java Script");
        register(aliases, "Jenkins");
        register(aliases, "Jira");
        register(aliases, "Kafka", "Apache Kafka");
        register(aliases, "Kotlin");
        register(aliases, "Kubernetes", "K8s");
        register(aliases, "Linux");
        register(aliases, "Machine Learning");
        register(aliases, "Microservices");
        register(aliases, "MongoDB");
        register(aliases, "MySQL");
        register(aliases, "Node.js", "NodeJS");
        register(aliases, "Oracle");
        register(aliases, "PostgreSQL", "Postgres", "Postgre SQL");
        register(aliases, "PowerShell");
        register(aliases, "Python");
        register(aliases, "RabbitMQ");
        register(aliases, "React", "React.js", "ReactJS");
        register(aliases, "REST API");
        register(aliases, "Scrum");
        register(aliases, "Spring");
        register(aliases, "Spring Boot", "SpringBoot");
        register(aliases, "SQL");
        register(aliases, "Terraform");
        register(aliases, "TypeScript", "Type Script");
        register(aliases, "Vue.js", "VueJS");
        return Map.copyOf(aliases);
    }

    private static void register(
            Map<String, String> aliases,
            String canonicalName,
            String... additionalAliases
    ) {
        registerAlias(aliases, canonicalName, canonicalName);
        for (String alias : additionalAliases) {
            registerAlias(aliases, alias, canonicalName);
        }
    }

    private static void registerAlias(
            Map<String, String> aliases,
            String alias,
            String canonicalName
    ) {
        String existingCanonicalName = aliases.put(aliasKey(alias), canonicalName);
        if (existingCanonicalName != null && !existingCanonicalName.equals(canonicalName)) {
            throw new IllegalStateException("Technology alias maps to multiple canonical names: " + alias);
        }
    }

    private static String aliasKey(String value) {
        return normalizeWhitespace(value).toLowerCase(Locale.ROOT);
    }

    private static String normalizeWhitespace(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).strip();
        if (normalized.isEmpty()) {
            return null;
        }
        return WHITESPACE.matcher(normalized).replaceAll(" ");
    }
}
