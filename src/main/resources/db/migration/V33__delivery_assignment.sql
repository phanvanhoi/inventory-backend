ALTER TABLE delivery_trips
    ADD COLUMN assigned_driver_id BIGINT NULL,
    ADD CONSTRAINT fk_trip_assigned_driver FOREIGN KEY (assigned_driver_id) REFERENCES delivery_drivers(driver_id);

ALTER TABLE delivery_books
    ADD COLUMN sudden_confirmed BOOLEAN NOT NULL DEFAULT FALSE;
