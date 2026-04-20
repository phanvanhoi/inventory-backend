-- =============================================================
-- V21: Expand Order - 6 mốc số đo chi tiết + file upload
-- Ref: docs/lark-integration-roadmap.md §G3 (W7-9)
-- Thêm từ Excel sheet "Phòng số đo - Đơn hàng":
--   6 ngày mốc: khách gửi DS → trả sổ → nhận từ KTV → gửi chốt KH → nhận chốt → bàn giao
--   2 FK user: người đi đo, người soạn số đo
--   4 file URL: hợp đồng, biên bản bàn giao, biên bản thanh lý, file số đo khách
-- =============================================================

-- 6 mốc ngày số đo chi tiết
ALTER TABLE orders ADD COLUMN customer_registration_sent_date     DATE;     -- Ngày KH gửi DS đăng ký
ALTER TABLE orders ADD COLUMN tech_book_return_date               DATE;     -- Ngày trả sổ đi đo cho KTV
ALTER TABLE orders ADD COLUMN measurement_received_from_tech_date DATE;     -- Ngày nhận số đo từ KTV
ALTER TABLE orders ADD COLUMN list_sent_to_customer_date          DATE;     -- Ngày gửi DS để chốt với KH
ALTER TABLE orders ADD COLUMN list_finalized_date                 DATE;     -- Ngày nhận DS chốt
ALTER TABLE orders ADD COLUMN measurement_handover_date_v2        DATE;     -- Ngày bàn giao số đo (khác production_handover_date)

-- 2 FK user (người đi đo / người soạn số đo)
ALTER TABLE orders ADD COLUMN measurement_taker_user_id    BIGINT NULL;
ALTER TABLE orders ADD COLUMN measurement_composer_user_id BIGINT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_order_meas_taker
    FOREIGN KEY (measurement_taker_user_id)    REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE orders ADD CONSTRAINT fk_order_meas_composer
    FOREIGN KEY (measurement_composer_user_id) REFERENCES users(user_id) ON DELETE SET NULL;

-- 4 file URLs (StorageService-generated paths)
ALTER TABLE orders ADD COLUMN contract_file_url             VARCHAR(500);
ALTER TABLE orders ADD COLUMN handover_record_url           VARCHAR(500);
ALTER TABLE orders ADD COLUMN liquidation_record_url        VARCHAR(500);
ALTER TABLE orders ADD COLUMN customer_measurement_file_url VARCHAR(500);
