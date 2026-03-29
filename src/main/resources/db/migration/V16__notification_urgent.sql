-- Add urgent flag to notifications for rate-change alerts
ALTER TABLE notifications ADD COLUMN is_urgent BOOLEAN NOT NULL DEFAULT FALSE;
