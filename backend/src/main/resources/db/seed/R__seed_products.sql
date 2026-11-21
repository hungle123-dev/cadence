-- Sample products for the local profile only (application-local.yaml adds db/seed).
-- Each product exercises acceptance criteria of slice 1a (07-slice-1.md 7.3), noted above it.
-- Dates are relative to now() so sales never silently expire. Fixed ids keep re-runs harmless.
-- Id blocks: 8001 products, 8002 colorways, 8003 variants, 8004 images.

-- ---------- Products ----------

INSERT INTO catalog.products (id, slug, name, description, type, gender, sport, category_id, status, publish_at, specs, created_at, updated_at) VALUES
  -- Tempo 2: published, 3 colours; Đen on sale, size 41 of Đen sold out (AC 1.1, 1.5, 3.1–3.3)
  ('00000000-0000-7000-8001-000000000001', 'tempo-2', 'Cadence Tempo 2',
   'Giày chạy bộ hằng ngày, đệm êm, nhẹ, hợp cho cự ly 5–21 km.',
   'SHOES', 'UNISEX', 'chay-bo', '00000000-0000-7000-8000-000000000011',
   'PUBLISHED', now() - interval '10 days',
   '{"Trọng lượng": "245 g", "Độ dốc": "8 mm", "Đế giữa": "Foam EVA"}', now(), now()),
  -- Court Classic: published; its sale ended yesterday; Trắng sold out in every size (AC 1.6, 1.11, 3.4)
  ('00000000-0000-7000-8001-000000000002', 'court-classic', 'Court Classic',
   'Giày lifestyle cổ thấp, da tổng hợp, đế cao su màu gum.',
   'SHOES', 'UNISEX', 'lifestyle', '00000000-0000-7000-8000-000000000012',
   'PUBLISHED', now() - interval '20 days',
   '{"Chất liệu": "Da tổng hợp", "Đế": "Cao su"}', now(), now()),
  -- Canvas Low: scheduled 30 days ahead, must stay hidden (AC 1.3)
  ('00000000-0000-7000-8001-000000000003', 'canvas-low', 'Canvas Low',
   'Giày vải cổ thấp, sắp ra mắt.',
   'SHOES', 'UNISEX', 'lifestyle', '00000000-0000-7000-8000-000000000012',
   'SCHEDULED', now() + interval '30 days',
   '{"Chất liệu": "Vải canvas"}', now(), now()),
  -- Daily Canvas: draft, must stay hidden (AC 1.3, 3.8)
  ('00000000-0000-7000-8001-000000000004', 'daily-canvas', 'Daily Canvas',
   'Bản nháp, chưa bán.',
   'SHOES', 'UNISEX', 'lifestyle', '00000000-0000-7000-8000-000000000012',
   'DRAFT', NULL,
   '{}', now(), now()),
  -- Pace: women's running tee, sizes S/M/L (size_order), newest product; Đen on sale (AC 2.5, 2.6, 2.12, 3.6)
  ('00000000-0000-7000-8001-000000000005', 'pace-tee', 'Áo chạy bộ Pace',
   'Áo thun chạy bộ nữ, vải thoáng khí, nhanh khô.',
   'APPAREL', 'WOMEN', 'chay-bo', '00000000-0000-7000-8000-000000000022',
   'PUBLISHED', now() - interval '3 days',
   '{"Chất liệu": "100% polyester tái chế", "Kiểu dáng": "Ôm vừa"}', now(), now())
ON CONFLICT (id) DO NOTHING;

-- ---------- Colorways ----------

