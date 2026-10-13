ALTER TABLE offers
    -- v1 prefix and SHA-256 hexadecimal digest
    ADD COLUMN deduplication_fingerprint VARCHAR(67);

CREATE INDEX idx_job_offer_deduplication_fingerprint
    ON offers (deduplication_fingerprint);
