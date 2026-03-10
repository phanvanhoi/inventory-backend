-- =====================================================
-- V4: Parent-Child Products
-- Hỗ trợ sản phẩm cha (SP2 = SƠ MI NAM 2026) chứa
-- nhiều sản phẩm con (SP1 = Trắng kem BD, SP11 = Viễn Thông, ...)
-- Mỗi child product có 88 variants riêng (4 styles × 11 sizes × 2 lengths)
-- =====================================================

-- 1. Thêm cột parent_product_id vào products
ALTER TABLE products ADD COLUMN parent_product_id BIGINT NULL;
ALTER TABLE products ADD CONSTRAINT fk_product_parent
    FOREIGN KEY (parent_product_id) REFERENCES products(product_id);
ALTER TABLE products ADD INDEX idx_parent_product (parent_product_id);

-- 2. SP1 trở thành child của SP2
UPDATE products SET parent_product_id = 2 WHERE product_id = 1;

-- 3. Stored procedure: Tạo child product + clone 88 variants từ sibling
DELIMITER //

DROP PROCEDURE IF EXISTS create_child_product//
CREATE PROCEDURE create_child_product(
    IN p_parent_id BIGINT,
    IN p_product_name VARCHAR(255),
    IN p_note TEXT
)
BEGIN
    DECLARE v_new_product_id BIGINT;
    DECLARE v_sibling_id BIGINT;

    -- Tìm sibling đầu tiên (child cùng parent đã có variants)
    SELECT p.product_id INTO v_sibling_id
    FROM products p
    WHERE p.parent_product_id = p_parent_id
      AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.product_id)
    LIMIT 1;

    IF v_sibling_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No sibling with variants found for cloning';
    END IF;

    -- Tạo product mới
    INSERT INTO products (product_name, variant_type, note, parent_product_id, created_at)
    SELECT p_product_name, 'STRUCTURED', p_note, p_parent_id, NOW()
    FROM DUAL;

    SET v_new_product_id = LAST_INSERT_ID();

    -- Clone variants từ sibling (thay product_id, giữ nguyên style/size/length/gender)
    INSERT INTO product_variants (product_id, style_id, size_id, length_type_id, gender)
    SELECT v_new_product_id, pv.style_id, pv.size_id, pv.length_type_id, pv.gender
    FROM product_variants pv
    WHERE pv.product_id = v_sibling_id;

    SELECT v_new_product_id AS new_product_id;
END//

DELIMITER ;