INSERT INTO catalog.colorways (id, product_id, slug, name, color_family, list_price, sale_price, sale_starts_at, sale_ends_at, position) VALUES
  -- Tempo 2
  ('00000000-0000-7000-8002-000000000001', '00000000-0000-7000-8001-000000000001', 'den',   'Đen',   'BLACK', 2490000, 1990000, now() - interval '1 day', now() + interval '30 days', 1),
  ('00000000-0000-7000-8002-000000000002', '00000000-0000-7000-8001-000000000001', 'trang', 'Trắng', 'WHITE', 2490000, NULL, NULL, NULL, 2),
  ('00000000-0000-7000-8002-000000000003', '00000000-0000-7000-8001-000000000001', 'xanh',  'Xanh',  'BLUE',  2490000, NULL, NULL, NULL, 3),
  -- Court Classic: the sale is over, list price applies
  ('00000000-0000-7000-8002-000000000004', '00000000-0000-7000-8001-000000000002', 'trang',   'Trắng',   'WHITE', 1890000, 1490000, now() - interval '30 days', now() - interval '1 day', 1),
  ('00000000-0000-7000-8002-000000000005', '00000000-0000-7000-8001-000000000002', 'xanh-reu', 'Xanh rêu', 'GREEN', 1890000, NULL, NULL, NULL, 2),
  -- Canvas Low
  ('00000000-0000-7000-8002-000000000006', '00000000-0000-7000-8001-000000000003', 'den', 'Đen', 'BLACK', 1290000, NULL, NULL, NULL, 1),
  -- Daily Canvas
  ('00000000-0000-7000-8002-000000000007', '00000000-0000-7000-8001-000000000004', 'be', 'Be', 'BEIGE', 990000, NULL, NULL, NULL, 1),
  -- Pace
  ('00000000-0000-7000-8002-000000000008', '00000000-0000-7000-8001-000000000005', 'hong', 'Hồng', 'PINK',  590000, NULL, NULL, NULL, 1),
  ('00000000-0000-7000-8002-000000000009', '00000000-0000-7000-8001-000000000005', 'den',  'Đen',  'BLACK', 590000, 450000, now() - interval '1 day', now() + interval '14 days', 2)
ON CONFLICT (id) DO NOTHING;

-- ---------- Variants (sku = <PRODUCT>-<COLOUR>-<SIZE>) ----------

INSERT INTO catalog.variants (id, colorway_id, sku, size, size_order) VALUES
  -- Tempo 2: 40–42 per colour
  ('00000000-0000-7000-8003-000000000001', '00000000-0000-7000-8002-000000000001', 'TEMPO2-BLK-40', '40', 40),
  ('00000000-0000-7000-8003-000000000002', '00000000-0000-7000-8002-000000000001', 'TEMPO2-BLK-41', '41', 41),
  ('00000000-0000-7000-8003-000000000003', '00000000-0000-7000-8002-000000000001', 'TEMPO2-BLK-42', '42', 42),
  ('00000000-0000-7000-8003-000000000004', '00000000-0000-7000-8002-000000000002', 'TEMPO2-WHT-40', '40', 40),
  ('00000000-0000-7000-8003-000000000005', '00000000-0000-7000-8002-000000000002', 'TEMPO2-WHT-41', '41', 41),
  ('00000000-0000-7000-8003-000000000006', '00000000-0000-7000-8002-000000000002', 'TEMPO2-WHT-42', '42', 42),
  ('00000000-0000-7000-8003-000000000007', '00000000-0000-7000-8002-000000000003', 'TEMPO2-BLU-40', '40', 40),
  ('00000000-0000-7000-8003-000000000008', '00000000-0000-7000-8002-000000000003', 'TEMPO2-BLU-41', '41', 41),
  ('00000000-0000-7000-8003-000000000009', '00000000-0000-7000-8002-000000000003', 'TEMPO2-BLU-42', '42', 42),
  -- Court Classic: 39–42 per colour
  ('00000000-0000-7000-8003-000000000010', '00000000-0000-7000-8002-000000000004', 'COURT-WHT-39', '39', 39),
  ('00000000-0000-7000-8003-000000000011', '00000000-0000-7000-8002-000000000004', 'COURT-WHT-40', '40', 40),
  ('00000000-0000-7000-8003-000000000012', '00000000-0000-7000-8002-000000000004', 'COURT-WHT-41', '41', 41),
  ('00000000-0000-7000-8003-000000000013', '00000000-0000-7000-8002-000000000004', 'COURT-WHT-42', '42', 42),
  ('00000000-0000-7000-8003-000000000014', '00000000-0000-7000-8002-000000000005', 'COURT-GRN-39', '39', 39),
  ('00000000-0000-7000-8003-000000000015', '00000000-0000-7000-8002-000000000005', 'COURT-GRN-40', '40', 40),
  ('00000000-0000-7000-8003-000000000016', '00000000-0000-7000-8002-000000000005', 'COURT-GRN-41', '41', 41),
  ('00000000-0000-7000-8003-000000000017', '00000000-0000-7000-8002-000000000005', 'COURT-GRN-42', '42', 42),
  -- Canvas Low
  ('00000000-0000-7000-8003-000000000018', '00000000-0000-7000-8002-000000000006', 'CANVAS-BLK-40', '40', 40),
  ('00000000-0000-7000-8003-000000000019', '00000000-0000-7000-8002-000000000006', 'CANVAS-BLK-41', '41', 41),
  ('00000000-0000-7000-8003-000000000020', '00000000-0000-7000-8002-000000000006', 'CANVAS-BLK-42', '42', 42),
  -- Daily Canvas
  ('00000000-0000-7000-8003-000000000021', '00000000-0000-7000-8002-000000000007', 'DAILY-BEI-40', '40', 40),
  ('00000000-0000-7000-8003-000000000022', '00000000-0000-7000-8002-000000000007', 'DAILY-BEI-41', '41', 41),
  -- Pace: S/M/L, size_order keeps them in that order (alphabetical would be L, M, S)
  ('00000000-0000-7000-8003-000000000023', '00000000-0000-7000-8002-000000000008', 'PACE-PNK-S', 'S', 1),
  ('00000000-0000-7000-8003-000000000024', '00000000-0000-7000-8002-000000000008', 'PACE-PNK-M', 'M', 2),
  ('00000000-0000-7000-8003-000000000025', '00000000-0000-7000-8002-000000000008', 'PACE-PNK-L', 'L', 3),
  ('00000000-0000-7000-8003-000000000026', '00000000-0000-7000-8002-000000000009', 'PACE-BLK-S', 'S', 1),
  ('00000000-0000-7000-8003-000000000027', '00000000-0000-7000-8002-000000000009', 'PACE-BLK-M', 'M', 2),
  ('00000000-0000-7000-8003-000000000028', '00000000-0000-7000-8002-000000000009', 'PACE-BLK-L', 'L', 3)
