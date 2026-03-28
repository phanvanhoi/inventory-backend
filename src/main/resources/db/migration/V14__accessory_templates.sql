-- V14: Accessory Templates (BOM) cho ĐX PHỤ LIỆU
CREATE TABLE accessory_templates (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)     NOT NULL,
    created_by    BIGINT           NULL,
    created_at    DATETIME         DEFAULT CURRENT_TIMESTAMP,
    deleted_at    DATETIME         NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE accessory_template_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id  BIGINT           NOT NULL,
    variant_id   BIGINT           NULL,
    item_code    VARCHAR(50)      NULL,
    item_name    VARCHAR(255)     NOT NULL,
    rate         DECIMAL(10,4)    NOT NULL,
    unit         VARCHAR(50)      NULL,
    sort_order   INT              DEFAULT 0,
    FOREIGN KEY (template_id) REFERENCES accessory_templates(id),
    FOREIGN KEY (variant_id)  REFERENCES product_variants(variant_id)
);

ALTER TABLE inventory_request_items
    ADD COLUMN rate DECIMAL(10,4) NULL;
