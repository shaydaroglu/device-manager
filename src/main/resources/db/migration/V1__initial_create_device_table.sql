CREATE TYPE device_state as ENUM('available', 'in-use', 'inactive');

CREATE TABLE devices
(
    id            UUID PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    brand         VARCHAR(255) NOT NULL,
    state         device_state NOT NULL DEFAULT 'inactive',
    creation_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_devices_brand ON devices (brand);
CREATE INDEX idx_devices_state ON devices (state);

CREATE OR REPLACE FUNCTION update_update_time_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_devices_update_time
BEFORE UPDATE
ON devices
FOR EACH ROW
EXECUTE FUNCTION update_update_time_column();
