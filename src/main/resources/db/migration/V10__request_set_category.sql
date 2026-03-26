-- =====================================================
-- V10: Request Set Category
-- Thêm cột category vào request_sets để phân loại
-- 6 loại: VAI_GIAO_THO, VAI_NHAP_KHO_THO, VAI_TRA_KHACH,
--         PHU_LIEU, PHU_KIEN, HANG_MAY_SAN
-- =====================================================

ALTER TABLE request_sets
  ADD COLUMN category ENUM(
    'VAI_GIAO_THO',
    'VAI_NHAP_KHO_THO',
    'VAI_TRA_KHACH',
    'PHU_LIEU',
    'PHU_KIEN',
    'HANG_MAY_SAN'
  ) NULL AFTER description;

CREATE INDEX idx_set_category ON request_sets(category);

-- Backfill dữ liệu cũ dựa trên product_id của request đầu tiên trong set
UPDATE request_sets rs
JOIN (
  SELECT ir.set_id, ir.product_id
  FROM inventory_requests ir
  INNER JOIN (
    SELECT set_id, MIN(request_id) AS min_id
    FROM inventory_requests
    WHERE set_id IS NOT NULL
    GROUP BY set_id
  ) first_req ON ir.request_id = first_req.min_id
) subq ON subq.set_id = rs.set_id
SET rs.category = CASE
  WHEN subq.product_id = 10 THEN 'PHU_LIEU'
  WHEN subq.product_id IN (9, 18) THEN 'PHU_KIEN'
  WHEN subq.product_id = 8 THEN 'VAI_GIAO_THO'
  ELSE 'HANG_MAY_SAN'
END
WHERE rs.category IS NULL;
