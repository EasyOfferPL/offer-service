ALTER TABLE technologies
    ADD COLUMN canonical_name VARCHAR(100);

ALTER TABLE technologies
    ADD COLUMN normalization_version VARCHAR(20);

UPDATE technologies
SET canonical_name = CASE LOWER(TRIM(name))
                         WHEN '.net' THEN '.NET'
                         WHEN 'dotnet' THEN '.NET'
                         WHEN '.net core' THEN '.NET Core'
                         WHEN 'dotnet core' THEN '.NET Core'
                         WHEN '.net framework' THEN '.NET Framework'
                         WHEN 'dotnet framework' THEN '.NET Framework'
                         WHEN 'angular' THEN 'Angular'
                         WHEN 'amazon web services' THEN 'AWS'
                         WHEN 'aws' THEN 'AWS'
                         WHEN 'bash' THEN 'Bash'
                         WHEN 'c#' THEN 'C#'
                         WHEN 'c sharp' THEN 'C#'
                         WHEN 'csharp' THEN 'C#'
                         WHEN 'c++' THEN 'C++'
                         WHEN 'cpp' THEN 'C++'
                         WHEN 'c plus plus' THEN 'C++'
                         WHEN 'ci/cd' THEN 'CI/CD'
                         WHEN 'cicd' THEN 'CI/CD'
                         WHEN 'ci cd' THEN 'CI/CD'
                         WHEN 'css' THEN 'CSS'
                         WHEN 'devops' THEN 'DevOps'
                         WHEN 'docker' THEN 'Docker'
                         WHEN 'etl' THEN 'ETL'
                         WHEN 'fastapi' THEN 'FastAPI'
                         WHEN 'fast api' THEN 'FastAPI'
                         WHEN 'git' THEN 'Git'
                         WHEN 'github actions' THEN 'GitHub Actions'
                         WHEN 'gitlab ci' THEN 'GitLab CI/CD'
                         WHEN 'gitlab ci/cd' THEN 'GitLab CI/CD'
                         WHEN 'gcp' THEN 'Google Cloud Platform'
                         WHEN 'google cloud platform' THEN 'Google Cloud Platform'
                         WHEN 'html' THEN 'HTML'
                         WHEN 'java' THEN 'Java'
                         WHEN 'java script' THEN 'JavaScript'
                         WHEN 'javascript' THEN 'JavaScript'
                         WHEN 'jenkins' THEN 'Jenkins'
                         WHEN 'jira' THEN 'Jira'
                         WHEN 'apache kafka' THEN 'Kafka'
                         WHEN 'kafka' THEN 'Kafka'
                         WHEN 'kotlin' THEN 'Kotlin'
                         WHEN 'k8s' THEN 'Kubernetes'
                         WHEN 'kubernetes' THEN 'Kubernetes'
                         WHEN 'linux' THEN 'Linux'
                         WHEN 'machine learning' THEN 'Machine Learning'
                         WHEN 'microservices' THEN 'Microservices'
                         WHEN 'mongodb' THEN 'MongoDB'
                         WHEN 'mysql' THEN 'MySQL'
                         WHEN 'node.js' THEN 'Node.js'
                         WHEN 'nodejs' THEN 'Node.js'
                         WHEN 'oracle' THEN 'Oracle'
                         WHEN 'postgres' THEN 'PostgreSQL'
                         WHEN 'postgre sql' THEN 'PostgreSQL'
                         WHEN 'postgresql' THEN 'PostgreSQL'
                         WHEN 'powershell' THEN 'PowerShell'
                         WHEN 'python' THEN 'Python'
                         WHEN 'rabbitmq' THEN 'RabbitMQ'
                         WHEN 'react' THEN 'React'
                         WHEN 'react.js' THEN 'React'
                         WHEN 'reactjs' THEN 'React'
                         WHEN 'rest api' THEN 'REST API'
                         WHEN 'scrum' THEN 'Scrum'
                         WHEN 'spring' THEN 'Spring'
                         WHEN 'spring boot' THEN 'Spring Boot'
                         WHEN 'springboot' THEN 'Spring Boot'
                         WHEN 'sql' THEN 'SQL'
                         WHEN 'terraform' THEN 'Terraform'
                         WHEN 'type script' THEN 'TypeScript'
                         WHEN 'typescript' THEN 'TypeScript'
                         WHEN 'vue.js' THEN 'Vue.js'
                         WHEN 'vuejs' THEN 'Vue.js'
                         ELSE TRIM(name)
    END,
    normalization_version = 'v1';

ALTER TABLE technologies
    ALTER COLUMN canonical_name SET NOT NULL;

ALTER TABLE technologies
    ALTER COLUMN normalization_version SET NOT NULL;

ALTER TABLE technologies
    ADD CONSTRAINT chk_technologies_canonical_name_not_blank
        CHECK (TRIM(canonical_name) <> '');

ALTER TABLE technologies
    ADD CONSTRAINT chk_technologies_normalization_version_not_blank
        CHECK (TRIM(normalization_version) <> '');

CREATE INDEX idx_technologies_canonical_name
    ON technologies (canonical_name);
