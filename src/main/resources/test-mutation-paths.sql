-- =====================================================
-- TEST DATA: 10 Mutation Paths
-- Date: 2026-03-24
-- =====================================================
-- Dùng account: thanh (user_id=3, ADMIN+USER+STOCKKEEPER)
--               thuy  (user_id=4, PURCHASER+USER+PRODUCTION)
--               khoa  (user_id=7, STOCKKEEPER)
--               hoi   (user_id=1, ADMIN+USER)
--
-- Variant IDs cần biết:
--   Product 1 (Sơ mi 2025): variant 1-88 (style+size+length)
--   Product 8 (Vải):        dùng SELECT để lấy variant_id
--   Product 9 (Phụ kiện):   dùng SELECT để lấy variant_id
--   Product 11 (Áo khoác):  variant bắt đầu từ ~89 (gender+size)
--   Product 12 (Áo phông):  variant (gender+size+length)
--
-- Warehouse: 1=Kho Chính, 2=Kho Trường
-- Unit: 1=BĐ Hà Nội
-- =====================================================

USE hangfashion_inventory;

-- =====================================================
-- Trước tiên: Tạo inventory ban đầu (tồn kho)
-- Dùng cho test xuất kho (Path 3, 4, 5)
-- =====================================================

-- Nhập 100 mét cho vài mã vải vào Kho Chính
-- (Tạo bộ phiếu nhập đã EXECUTED để có tồn kho thực tế)
INSERT INTO request_sets (set_name, status, created_by, created_at, executed_by, executed_at) VALUES
('SEED - Nhập kho vải', 'EXECUTED', 4, '2026-01-15 08:00:00', 3, '2026-01-15 10:00:00');
SET @seed_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, request_status, created_at) VALUES
(@seed_set, 1, 8, 'IN', 1, 'COMPLETED', '2026-01-15 08:00:00');
SET @seed_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @seed_req, pv.variant_id, 100.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B7', 'B12', 'B15', 'B22');

-- Nhập 50 chiếc phụ kiện vào Kho Chính
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, request_status, created_at) VALUES
(@seed_set, 1, 9, 'IN', 1, 'COMPLETED', '2026-01-15 08:00:00');
SET @seed_req2 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @seed_req2, pv.variant_id, 50.00
FROM product_variants pv
WHERE pv.product_id = 9 AND pv.item_code IN ('PK1', 'PK2', 'PK3');

-- Nhập 20 chiếc sơ mi Cổ điển size 38 Cộc vào Kho Chính
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, request_status, created_at) VALUES
(@seed_set, 1, 1, 'IN', 1, 'COMPLETED', '2026-01-15 08:00:00');
SET @seed_req3 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity) VALUES
(@seed_req3, 7, 20.00),  -- Cổ điển, size 38, Cộc (variant_id=7)
(@seed_req3, 8, 15.00),  -- Cổ điển, size 38, Dài (variant_id=8)
(@seed_req3, 9, 10.00);  -- Cổ điển, size 39, Cộc (variant_id=9)

-- Lưu history cho seed
INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@seed_set, 'SUBMIT', 4, '2026-01-15 08:00:00'),
(@seed_set, 'APPROVE', 1, '2026-01-15 09:00:00'),
(@seed_set, 'EXECUTE', 3, '2026-01-15 10:00:00');


-- =====================================================
-- PATH 1: CREATE REQUEST SET (PENDING)
-- Tạo bởi thuy (PURCHASER), chờ duyệt
-- Test: Login thuy → xem danh sách → thấy PENDING
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P1: Nhập vải BĐ HN', 'PENDING', 4, '2026-03-24 08:00:00');
SET @p1_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p1_set, 1, 8, 'IN', 1, '2026-03-24 08:00:00');
SET @p1_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p1_req, pv.variant_id,
  CASE pv.item_code
    WHEN 'B1' THEN 20.50
    WHEN 'B7' THEN 15.00
    WHEN 'B12' THEN 10.00
  END
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B7', 'B12');

INSERT INTO approval_history (set_id, action, performed_by, reason, created_at) VALUES
(@p1_set, 'SUBMIT', 4, NULL, '2026-03-24 08:00:00');


-- =====================================================
-- PATH 2: APPROVE REQUEST SET
-- PENDING → cần ADMIN duyệt
-- Test: Login hoi (ADMIN) → duyệt → status → APPROVED
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P2: Nhập phụ kiện - Chờ duyệt', 'PENDING', 4, '2026-03-24 08:10:00');
SET @p2_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p2_set, 1, 9, 'IN', 1, '2026-03-24 08:10:00');
SET @p2_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p2_req, pv.variant_id, 30.00
FROM product_variants pv
WHERE pv.product_id = 9 AND pv.item_code IN ('PK1', 'PK2');

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p2_set, 'SUBMIT', 4, '2026-03-24 08:10:00');


