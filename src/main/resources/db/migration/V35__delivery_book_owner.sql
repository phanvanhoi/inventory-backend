ALTER TABLE delivery_books
    ADD COLUMN owner_name VARCHAR(80) NULL;

UPDATE delivery_books b
JOIN delivery_trips t ON t.trip_id = b.trip_id
SET b.owner_name = t.owner_name
WHERE b.owner_name IS NULL
  AND t.owner_name IS NOT NULL;
