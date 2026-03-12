ALTER TABLE inventory_requests ADD COLUMN fabric_metadata TEXT NULL COMMENT 'JSON state cho fabric templates (norms, workers, warehouses...)';
