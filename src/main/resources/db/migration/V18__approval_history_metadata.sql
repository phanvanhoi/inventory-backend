-- Store rollback data (JSON) for EDIT_AND_RECEIVE actions
ALTER TABLE approval_history ADD COLUMN metadata TEXT NULL;
