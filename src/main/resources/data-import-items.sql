-- =====================================================
-- IMPORT INVENTORY REQUEST ITEMS
-- Dữ liệu chi tiết từng item theo variant
-- =====================================================

-- =====================================================
-- HELPER: Tạo procedure để insert items dễ dàng hơn
-- =====================================================

DELIMITER //

DROP PROCEDURE IF EXISTS insert_item_by_variant//
CREATE PROCEDURE insert_item_by_variant(
    IN p_request_id BIGINT,
    IN p_style_name VARCHAR(50),
    IN p_size_value INT,
    IN p_length_code VARCHAR(10),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;

    IF p_quantity > 0 THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        JOIN styles s ON s.style_id = pv.style_id
        JOIN sizes sz ON sz.size_id = pv.size_id
        JOIN length_types lt ON lt.length_type_id = pv.length_type_id
        WHERE s.style_name = p_style_name
          AND sz.size_value = p_size_value
          AND lt.code = p_length_code;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (p_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

DELIMITER ;

-- =====================================================
-- REQUEST 1: Nhập kho ban đầu (20/06/2025) - Kho
-- =====================================================
-- CỔ ĐIỂN
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'Dài', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'Cộc', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'Dài', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'Cộc', 17);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'Dài', 22);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 38, 'Cộc', 26);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 38, 'Dài', 24);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 39, 'Cộc', 26);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 39, 'Dài', 4);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'Cộc', 26);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'Dài', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 41, 'Cộc', 22);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 41, 'Dài', 19);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'Cộc', 17);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'Dài', 11);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'Cộc', 9);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'Dài', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'Cộc', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'Dài', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 45, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 45, 'Dài', 0);
-- CỔ ĐIỂN NGẮN
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'Dài', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 13);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 14);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 14);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 11);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 11);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'Dài', 9);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'Cộc', 17);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'Dài', 9);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'Cộc', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'Dài', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'Dài', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'Dài', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 44, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 44, 'Dài', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 45, 'Cộc', 0);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 45, 'Dài', 0);
-- SLIM
CALL insert_item_by_variant(1, 'SLIM', 35, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM', 35, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM', 36, 'Cộc', 17);
CALL insert_item_by_variant(1, 'SLIM', 36, 'Dài', 23);
CALL insert_item_by_variant(1, 'SLIM', 37, 'Cộc', 20);
CALL insert_item_by_variant(1, 'SLIM', 37, 'Dài', 22);
CALL insert_item_by_variant(1, 'SLIM', 38, 'Cộc', 21);
CALL insert_item_by_variant(1, 'SLIM', 38, 'Dài', 19);
CALL insert_item_by_variant(1, 'SLIM', 39, 'Cộc', 20);
CALL insert_item_by_variant(1, 'SLIM', 39, 'Dài', 22);
CALL insert_item_by_variant(1, 'SLIM', 40, 'Cộc', 17);
CALL insert_item_by_variant(1, 'SLIM', 40, 'Dài', 15);
CALL insert_item_by_variant(1, 'SLIM', 41, 'Cộc', 22);
CALL insert_item_by_variant(1, 'SLIM', 41, 'Dài', 22);
CALL insert_item_by_variant(1, 'SLIM', 42, 'Cộc', 3);
CALL insert_item_by_variant(1, 'SLIM', 42, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM', 43, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM', 43, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM', 44, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM', 44, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM', 45, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM', 45, 'Dài', 0);
-- SLIM Ngắn
CALL insert_item_by_variant(1, 'SLIM Ngắn', 35, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 35, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 36, 'Cộc', 15);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 36, 'Dài', 17);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'Cộc', 15);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'Dài', 10);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'Cộc', 12);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'Dài', 17);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'Cộc', 18);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'Dài', 21);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'Cộc', 13);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'Dài', 8);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'Cộc', 9);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'Dài', 10);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'Cộc', 1);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 43, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 43, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 44, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 44, 'Dài', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 45, 'Cộc', 0);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 45, 'Dài', 0);