-- =====================================================
-- PATH 3A: EXECUTE REQUEST SET (APPROVED → EXECUTED)
-- Đã được duyệt, chờ STOCKKEEPER thực hiện
-- Test: Login khoa (STOCKKEEPER) → "Thực hiện" → inventory thay đổi
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P3A: Xuất vải BĐ HN - Chờ thực hiện', 'APPROVED', 4, '2026-03-24 08:20:00');
SET @p3a_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p3a_set, 1, 8, 'OUT', 1, '2026-03-24 08:20:00');
SET @p3a_req = LAST_INSERT_ID();

-- Xuất 5 mét B1 và 3 mét B7 (tồn kho có 100)
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p3a_req, pv.variant_id,
  CASE pv.item_code WHEN 'B1' THEN 5.00 WHEN 'B7' THEN 3.00 END
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B7');

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p3a_set, 'SUBMIT', 4, '2026-03-24 08:20:00'),
(@p3a_set, 'APPROVE', 1, '2026-03-24 09:00:00');


-- =====================================================
-- PATH 3B: COMPLETE REQUEST (multi-warehouse)
-- APPROVED set với 2 kho → STOCKKEEPER thực hiện từng request
-- Test: Login khoa → "Hoàn thành request" cho từng kho
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P3B: Xuất sơ mi 2 kho - Chờ hoàn thành', 'APPROVED', 4, '2026-03-24 08:30:00');
SET @p3b_set = LAST_INSERT_ID();

-- Request 1: Xuất từ Kho Chính
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p3b_set, 1, 1, 'OUT', 1, '2026-03-24 08:30:00');
SET @p3b_req1 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity) VALUES
(@p3b_req1, 7, 2.00),  -- Cổ điển 38 Cộc (tồn 20)
(@p3b_req1, 8, 1.00);  -- Cổ điển 38 Dài (tồn 15)

-- Request 2: Xuất từ Kho Trường
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p3b_set, 1, 1, 'OUT', 2, '2026-03-24 08:30:00');
SET @p3b_req2 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity) VALUES
(@p3b_req2, 7, 3.00);  -- Cổ điển 38 Cộc từ Kho Trường (chưa có tồn → expect fail)

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p3b_set, 'SUBMIT', 4, '2026-03-24 08:30:00'),
(@p3b_set, 'APPROVE', 1, '2026-03-24 09:00:00');


-- =====================================================
-- PATH 4: RECORD RECEIPT (Partial - nhận từng phần)
-- APPROVED → RECEIVING (nhận 1 phần) → nhận thêm → complete
-- Test: Login khoa → "Nhận hàng" → nhập SL → ghi nhận
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P4: Nhập vải - Nhận từng phần', 'APPROVED', 4, '2026-03-24 08:40:00');
SET @p4_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p4_set, 1, 8, 'IN', 1, '2026-03-24 08:40:00');
SET @p4_req = LAST_INSERT_ID();

-- Nhập 50 mét B1, 30 mét B7
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p4_req, pv.variant_id,
  CASE pv.item_code WHEN 'B1' THEN 50.00 WHEN 'B7' THEN 30.00 END
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B7');

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p4_set, 'SUBMIT', 4, '2026-03-24 08:40:00'),
(@p4_set, 'APPROVE', 1, '2026-03-24 09:00:00');

-- Gợi ý test:
-- Lần 1: Nhận 20 mét B1, 15 mét B7 → status chuyển RECEIVING
-- Lần 2: Nhận 30 mét B1, 15 mét B7 → đã đủ
-- Lần 3: Nhận thêm 1 mét B1 → EXPECT FAIL (vượt quota 50)


-- =====================================================
-- PATH 5: COMPLETE RECEIPT (RECEIVING → EXECUTED)
-- Đã nhận 1 phần, cần "Hoàn tất nhận hàng"
-- Test: Login khoa → "Hoàn tất" → inventory cập nhật
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P5: Nhập phụ kiện - Đã nhận 1 phần', 'RECEIVING', 4, '2026-03-24 08:50:00');
SET @p5_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p5_set, 1, 9, 'IN', 1, '2026-03-24 08:50:00');
SET @p5_req = LAST_INSERT_ID();

-- Đề xuất nhập 40 chiếc PK1
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p5_req, pv.variant_id, 40.00
FROM product_variants pv
WHERE pv.product_id = 9 AND pv.item_code = 'PK1';

-- Đã nhận 25 chiếc (receipt record đã có)
INSERT INTO receipt_records (set_id, received_by, received_at, note) VALUES
(@p5_set, 7, '2026-03-24 10:00:00', 'Nhận đợt 1');
SET @p5_receipt = LAST_INSERT_ID();

