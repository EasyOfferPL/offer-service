ALTER TABLE offers
    ADD COLUMN salary_type VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN';

ALTER TABLE offers
    ADD CONSTRAINT chk_offers_salary_type
        CHECK (salary_type IN ('GROSS', 'NET', 'UNKNOWN'));
