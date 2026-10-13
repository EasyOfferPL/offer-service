UPDATE offers
SET source = 'MANUAL'
WHERE source IS NULL OR TRIM(source) = '';

ALTER TABLE offers
    ALTER COLUMN source SET DEFAULT 'MANUAL';

ALTER TABLE offers
    ALTER COLUMN source SET NOT NULL;

ALTER TABLE offers
    ADD CONSTRAINT uk_offers_source_external_id UNIQUE (source, external_id);