INSERT INTO receipt_items (receipt_id, request_id, variant_id, received_quantity)
SELECT @p5_receipt, @p5_req, pv.variant_id, 25.00
FROM product_variants pv
WHERE pv.product_id = 9 AND pv.item_code = 'PK1';

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p5_set, 'SUBMIT', 4, '2026-03-24 08:50:00'),
(@p5_set, 'APPROVE', 1, '2026-03-24 09:00:00'),
(@p5_set, 'RECEIVE', 7, '2026-03-24 10:00:00');

-- Gợi ý test:
-- Nhận thêm 15 chiếc → đủ 40 → hoàn tất
-- Hoặc hoàn tất ngay → inventory nhận 25 (chỉ tổng đã nhận)


-- =====================================================
-- PATH 6A: EDIT REJECTED REQUEST SET
-- Phiếu bị từ chối, cần sửa và gửi lại
-- Test: Login thuy → vào phiếu → sửa SL → gửi lại
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P6A: Xuất vải - Bị từ chối', 'REJECTED', 4, '2026-03-24 09:00:00');
SET @p6a_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p6a_set, 1, 8, 'OUT', 1, '2026-03-24 09:00:00');
SET @p6a_req = LAST_INSERT_ID();

-- Xuất 200 mét B1 (quá nhiều → bị reject)
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p6a_req, pv.variant_id, 200.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code = 'B1';

INSERT INTO approval_history (set_id, action, performed_by, reason, created_at) VALUES
(@p6a_set, 'SUBMIT', 4, NULL, '2026-03-24 09:00:00'),
(@p6a_set, 'REJECT', 1, 'Số lượng quá lớn, giảm xuống 50', '2026-03-24 09:30:00');

-- Gợi ý test:
-- Login thuy → Edit → sửa 200 → 50 → gửi lại → PENDING


-- =====================================================
-- PATH 6B: EDIT APPROVED REQUEST SET
-- Phiếu đã duyệt, owner muốn sửa lại (→ PENDING)
-- Test: Login thuy → "Sửa phiếu" → sửa SL → gửi → PENDING
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P6B: Nhập vải - Đã duyệt cần sửa', 'APPROVED', 4, '2026-03-24 09:10:00');
SET @p6b_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p6b_set, 1, 8, 'IN', 1, '2026-03-24 09:10:00');
SET @p6b_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p6b_req, pv.variant_id,
  CASE pv.item_code WHEN 'B1' THEN 30.00 WHEN 'B7' THEN 20.00 END
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B7');

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p6b_set, 'SUBMIT', 4, '2026-03-24 09:10:00'),
(@p6b_set, 'APPROVE', 1, '2026-03-24 09:30:00');

-- Gợi ý test:
-- Login thuy → "Sửa phiếu" → sửa → status → PENDING


-- =====================================================
-- PATH 7: EDIT & RECEIVE (STOCKKEEPER)
-- Phiếu APPROVED, STOCKKEEPER muốn sửa SL và nhận luôn
-- Test: Login khoa → "Sửa SL & Nhận hàng" → sửa → nhận
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P7: Nhập vải - Sửa SL & Nhận', 'APPROVED', 4, '2026-03-24 09:20:00');
SET @p7_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p7_set, 1, 8, 'IN', 1, '2026-03-24 09:20:00');
SET @p7_req = LAST_INSERT_ID();

-- Đề xuất nhập 100, nhưng STOCKKEEPER sẽ sửa xuống 80 và nhận luôn
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p7_req, pv.variant_id,
  CASE pv.item_code WHEN 'B1' THEN 100.00 WHEN 'B12' THEN 50.00 END
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code IN ('B1', 'B12');

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p7_set, 'SUBMIT', 4, '2026-03-24 09:20:00'),
(@p7_set, 'APPROVE', 1, '2026-03-24 09:40:00');

-- Gợi ý test:
-- Login khoa → mở phiếu → "Sửa SL & Nhận hàng"
-- Sửa B1: 100 → 80, B12: 50 → 45 → nhận


-- =====================================================
-- PATH 8A: UPDATE EXPECTED DATE
-- Phiếu ADJUST_IN chờ duyệt, cần đổi ngày dự kiến
-- Test: Login thuy → đổi ngày → kiểm tra
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P8A: Dự kiến nhập vải - Đổi ngày', 'APPROVED', 4, '2026-03-24 09:30:00');
SET @p8a_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, expected_date, warehouse_id, created_at) VALUES
(@p8a_set, 1, 8, 'ADJUST_IN', '2026-04-15', 1, '2026-03-24 09:30:00');
SET @p8a_req1 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p8a_req1, pv.variant_id, 25.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code = 'B1';

