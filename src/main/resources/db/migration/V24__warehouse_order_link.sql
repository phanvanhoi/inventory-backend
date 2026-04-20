-- =============================================================
-- V24: Warehouse Order Link (G6, W15) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G6
--
-- ⚠️ DANGER ZONE: This migration touches tables used by the existing
-- RequestSet flow (receipt_records, receipt_items). To avoid breaking
-- anything:
--   - NEW columns are nullable
--   - NO changes to existing columns
--   - FKs added with ON DELETE SET NULL
--   - Existing data: order_item_id = NULL, tailor_assignment_id = NULL
--
-- Purpose: when stockkeeper receives finished goods from tailor, link the
-- receipt back to the originating OrderItem (and optionally the specific
-- TailorAssignment batch) so Order detail shows the warehouse inflow.
-- =============================================================

-- receipt_records.order_item_id: which OrderItem this receipt is for
ALTER TABLE receipt_records
    ADD COLUMN order_item_id BIGINT NULL,
    ADD CONSTRAINT fk_receipt_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
        ON DELETE SET NULL,
    ADD INDEX idx_receipt_order_item (order_item_id);

-- receipt_items.tailor_assignment_id: which tailor batch these items came from
ALTER TABLE receipt_items
    ADD COLUMN tailor_assignment_id BIGINT NULL,
    ADD CONSTRAINT fk_receipt_item_assignment
        FOREIGN KEY (tailor_assignment_id) REFERENCES tailor_assignments(assignment_id)
        ON DELETE SET NULL,
    ADD INDEX idx_ri_assignment (tailor_assignment_id);
