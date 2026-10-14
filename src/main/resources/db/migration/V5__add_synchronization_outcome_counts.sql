ALTER TABLE synchronization_runs
    ADD COLUMN created_offers INTEGER;

ALTER TABLE synchronization_runs
    ADD COLUMN updated_offers INTEGER;

ALTER TABLE synchronization_runs
    ADD COLUMN skipped_offers INTEGER;

ALTER TABLE synchronization_runs
    ADD CONSTRAINT chk_synchronization_run_outcome_counts
        CHECK (
            (
                created_offers IS NULL
                AND updated_offers IS NULL
                AND skipped_offers IS NULL
            )
            OR
            (
                created_offers >= 0
                AND updated_offers >= 0
                AND skipped_offers >= 0
                AND fetched_offers = created_offers + updated_offers + skipped_offers
                AND persisted_offers = created_offers + updated_offers
            )
        );
