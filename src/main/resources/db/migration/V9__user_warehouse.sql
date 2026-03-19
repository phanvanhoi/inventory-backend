-- V9: Gán kho cho thủ kho (STOCKKEEPER)
ALTER TABLE users ADD COLUMN warehouse_id BIGINT NULL;
ALTER TABLE users ADD CONSTRAINT fk_user_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id);
