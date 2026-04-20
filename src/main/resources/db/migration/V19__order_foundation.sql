-- =============================================================
-- V19: Order Foundation — Lark Integration
-- Decision 2026-04-18 (A1): Order root, ContractReport = view
-- Migrate: contract_reports → orders, contract_report_history → order_history
-- Safe: test data only, OK to DROP old tables
-- Reference: docs/lark-integration-roadmap.md §G1
-- =============================================================

-- -------------------------------------------------------------
-- STEP 1: customers
-- -------------------------------------------------------------
CREATE TABLE customers (
    customer_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id             BIGINT NOT NULL,
    parent_customer_id  BIGINT NULL,
    tax_code            VARCHAR(20),
    signer_name         VARCHAR(255),
    customer_type       ENUM('TRADITIONAL','NEW') NOT NULL DEFAULT 'NEW',
    province            VARCHAR(100),
    contract_year       INT,
    note                TEXT,

    seed_source         VARCHAR(50) NULL,
    lark_legacy_id      VARCHAR(50) NULL,

    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME NULL,

    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (parent_customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL,
    INDEX idx_cust_unit (unit_id),
    INDEX idx_cust_year (contract_year),
    INDEX idx_cust_seed (seed_source),
    INDEX idx_cust_parent (parent_customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- STEP 2: orders (replacement của contract_reports)
-- -------------------------------------------------------------
CREATE TABLE orders (
    order_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code             VARCHAR(100) UNIQUE,
    customer_id            BIGINT NOT NULL,

    status ENUM('NEW','NEGOTIATION','CONTRACT_SIGNED','DESIGNING',
                'MEASURING','PRODUCING','QC','PACKING',
                'DELIVERED','SUCCESS','LIQUIDATED','CANCELLED')
           NOT NULL DEFAULT 'NEW',

    current_phase ENUM('SALES_INPUT','MEASUREMENT_INPUT','PRODUCTION_INPUT',
                       'STOCKKEEPER_INPUT','COMPLETED')
           NOT NULL DEFAULT 'SALES_INPUT',

    sales_person_user_id   BIGINT NULL,
    sales_person_name      VARCHAR(100) NULL,

    unit_type              VARCHAR(30),
    contract_year          INT,

    total_before_vat       DECIMAL(18,2) DEFAULT 0,
    vat_amount             DECIMAL(18,2) DEFAULT 0,
    total_after_vat        DECIMAL(18,2) DEFAULT 0,

    -- Phase: SALES
    expected_delivery_date       DATE,
    finalized_list_sent_date     DATE,
    finalized_list_received_date DATE,
    delivery_method              VARCHAR(50),
    extra_payment_date           DATE,
    extra_payment_amount         DECIMAL(15,0) DEFAULT 0,

    -- Phase: MEASUREMENT
    measurement_start              DATE,
    measurement_end                DATE,
    technician_name                VARCHAR(100),
    measurement_received_date      DATE,
    measurement_handler            VARCHAR(100),
    skip_measurement               BOOLEAN DEFAULT FALSE,
    production_handover_date       DATE,

    -- Phase: PRODUCTION
    tailor_start_date         DATE,
    tailor_expected_return    DATE,
    tailor_actual_return      DATE,
    packing_return_date       DATE,

    -- Phase: STOCKKEEPER
    actual_shipping_date      DATE,

    -- Flags
    skip_design               BOOLEAN DEFAULT TRUE,
    design_ready              BOOLEAN DEFAULT FALSE,
    skip_kcs                  BOOLEAN DEFAULT TRUE,
    qc_passed                 BOOLEAN DEFAULT FALSE,
    has_repair                BOOLEAN DEFAULT FALSE,
    cancelled                 BOOLEAN DEFAULT FALSE,

    note                      TEXT,

    -- Migration tracking
    legacy_report_id  BIGINT NULL,
    seed_source       VARCHAR(50) NULL,
    lark_legacy_id    VARCHAR(50) NULL,

    -- Metadata
    created_by   BIGINT,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME NULL,

    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (sales_person_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status),
    INDEX idx_order_phase (current_phase),
    INDEX idx_order_legacy (legacy_report_id),
    INDEX idx_order_seed (seed_source),
    INDEX idx_order_sales_person (sales_person_user_id),
    UNIQUE KEY uk_legacy_report (legacy_report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- STEP 3: order_items
-- -------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id           BIGINT NOT NULL,
    product_id         BIGINT NULL,
    product_name       VARCHAR(255),

    qty_contract       INT NOT NULL DEFAULT 0,
    qty_settlement     INT NULL,
    unit_price         DECIMAL(18,2) DEFAULT 0,

    amount_contract    DECIMAL(18,2) GENERATED ALWAYS AS (qty_contract * unit_price) STORED,
    amount_settlement  DECIMAL(18,2) GENERATED ALWAYS AS (COALESCE(qty_settlement, 0) * unit_price) STORED,

    note               TEXT,

    seed_source        VARCHAR(50) NULL,
    lark_legacy_id     VARCHAR(50) NULL,

    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME NULL,

    FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL,
    INDEX idx_oi_order (order_id),
    INDEX idx_oi_product (product_id),
    INDEX idx_oi_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- STEP 4: order_history (refactor từ contract_report_history)
-- -------------------------------------------------------------
CREATE TABLE order_history (
    history_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    changed_by  BIGINT NOT NULL,
    changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action      VARCHAR(30) NOT NULL,
    field_name  VARCHAR(100),
    old_value   TEXT,
    new_value   TEXT,
    reason      TEXT,

    FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id),
    INDEX idx_oh_order (order_id),
    INDEX idx_oh_changed_by (changed_by),
    INDEX idx_oh_action (action),
    INDEX idx_oh_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================
-- DATA MIGRATION: contract_reports → orders
-- =============================================================

-- STEP 5: Tạo customers từ distinct (unit_id, contract_year) của contract_reports
INSERT INTO customers (unit_id, contract_year, customer_type, seed_source, created_at)
SELECT DISTINCT
    cr.unit_id,
    cr.contract_year,
    'TRADITIONAL',
    'MIGRATED_FROM_CR',
    NOW()
FROM contract_reports cr
WHERE cr.unit_id IS NOT NULL;

-- STEP 6: Copy contract_reports → orders
INSERT INTO orders (
    order_code, customer_id, status, current_phase,
    sales_person_user_id, sales_person_name,
    unit_type, contract_year,
    expected_delivery_date, finalized_list_sent_date, finalized_list_received_date,
    delivery_method, extra_payment_date, extra_payment_amount,
    measurement_start, measurement_end, technician_name, measurement_received_date,
    measurement_handler, skip_measurement, production_handover_date,
    tailor_start_date, tailor_expected_return, tailor_actual_return, packing_return_date,
    actual_shipping_date,
    note, legacy_report_id, seed_source,
    created_by, created_at, updated_at
)
SELECT
    CONCAT('MIG-', cr.report_id),
    c.customer_id,
    CASE cr.current_phase
        WHEN 'SALES_INPUT'       THEN 'NEW'
        WHEN 'MEASUREMENT_INPUT' THEN 'MEASURING'
        WHEN 'PRODUCTION_INPUT'  THEN 'PRODUCING'
        WHEN 'STOCKKEEPER_INPUT' THEN 'PACKING'
        WHEN 'COMPLETED'         THEN 'SUCCESS'
    END,
    cr.current_phase,
    (SELECT u.user_id FROM users u WHERE u.full_name = cr.sales_person LIMIT 1),
    cr.sales_person,
    cr.unit_type, cr.contract_year,
    cr.expected_delivery_date, cr.finalized_list_sent_date, cr.finalized_list_received_date,
    cr.delivery_method, cr.extra_payment_date, cr.extra_payment_amount,
    cr.measurement_start, cr.measurement_end, cr.technician_name, cr.measurement_received_date,
    cr.measurement_handler, cr.skip_measurement, cr.production_handover_date,
    cr.tailor_start_date, cr.tailor_expected_return, cr.tailor_actual_return, cr.packing_return_date,
    cr.actual_shipping_date,
    cr.note, cr.report_id, 'MIGRATED_FROM_CR',
    cr.created_by, cr.created_at, cr.updated_at
FROM contract_reports cr
JOIN customers c
  ON c.unit_id = cr.unit_id
 AND ((c.contract_year = cr.contract_year) OR (c.contract_year IS NULL AND cr.contract_year IS NULL))
 AND c.seed_source = 'MIGRATED_FROM_CR';

-- STEP 7: Copy contract_report_history → order_history
INSERT INTO order_history (order_id, changed_by, changed_at, action, field_name, old_value, new_value, reason)
SELECT
    o.order_id,
    crh.changed_by, crh.changed_at,
    crh.action, crh.field_name, crh.old_value, crh.new_value, crh.reason
FROM contract_report_history crh
JOIN orders o ON o.legacy_report_id = crh.report_id;

-- =============================================================
-- STEP 8: Drop tables cũ
-- Decision 2026-04-18: user confirmed test data, OK to drop
-- =============================================================
DROP TABLE IF EXISTS contract_report_history;
DROP TABLE IF EXISTS contract_reports;

-- =============================================================
-- STEP 9: Seed 4 role mới (A2 decision)
-- =============================================================
INSERT INTO roles (role_name) VALUES ('DESIGNER'),('KCS'),('PACKER'),('REPAIRER')
  ON DUPLICATE KEY UPDATE role_name = role_name;