-- Thêm ADJUST_OUT phụ thuộc (cùng ngày hoặc sau)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, expected_date, warehouse_id, created_at) VALUES
(@p8a_set, 1, 8, 'ADJUST_OUT', '2026-04-20', 1, '2026-03-24 09:30:00');
SET @p8a_req2 = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p8a_req2, pv.variant_id, 10.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code = 'B1';

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p8a_set, 'SUBMIT', 4, '2026-03-24 09:30:00'),
(@p8a_set, 'APPROVE', 1, '2026-03-24 09:50:00');

-- Gợi ý test:
-- Đổi ADJUST_IN sang 2026-04-25 → EXPECT FAIL (ADJUST_OUT vẫn 04-20)
-- Đổi ADJUST_OUT sang 2026-04-30 trước → OK
-- Rồi đổi ADJUST_IN sang 2026-04-25 → OK


-- =====================================================
-- PATH 9: DELETE REQUEST SET (PENDING/REJECTED)
-- Test: Login thuy → xóa phiếu PENDING
-- =====================================================
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P9: Phiếu cần xóa - PENDING', 'PENDING', 4, '2026-03-24 09:40:00');
SET @p9a_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p9a_set, 1, 8, 'IN', 1, '2026-03-24 09:40:00');
SET @p9a_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p9a_req, pv.variant_id, 10.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code = 'B1';

INSERT INTO approval_history (set_id, action, performed_by, created_at) VALUES
(@p9a_set, 'SUBMIT', 4, '2026-03-24 09:40:00');

-- Thêm 1 phiếu REJECTED để test xóa
INSERT INTO request_sets (set_name, status, created_by, created_at) VALUES
('TEST-P9: Phiếu cần xóa - REJECTED', 'REJECTED', 4, '2026-03-24 09:45:00');
SET @p9b_set = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, warehouse_id, created_at) VALUES
(@p9b_set, 1, 8, 'IN', 1, '2026-03-24 09:45:00');
SET @p9b_req = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @p9b_req, pv.variant_id, 5.00
FROM product_variants pv
WHERE pv.product_id = 8 AND pv.item_code = 'B7';

INSERT INTO approval_history (set_id, action, performed_by, reason, created_at) VALUES
(@p9b_set, 'SUBMIT', 4, NULL, '2026-03-24 09:45:00'),
(@p9b_set, 'REJECT', 1, 'Sai đơn vị', '2026-03-24 10:00:00');

-- Gợi ý test:
-- Login thuy → xóa PENDING → OK
-- Login thuy → xóa REJECTED → OK
-- Login khoa (STOCKKEEPER) → xóa → EXPECT FAIL (không phải owner)


-- =====================================================
-- PATH 10: DELETE ALL REQUEST SETS (ADMIN only)
-- ⚠️ NGUY HIỂM: Xóa toàn bộ!
-- Test: Login hoi (ADMIN) → thử xóa tất cả
-- Lưu ý: Chỉ test khi backup xong!
-- =====================================================
-- Không cần data riêng. Dùng nút "Xóa tất cả" trong UI.
-- Expected: Xóa receipt_items → receipt_records → approval_history
--           → request_items → requests → sets


-- =====================================================
-- CHECKLIST KIỂM TRA
-- =====================================================
-- Trước khi test, chạy query này để xem tồn kho ban đầu:
--
-- SELECT p.product_name, pv.item_code, pv.item_name,
--   COALESCE(SUM(CASE WHEN ir.request_type='IN' THEN iri.quantity ELSE -iri.quantity END), 0) AS actual_qty,
--   w.warehouse_name
-- FROM inventory_request_items iri
-- JOIN inventory_requests ir ON ir.request_id = iri.request_id
-- JOIN request_sets rs ON rs.set_id = ir.set_id
-- JOIN product_variants pv ON pv.variant_id = iri.variant_id
-- JOIN products p ON p.product_id = ir.product_id
-- JOIN warehouses w ON w.warehouse_id = ir.warehouse_id
-- WHERE rs.status = 'EXECUTED'
-- GROUP BY p.product_name, pv.item_code, pv.item_name, w.warehouse_name
-- ORDER BY p.product_name, pv.item_code;
--
-- =====================================================
-- ACCOUNT TEST:
-- | Username | Password | Roles                       |
-- |----------|----------|-----------------------------|
-- | hoi      | password | ADMIN, USER                 |
-- | thuy     | password | PURCHASER, USER, PRODUCTION |
-- | khoa     | password | STOCKKEEPER                 |
-- | thanh    | password | ADMIN, USER, STOCKKEEPER    |
-- =====================================================
