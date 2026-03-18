-- V8: Thêm cột min_stock cho cảnh báo tồn kho thấp
ALTER TABLE products ADD COLUMN min_stock INT DEFAULT NULL;
