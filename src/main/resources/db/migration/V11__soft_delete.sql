-- V10: Soft delete cho users và products
ALTER TABLE users ADD COLUMN deleted_at DATETIME NULL;
ALTER TABLE products ADD COLUMN deleted_at DATETIME NULL;