ON CONFLICT (id) DO NOTHING;

-- ---------- Stock flags (Q-1a-1: seeded directly until inventory exists) ----------
-- Sold out: Tempo 2 Đen size 41, and every size of Court Classic Trắng.

INSERT INTO catalog.variant_stock_flags (variant_id, sku, in_stock, updated_at)
SELECT id, sku, NOT (sku = 'TEMPO2-BLK-41' OR sku LIKE 'COURT-WHT-%'), now()
FROM catalog.variants
ON CONFLICT (variant_id) DO NOTHING;

-- ---------- Images: one per colour; files go to SeaweedFS later (Q-1a-2), we store the key (ADR-0007) ----------

INSERT INTO catalog.product_images (id, colorway_id, storage_key, alt, width, height, position, source) VALUES
  ('00000000-0000-7000-8004-000000000001', '00000000-0000-7000-8002-000000000001', 'products/tempo-2/den-1.jpg',          'Giày chạy bộ Cadence Tempo 2 màu đen, nhìn từ bên hông',      1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000002', '00000000-0000-7000-8002-000000000002', 'products/tempo-2/trang-1.jpg',        'Giày chạy bộ Cadence Tempo 2 màu trắng, nhìn từ bên hông',    1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000003', '00000000-0000-7000-8002-000000000003', 'products/tempo-2/xanh-1.jpg',         'Giày chạy bộ Cadence Tempo 2 màu xanh, nhìn từ bên hông',     1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000004', '00000000-0000-7000-8002-000000000004', 'products/court-classic/trang-1.jpg',  'Giày Court Classic màu trắng đế gum, nhìn từ bên hông',       1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000005', '00000000-0000-7000-8002-000000000005', 'products/court-classic/xanh-reu-1.jpg','Giày Court Classic màu xanh rêu, nhìn từ bên hông',          1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000006', '00000000-0000-7000-8002-000000000006', 'products/canvas-low/den-1.jpg',       'Giày vải Canvas Low màu đen, nhìn từ bên hông',               1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000007', '00000000-0000-7000-8002-000000000007', 'products/daily-canvas/be-1.jpg',      'Giày vải Daily Canvas màu be, nhìn từ bên hông',              1200, 1200, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000008', '00000000-0000-7000-8002-000000000008', 'products/pace-tee/hong-1.jpg',        'Áo chạy bộ nữ Pace màu hồng, mặt trước',                      1200, 1500, 1, 'seed'),
  ('00000000-0000-7000-8004-000000000009', '00000000-0000-7000-8002-000000000009', 'products/pace-tee/den-1.jpg',         'Áo chạy bộ nữ Pace màu đen, mặt trước',                       1200, 1500, 1, 'seed')
ON CONFLICT (id) DO NOTHING;
