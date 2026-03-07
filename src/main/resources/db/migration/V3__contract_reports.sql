-- =====================================================
-- V3: Contract Reports Module
-- Workflow: SALES -> MEASUREMENT -> PRODUCTION -> STOCKKEEPER -> COMPLETED
-- =====================================================

CREATE TABLE contract_reports (
    report_id               BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Phase workflow
    current_phase ENUM('SALES_INPUT','MEASUREMENT_INPUT','PRODUCTION_INPUT','STOCKKEEPER_INPUT','COMPLETED')
                  NOT NULL DEFAULT 'SALES_INPUT',

    -- === SALES fields ===
    unit_id                 BIGINT NOT NULL,
    unit_type               VARCHAR(30),         -- BUU_DIEN, VIEN_THONG, KHAC
    contract_year           INT,                 -- Nam hop dong
    sales_person            VARCHAR(100),
    expected_delivery_date  DATE,
    finalized_list_sent_date     DATE,
    finalized_list_received_date DATE,
    delivery_method          VARCHAR(50),
    extra_payment_date       DATE,
    extra_payment_amount     DECIMAL(15,0) DEFAULT 0,
    note                     TEXT,

    -- === MEASUREMENT fields ===
    measurement_start         DATE,
    measurement_end           DATE,
    technician_name           VARCHAR(100),
    measurement_received_date DATE,
    measurement_handler       VARCHAR(100),
    skip_measurement          BOOLEAN DEFAULT FALSE,
    production_handover_date  DATE,

    -- === PRODUCTION fields ===
    packing_return_date      DATE,
    tailor_start_date        DATE,
    tailor_expected_return    DATE,
    tailor_actual_return      DATE,

    -- === STOCKKEEPER fields ===
    actual_shipping_date     DATE,

    -- Metadata
    created_by  BIGINT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Lich su chinh sua (field-level audit trail)
CREATE TABLE contract_report_history (
    history_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id   BIGINT NOT NULL,
    changed_by  BIGINT NOT NULL,
    changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action      VARCHAR(30) NOT NULL,       -- EDIT, ADVANCE, RETURN
    field_name  VARCHAR(50),                -- NULL for ADVANCE/RETURN
    old_value   TEXT,
    new_value   TEXT,
    reason      TEXT,                       -- Ly do tra lai (RETURN)

    FOREIGN KEY (report_id) REFERENCES contract_reports(report_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id),
    INDEX idx_crh_report (report_id),
    INDEX idx_crh_changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
