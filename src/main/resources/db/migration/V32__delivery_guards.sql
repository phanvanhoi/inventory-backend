ALTER TABLE delivery_drivers
    ADD COLUMN user_id BIGINT NULL,
    ADD CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    ADD CONSTRAINT uk_driver_user UNIQUE (user_id);

ALTER TABLE driver_day_plans
    ADD COLUMN start_stop_id BIGINT NULL,
    ADD COLUMN passed_seq INT NOT NULL DEFAULT 0,
    ADD COLUMN touched_open_stop BOOLEAN NOT NULL DEFAULT FALSE,
    ADD CONSTRAINT fk_plan_start_stop FOREIGN KEY (start_stop_id) REFERENCES delivery_stops(stop_id),
    ADD CONSTRAINT uk_plan_driver_date UNIQUE (driver_id, service_date);

INSERT INTO roles (role_name, description)
SELECT 'DISPATCHER', 'Điều phối giao hàng: xếp lịch, khóa sáng, sửa khóa bản đồ và xác nhận hộ'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'DISPATCHER');
