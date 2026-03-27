-- V13: Add VAI_NHAP_KHO and PHU_LIEU_KHO_THO transfer categories
-- Backfill: old VAI_NHAP_KHO_THO records (simple IN) → VAI_NHAP_KHO

ALTER TABLE request_sets
  MODIFY COLUMN category ENUM(
    'VAI_NHAP_KHO',
    'VAI_NHAP_KHO_THO',
    'VAI_GIAO_THO',
    'VAI_TRA_KHACH',
    'PHU_LIEU',
    'PHU_LIEU_KHO_THO',
    'PHU_KIEN',
    'HANG_MAY_SAN'
  ) NULL;

UPDATE request_sets
SET category = 'VAI_NHAP_KHO'
WHERE category = 'VAI_NHAP_KHO_THO';
