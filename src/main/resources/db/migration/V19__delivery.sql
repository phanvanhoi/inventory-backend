CREATE TABLE delivery_stops (
    stop_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stop_name VARCHAR(255) NOT NULL,
    address VARCHAR(500) NULL,
    latitude DOUBLE NULL,
    longitude DOUBLE NULL,
    is_shop BOOLEAN NOT NULL DEFAULT FALSE,
    unit_id BIGINT NULL,
    warehouse_id BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_delivery_stop_unit FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    CONSTRAINT fk_delivery_stop_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE delivery_drivers (
    driver_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE delivery_settings (
    setting_id TINYINT PRIMARY KEY,
    distance_base_url VARCHAR(255) NULL,
    detour_limit_meters INT NOT NULL DEFAULT 5000,
    cost_per_km DECIMAL(12,0) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO delivery_settings (setting_id, detour_limit_meters, cost_per_km) VALUES (1, 5000, 0);

CREATE TABLE delivery_trips (
    trip_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_date DATE NOT NULL,
    pickup_stop_id BIGINT NOT NULL,
    dropoff_stop_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_trip_pickup FOREIGN KEY (pickup_stop_id) REFERENCES delivery_stops(stop_id),
    CONSTRAINT fk_trip_dropoff FOREIGN KEY (dropoff_stop_id) REFERENCES delivery_stops(stop_id),
    INDEX idx_trip_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE delivery_books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pickup_stop_id BIGINT NOT NULL,
    dropoff_stop_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    note VARCHAR(500) NULL,
    request_set_id BIGINT NULL,
    status VARCHAR(30) NOT NULL,
    trip_id BIGINT NULL,
    created_by BIGINT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_confirmed_at DATETIME NULL,
    sender_confirmed_by BIGINT NULL,
    sender_on_behalf BOOLEAN NOT NULL DEFAULT FALSE,
    receiver_confirmed_at DATETIME NULL,
    receiver_confirmed_by BIGINT NULL,
    receiver_on_behalf BOOLEAN NOT NULL DEFAULT FALSE,
    sudden BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_book_pickup FOREIGN KEY (pickup_stop_id) REFERENCES delivery_stops(stop_id),
    CONSTRAINT fk_book_dropoff FOREIGN KEY (dropoff_stop_id) REFERENCES delivery_stops(stop_id),
    CONSTRAINT fk_book_trip FOREIGN KEY (trip_id) REFERENCES delivery_trips(trip_id),
    INDEX idx_book_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE delivery_book_lines (
    line_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_label VARCHAR(50) NULL,
    CONSTRAINT fk_line_book FOREIGN KEY (book_id) REFERENCES delivery_books(book_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE driver_day_plans (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    return_to_shop BOOLEAN NOT NULL DEFAULT TRUE,
    total_meters INT NOT NULL DEFAULT 0,
    explanation TEXT NULL,
    CONSTRAINT fk_plan_driver FOREIGN KEY (driver_id) REFERENCES delivery_drivers(driver_id),
    INDEX idx_plan_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_visits (
    visit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    seq_no INT NOT NULL,
    stop_id BIGINT NOT NULL,
    CONSTRAINT fk_visit_plan FOREIGN KEY (plan_id) REFERENCES driver_day_plans(plan_id) ON DELETE CASCADE,
    CONSTRAINT fk_visit_stop FOREIGN KEY (stop_id) REFERENCES delivery_stops(stop_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE plan_visit_actions (
    action_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    visit_id BIGINT NOT NULL,
    trip_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    CONSTRAINT fk_action_visit FOREIGN KEY (visit_id) REFERENCES plan_visits(visit_id) ON DELETE CASCADE,
    CONSTRAINT fk_action_trip FOREIGN KEY (trip_id) REFERENCES delivery_trips(trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE dispatch_notices (
    notice_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    service_date DATE NOT NULL,
    message TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acknowledged_at DATETIME NULL,
    CONSTRAINT fk_notice_driver FOREIGN KEY (driver_id) REFERENCES delivery_drivers(driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE driver_locations (
    driver_id BIGINT PRIMARY KEY,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    recorded_at DATETIME NOT NULL,
    CONSTRAINT fk_location_driver FOREIGN KEY (driver_id) REFERENCES delivery_drivers(driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
