ALTER TABLE devices
    ALTER COLUMN state DROP DEFAULT;

ALTER TABLE devices
    ALTER COLUMN state TYPE VARCHAR(50)
        USING state::text;

ALTER TABLE devices
    ALTER COLUMN state SET DEFAULT 'AVAILABLE';

ALTER TABLE devices
    ADD CONSTRAINT chk_devices_state
        CHECK (state IN ('AVAILABLE', 'IN_USE', 'INACTIVE'));

DROP TYPE device_state;