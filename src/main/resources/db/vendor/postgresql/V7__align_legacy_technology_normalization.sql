WITH normalized_technology_names AS (
    SELECT id,
           BTRIM(
                   REGEXP_REPLACE(
                           NORMALIZE(name, NFKC),
                           '[[:space:]]+',
                           ' ',
                           'g'
                   )
           ) AS cleaned_name
    FROM technologies
)
UPDATE technologies technology
SET canonical_name = CASE LOWER(normalized.cleaned_name)
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
                         ELSE normalized.cleaned_name
    END,
    normalization_version = 'v1'
FROM normalized_technology_names normalized
WHERE technology.id = normalized.id;