-- =====================================================
-- REQUEST 2: ĐX 48 - Thúy - Bưu điện Vĩnh Phúc (01/07/2025)
-- =====================================================
CALL insert_item_by_variant(2, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(2, 'CỔ ĐIỂN', 39, 'Cộc', 2);
CALL insert_item_by_variant(2, 'CỔ ĐIỂN', 39, 'Dài', 2);
CALL insert_item_by_variant(2, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(2, 'SLIM Ngắn', 39, 'Cộc', 1);

-- =====================================================
-- REQUEST 3: ĐX 48 - Thúy - Bưu điện Hải Dương (01/07/2025)
-- =====================================================
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 37, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 37, 'Dài', 2);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 39, 'Cộc', 2);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 39, 'Dài', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 2);
CALL insert_item_by_variant(3, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 1);
CALL insert_item_by_variant(3, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(3, 'SLIM', 37, 'Cộc', 2);
CALL insert_item_by_variant(3, 'SLIM', 37, 'Dài', 1);
CALL insert_item_by_variant(3, 'SLIM Ngắn', 37, 'Cộc', 1);

-- =====================================================
-- REQUEST 4: ĐX 55 - Thúy - Bưu điện Bến Tre (10/07/2025)
-- =====================================================
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 36, 'Dài', 1);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 37, 'Cộc', 4);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 38, 'Cộc', 2);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 40, 'Dài', 1);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN', 41, 'Dài', 1);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 1);
CALL insert_item_by_variant(4, 'CỔ ĐIỂN NGẮN', 39, 'Dài', 1);
CALL insert_item_by_variant(4, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(4, 'SLIM', 36, 'Dài', 1);

-- =====================================================
-- REQUEST 5: ĐX 6 - Nga - Nhập (05/07/2025)
-- =====================================================
CALL insert_item_by_variant(5, 'SLIM', 36, 'Cộc', 1);

-- =====================================================
-- REQUEST 6: ĐX 6 - Nga - Xuất (05/07/2025)
-- =====================================================
CALL insert_item_by_variant(6, 'CỔ ĐIỂN', 39, 'Dài', 1);

-- =====================================================
-- REQUEST 7: ĐX 7 - Nga - Nhập (07/07/2025)
-- =====================================================
CALL insert_item_by_variant(7, 'CỔ ĐIỂN', 39, 'Dài', 4);
CALL insert_item_by_variant(7, 'SLIM', 36, 'Cộc', 2);
CALL insert_item_by_variant(7, 'SLIM Ngắn', 38, 'Dài', 1);

-- =====================================================
-- REQUEST 8: ĐX 7 - Nga - Xuất (07/07/2025)
-- =====================================================
CALL insert_item_by_variant(8, 'CỔ ĐIỂN', 38, 'Dài', 3);
CALL insert_item_by_variant(8, 'CỔ ĐIỂN', 40, 'Dài', 1);
CALL insert_item_by_variant(8, 'CỔ ĐIỂN NGẮN', 39, 'Dài', 1);
CALL insert_item_by_variant(8, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(8, 'SLIM', 37, 'Dài', 1);

-- =====================================================
-- REQUEST 9: Đơn đặt hàng 18 - Sài Đồng - Nhập (23/07/2025)
-- =====================================================
CALL insert_item_by_variant(9, 'CỔ ĐIỂN', 37, 'Cộc', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN', 38, 'Cộc', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN', 39, 'Cộc', 25);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN', 40, 'Cộc', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN', 41, 'Cộc', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN NGẮN', 39, 'Dài', 10);
CALL insert_item_by_variant(9, 'CỔ ĐIỂN NGẮN', 40, 'Cộc', 10);
CALL insert_item_by_variant(9, 'SLIM', 37, 'Cộc', 10);
CALL insert_item_by_variant(9, 'SLIM', 37, 'Dài', 10);
CALL insert_item_by_variant(9, 'SLIM', 39, 'Cộc', 10);
CALL insert_item_by_variant(9, 'SLIM', 39, 'Dài', 15);
CALL insert_item_by_variant(9, 'SLIM Ngắn', 37, 'Cộc', 10);
CALL insert_item_by_variant(9, 'SLIM Ngắn', 38, 'Cộc', 10);
CALL insert_item_by_variant(9, 'SLIM Ngắn', 38, 'Dài', 10);

-- =====================================================
-- REQUEST 10: ĐX 60 - Thúy - Bưu điện Lào Cai (10/07/2025)
-- =====================================================
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 40, 'Dài', 1);
CALL insert_item_by_variant(10, 'CỔ ĐIỂN', 41, 'Cộc', 3);
CALL insert_item_by_variant(10, 'SLIM', 36, 'Cộc', 2);
CALL insert_item_by_variant(10, 'SLIM', 37, 'Cộc', 2);
CALL insert_item_by_variant(10, 'SLIM Ngắn', 37, 'Dài', 1);

-- =====================================================
-- REQUEST 11: ĐX 60 - Thúy - Bưu điện Hưng Yên (10/07/2025)
-- =====================================================
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 37, 'Cộc', 1);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 37, 'Dài', 2);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 38, 'Cộc', 3);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN', 41, 'Dài', 1);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 2);
CALL insert_item_by_variant(11, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 2);
CALL insert_item_by_variant(11, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(11, 'SLIM Ngắn', 37, 'Cộc', 1);

-- =====================================================
-- REQUEST 12: ĐX 60 - Thúy - Bưu điện Đông Anh (10/07/2025)
-- =====================================================
CALL insert_item_by_variant(12, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(12, 'CỔ ĐIỂN', 41, 'Dài', 1);
CALL insert_item_by_variant(12, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);

-- =====================================================
-- REQUEST 13: ĐX 60 - Thúy - Công ty Logistics (10/07/2025)
-- =====================================================
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 38, 'Dài', 3);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 39, 'Dài', 5);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 40, 'Dài', 6);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 42, 'Dài', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 43, 'Cộc', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN', 44, 'Dài', 2);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 1);
CALL insert_item_by_variant(13, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM', 36, 'Cộc', 4);
CALL insert_item_by_variant(13, 'SLIM', 37, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM', 37, 'Dài', 1);
CALL insert_item_by_variant(13, 'SLIM', 38, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM', 38, 'Dài', 3);
CALL insert_item_by_variant(13, 'SLIM', 39, 'Cộc', 2);
CALL insert_item_by_variant(13, 'SLIM', 40, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM Ngắn', 38, 'Cộc', 1);
CALL insert_item_by_variant(13, 'SLIM Ngắn', 39, 'Cộc', 3);
CALL insert_item_by_variant(13, 'SLIM Ngắn', 40, 'Cộc', 2);

-- =====================================================
-- REQUEST 14: ĐX 60 - Hương - TTKD Hà Nội + BĐ Gia Lai (12/07/2025)
-- =====================================================
CALL insert_item_by_variant(14, 'CỔ ĐIỂN', 39, 'Dài', 3);
CALL insert_item_by_variant(14, 'CỔ ĐIỂN', 40, 'Cộc', 3);

-- =====================================================
-- REQUEST 15: ĐX 63 - Thúy - BĐTT Sài Gòn - BĐ Hồ Chí Minh (19/07/2025)
-- =====================================================
CALL insert_item_by_variant(15, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(15, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 1);
CALL insert_item_by_variant(15, 'SLIM', 36, 'Cộc', 1);

-- =====================================================
-- REQUEST 16: ĐX 63 - Thúy - Bưu điện Nam Định (19/07/2025)
-- =====================================================
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 39, 'Cộc', 2);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 41, 'Dài', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 42, 'Dài', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN', 44, 'Cộc', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 1);
CALL insert_item_by_variant(16, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(16, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(16, 'SLIM', 37, 'Dài', 1);
CALL insert_item_by_variant(16, 'SLIM', 38, 'Cộc', 4);
CALL insert_item_by_variant(16, 'SLIM', 40, 'Cộc', 1);
CALL insert_item_by_variant(16, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(16, 'SLIM Ngắn', 38, 'Cộc', 1);

-- =====================================================
-- REQUEST 17: ĐX 63 - Thúy - Bưu điện Ninh Bình (19/07/2025)
-- =====================================================
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 40, 'Dài', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 1);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 2);
CALL insert_item_by_variant(17, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(17, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(17, 'SLIM', 38, 'Cộc', 1);

-- =====================================================
-- REQUEST 18: ĐX 63 - Thúy - Bưu điện Cầu Giấy (19/07/2025)
-- =====================================================
CALL insert_item_by_variant(18, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(18, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(18, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(18, 'CỔ ĐIỂN', 42, 'Cộc', 1);
CALL insert_item_by_variant(18, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(18, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(18, 'SLIM', 38, 'Cộc', 2);

-- =====================================================
-- REQUEST 19: ĐX 63 - Thúy - Bưu điện Lạng Sơn (19/07/2025)
-- =====================================================
CALL insert_item_by_variant(19, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN', 38, 'Cộc', 2);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN', 39, 'Cộc', 2);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(19, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 2);
CALL insert_item_by_variant(19, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(19, 'SLIM', 38, 'Cộc', 2);
CALL insert_item_by_variant(19, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(19, 'SLIM Ngắn', 37, 'Dài', 1);
CALL insert_item_by_variant(19, 'SLIM Ngắn', 38, 'Cộc', 2);

-- =====================================================
-- REQUEST 20: ĐX 65 - Thúy - Bưu điện Quảng Bình (29/07/2025)
-- =====================================================
CALL insert_item_by_variant(20, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(20, 'CỔ ĐIỂN', 38, 'Cộc', 3);
CALL insert_item_by_variant(20, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(20, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(20, 'CỔ ĐIỂN', 42, 'Dài', 1);
CALL insert_item_by_variant(20, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(20, 'SLIM', 37, 'Cộc', 1);
CALL insert_item_by_variant(20, 'SLIM', 38, 'Cộc', 2);
CALL insert_item_by_variant(20, 'SLIM', 38, 'Dài', 2);
CALL insert_item_by_variant(20, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(20, 'SLIM Ngắn', 38, 'Cộc', 2);
CALL insert_item_by_variant(20, 'SLIM Ngắn', 38, 'Dài', 1);
CALL insert_item_by_variant(20, 'SLIM Ngắn', 39, 'Cộc', 2);

-- =====================================================
-- REQUEST 21: ĐX 71 - Thúy - Bưu điện Chương Mỹ (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 37, 'Dài', 3);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 39, 'Dài', 1);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(21, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(21, 'SLIM', 38, 'Cộc', 1);
CALL insert_item_by_variant(21, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(21, 'SLIM Ngắn', 39, 'Cộc', 1);

-- =====================================================
-- REQUEST 22: ĐX 71 - Thúy - Bưu điện Hoàn Kiếm (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(22, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(22, 'CỔ ĐIỂN', 38, 'Cộc', 2);
CALL insert_item_by_variant(22, 'CỔ ĐIỂN', 41, 'Cộc', 3);
CALL insert_item_by_variant(22, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(22, 'SLIM', 37, 'Dài', 1);
CALL insert_item_by_variant(22, 'SLIM Ngắn', 40, 'Cộc', 2);

-- =====================================================
-- REQUEST 23: ĐX 71 - Thúy - Bưu điện Cần Thơ (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 37, 'Cộc', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 37, 'Dài', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 38, 'Cộc', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 38, 'Dài', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 40, 'Dài', 3);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 41, 'Cộc', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN', 42, 'Dài', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 2);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(23, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 1);
CALL insert_item_by_variant(23, 'SLIM', 37, 'Cộc', 1);
CALL insert_item_by_variant(23, 'SLIM', 39, 'Cộc', 2);
CALL insert_item_by_variant(23, 'SLIM Ngắn', 37, 'Cộc', 2);
CALL insert_item_by_variant(23, 'SLIM Ngắn', 38, 'Cộc', 1);
CALL insert_item_by_variant(23, 'SLIM Ngắn', 38, 'Dài', 1);
CALL insert_item_by_variant(23, 'SLIM Ngắn', 40, 'Cộc', 2);

-- =====================================================
-- REQUEST 24: ĐX 71 - Thúy - Bưu điện Long Biên (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(24, 'CỔ ĐIỂN', 39, 'Dài', 1);
CALL insert_item_by_variant(24, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(24, 'CỔ ĐIỂN', 40, 'Dài', 2);
CALL insert_item_by_variant(24, 'CỔ ĐIỂN', 43, 'Cộc', 1);
CALL insert_item_by_variant(24, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(24, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 2);

-- =====================================================
-- REQUEST 25: ĐX 71 - Thúy - Bưu điện Hà Đông (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(25, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(25, 'CỔ ĐIỂN', 39, 'Cộc', 3);
CALL insert_item_by_variant(25, 'CỔ ĐIỂN', 40, 'Cộc', 1);
CALL insert_item_by_variant(25, 'CỔ ĐIỂN', 42, 'Dài', 1);
CALL insert_item_by_variant(25, 'SLIM', 37, 'Cộc', 1);

-- =====================================================
-- REQUEST 26: ĐX 71 - Thúy - Kho vận (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 37, 'Cộc', 2);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 37, 'Dài', 2);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 38, 'Cộc', 3);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 38, 'Dài', 4);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 39, 'Cộc', 4);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 39, 'Dài', 2);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 40, 'Cộc', 4);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 41, 'Cộc', 5);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 41, 'Dài', 6);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 42, 'Cộc', 1);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 42, 'Dài', 3);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN', 43, 'Dài', 1);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 2);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 3);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 3);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 2);
CALL insert_item_by_variant(26, 'CỔ ĐIỂN NGẮN', 40, 'Cộc', 1);
CALL insert_item_by_variant(26, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(26, 'SLIM', 36, 'Dài', 2);
CALL insert_item_by_variant(26, 'SLIM', 37, 'Cộc', 2);
CALL insert_item_by_variant(26, 'SLIM', 37, 'Dài', 1);
CALL insert_item_by_variant(26, 'SLIM', 38, 'Cộc', 2);
CALL insert_item_by_variant(26, 'SLIM', 38, 'Dài', 3);
CALL insert_item_by_variant(26, 'SLIM', 39, 'Cộc', 4);
CALL insert_item_by_variant(26, 'SLIM', 39, 'Dài', 2);
CALL insert_item_by_variant(26, 'SLIM Ngắn', 37, 'Cộc', 1);
CALL insert_item_by_variant(26, 'SLIM Ngắn', 38, 'Cộc', 3);
CALL insert_item_by_variant(26, 'SLIM Ngắn', 38, 'Dài', 3);
CALL insert_item_by_variant(26, 'SLIM Ngắn', 39, 'Cộc', 5);

-- =====================================================
-- REQUEST 27: ĐX 71 - Thúy - Bưu điện Nam Định (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(27, 'SLIM Ngắn', 40, 'Cộc', 1);

-- =====================================================
-- REQUEST 28: ĐX 71 - Thúy - Bưu điện Cầu Giấy (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(28, 'SLIM', 37, 'Cộc', 2);

-- =====================================================
-- REQUEST 29: ĐX 71 - Thúy - Bưu điện Quảng Bình (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(29, 'SLIM', 38, 'Cộc', 1);
CALL insert_item_by_variant(29, 'SLIM', 38, 'Dài', 1);
CALL insert_item_by_variant(29, 'SLIM', 39, 'Cộc', 1);
CALL insert_item_by_variant(29, 'SLIM', 39, 'Dài', 1);

-- =====================================================
-- REQUEST 30: ĐX 73 - Thúy - Bưu điện Cà Mau (17/08/2025)
-- =====================================================
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 39, 'Cộc', 2);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 40, 'Cộc', 2);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 40, 'Dài', 4);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 42, 'Cộc', 1);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN', 43, 'Cộc', 1);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 2);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 1);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(30, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 1);
CALL insert_item_by_variant(30, 'SLIM', 38, 'Cộc', 1);
CALL insert_item_by_variant(30, 'SLIM', 38, 'Dài', 1);
CALL insert_item_by_variant(30, 'SLIM', 39, 'Cộc', 2);
CALL insert_item_by_variant(30, 'SLIM Ngắn', 39, 'Cộc', 3);
CALL insert_item_by_variant(30, 'SLIM Ngắn', 39, 'Dài', 1);
CALL insert_item_by_variant(30, 'SLIM Ngắn', 40, 'Cộc', 1);

-- =====================================================
-- REQUEST 31: ĐX 73 - Thúy - Bưu điện Bình Phước (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(31, 'CỔ ĐIỂN', 37, 'Cộc', 1);
CALL insert_item_by_variant(31, 'CỔ ĐIỂN', 42, 'Cộc', 1);
CALL insert_item_by_variant(31, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 1);
CALL insert_item_by_variant(31, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(31, 'SLIM', 36, 'Cộc', 1);

-- =====================================================
-- REQUEST 32: ĐX 73 - Thúy - Bưu điện Quảng Trị (20/08/2025)
-- =====================================================
CALL insert_item_by_variant(32, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(32, 'CỔ ĐIỂN', 38, 'Dài', 1);
CALL insert_item_by_variant(32, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(32, 'CỔ ĐIỂN', 40, 'Cộc', 3);
CALL insert_item_by_variant(32, 'CỔ ĐIỂN', 41, 'Cộc', 1);
CALL insert_item_by_variant(32, 'SLIM', 36, 'Cộc', 1);
CALL insert_item_by_variant(32, 'SLIM', 38, 'Cộc', 1);

-- =====================================================
-- REQUEST 33: ĐX 73 - Thúy - Bưu điện Sơn La (18/08/2025)
-- =====================================================
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 37, 'Dài', 1);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 38, 'Cộc', 3);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 38, 'Dài', 2);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 39, 'Cộc', 1);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 40, 'Cộc', 3);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN', 43, 'Cộc', 1);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(33, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 1);
CALL insert_item_by_variant(33, 'SLIM', 36, 'Cộc', 2);
CALL insert_item_by_variant(33, 'SLIM', 36, 'Dài', 2);
CALL insert_item_by_variant(33, 'SLIM', 37, 'Cộc', 1);
CALL insert_item_by_variant(33, 'SLIM', 37, 'Dài', 2);
CALL insert_item_by_variant(33, 'SLIM', 38, 'Cộc', 2);
CALL insert_item_by_variant(33, 'SLIM', 38, 'Dài', 1);
CALL insert_item_by_variant(33, 'SLIM', 39, 'Cộc', 4);
CALL insert_item_by_variant(33, 'SLIM', 39, 'Dài', 2);
CALL insert_item_by_variant(33, 'SLIM Ngắn', 40, 'Cộc', 1);

-- =====================================================
-- REQUEST 34: ĐX 66 - Hương - BĐ Nghệ An (06/08/2025)
-- =====================================================
CALL insert_item_by_variant(34, 'CỔ ĐIỂN', 39, 'Dài', 1);
CALL insert_item_by_variant(34, 'CỔ ĐIỂN', 40, 'Cộc', 1);

-- =====================================================
-- REQUEST 35: ĐX 67 - Hương - Tct Bưu điện (12/08/2025)
-- =====================================================
CALL insert_item_by_variant(35, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 1);
CALL insert_item_by_variant(35, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 1);

-- =====================================================
-- REQUEST 36: ĐX 64 - Thương - Tcty Bưu điện (16/08/2025)
-- =====================================================
CALL insert_item_by_variant(36, 'CỔ ĐIỂN', 42, 'Cộc', 1);

-- =====================================================
-- REQUEST 37: Đơn đặt hàng 26 - Sài Đồng - Nhập (29/08/2025)
-- =====================================================
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 35, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 35, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 37, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 37, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 38, 'Cộc', 20);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 38, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 39, 'Cộc', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 39, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 40, 'Cộc', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 40, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 41, 'Cộc', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 42, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 43, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN', 44, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 35, 'Cộc', 5);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 35, 'Dài', 5);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 36, 'Cộc', 5);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 36, 'Dài', 5);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 37, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 37, 'Dài', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 38, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 38, 'Dài', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 39, 'Cộc', 15);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 39, 'Dài', 20);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 40, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 40, 'Dài', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 41, 'Cộc', 10);
CALL insert_item_by_variant(37, 'CỔ ĐIỂN NGẮN', 41, 'Dài', 9);
CALL insert_item_by_variant(37, 'SLIM', 36, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM', 37, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM', 38, 'Cộc', 15);
CALL insert_item_by_variant(37, 'SLIM', 39, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM', 39, 'Dài', 10);
CALL insert_item_by_variant(37, 'SLIM', 41, 'Cộc', 9);
CALL insert_item_by_variant(37, 'SLIM', 41, 'Dài', 9);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 36, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 36, 'Dài', 10);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 37, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 38, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 39, 'Cộc', 10);
CALL insert_item_by_variant(37, 'SLIM Ngắn', 39, 'Dài', 10);

-- =====================================================
-- Tiếp tục với các request còn lại...
-- Do giới hạn độ dài, tôi sẽ tạo file riêng cho phần còn lại
-- =====================================================

-- Cleanup procedure sau khi import xong
-- DROP PROCEDURE IF EXISTS insert_item_by_variant;
