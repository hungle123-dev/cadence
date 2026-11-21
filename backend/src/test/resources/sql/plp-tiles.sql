-- PLP test data for ProductTileReadAdapterIntegrationTest. Needs db/seed/R__seed_categories.sql first.
-- Times are fixed: the test searches at NOW = 2026-12-04T12:00:00Z, so nothing expires with the calendar.
-- Id blocks: 9001 products, 9002 colorways, 9003 variants, 9004 images.
-- Categories: 0011 giay-chay-bo, 0012 giay-lifestyle, 0021 ao-thun.

INSERT INTO catalog.products (id, slug, name, type, gender, category_id, status, publish_at, created_at, updated_at) VALUES
  -- run-a: MEN, 3 colours (AC 1.1); Đen on sale, Đỏ sold out with no image (AC 1.11)
  ('00000000-0000-7000-9001-000000000001', 'run-a', 'Run A', 'SHOES', 'MEN', '00000000-0000-7000-8000-000000000011',
   'PUBLISHED', '2026-11-01T00:00:00Z', now(), now()),
  -- run-b: WOMEN, newer (AC 2.5, 2.6)
  ('00000000-0000-7000-9001-000000000002', 'run-b', 'Run B', 'SHOES', 'WOMEN', '00000000-0000-7000-8000-000000000011',
   'PUBLISHED', '2026-11-20T00:00:00Z', now(), now()),
  -- run-c: UNISEX in another category; its sale ended (AC 1.6, 2.5)
  ('00000000-0000-7000-9001-000000000003', 'run-c', 'Run C', 'SHOES', 'UNISEX', '00000000-0000-7000-8000-000000000012',
   'PUBLISHED', '2026-11-10T00:00:00Z', now(), now()),
  -- hidden: draft, and scheduled for tomorrow (AC 1.3)
  ('00000000-0000-7000-9001-000000000004', 'draft-x', 'Draft X', 'SHOES', 'MEN', '00000000-0000-7000-8000-000000000011',
   'DRAFT', NULL, now(), now()),
  ('00000000-0000-7000-9001-000000000005', 'sched-future', 'Sched Future', 'SHOES', 'MEN', '00000000-0000-7000-8000-000000000011',
   'SCHEDULED', '2026-12-05T12:00:00Z', now(), now()),
  -- scheduled one minute before NOW: visible (AC 1.4), and the newest
  ('00000000-0000-7000-9001-000000000006', 'sched-past', 'Sched Past', 'SHOES', 'WOMEN', '00000000-0000-7000-8000-000000000011',
   'SCHEDULED', '2026-12-04T11:59:00Z', now(), now()),
  -- bulk: 30 colours at one price, for paging (AC 1.7, 1.8)
  ('00000000-0000-7000-9001-000000000007', 'bulk', 'Bulk Tee', 'APPAREL', 'UNISEX', '00000000-0000-7000-8000-000000000021',
   'PUBLISHED', '2026-10-01T00:00:00Z', now(), now());

INSERT INTO catalog.colorways (id, product_id, slug, name, color_family, list_price, sale_price, sale_starts_at, sale_ends_at, position) VALUES
  ('00000000-0000-7000-9002-000000000001', '00000000-0000-7000-9001-000000000001', 'den',   'Đen',   'BLACK', 1500000, 900000, '2026-12-01T00:00:00Z', '2026-12-31T00:00:00Z', 0),
  ('00000000-0000-7000-9002-000000000002', '00000000-0000-7000-9001-000000000001', 'trang', 'Trắng', 'WHITE', 1000000, NULL, NULL, NULL, 1),
  ('00000000-0000-7000-9002-000000000003', '00000000-0000-7000-9001-000000000001', 'do',    'Đỏ',    'RED',   2000000, NULL, NULL, NULL, 2),
  ('00000000-0000-7000-9002-000000000004', '00000000-0000-7000-9001-000000000002', 'den',   'Đen',   'BLACK', 1200000, NULL, NULL, NULL, 0),
  ('00000000-0000-7000-9002-000000000005', '00000000-0000-7000-9001-000000000003', 'xanh',  'Xanh',  'BLUE',  800000, 500000, '2026-11-01T00:00:00Z', '2026-11-30T00:00:00Z', 0),
  ('00000000-0000-7000-9002-000000000006', '00000000-0000-7000-9001-000000000004', 'den',   'Đen',   'BLACK', 1000000, NULL, NULL, NULL, 0),
  ('00000000-0000-7000-9002-000000000007', '00000000-0000-7000-9001-000000000005', 'den',   'Đen',   'BLACK', 1000000, NULL, NULL, NULL, 0),
  ('00000000-0000-7000-9002-000000000008', '00000000-0000-7000-9001-000000000006', 'xanh-la', 'Xanh lá', 'GREEN', 1100000, NULL, NULL, NULL, 0);

INSERT INTO catalog.colorways (id, product_id, slug, name, color_family, list_price, position)
SELECT ('00000000-0000-7000-9002-' || lpad((100 + n)::text, 12, '0'))::uuid,
       '00000000-0000-7000-9001-000000000007', 'm' || n, 'Màu ' || n, 'GREY', 300000, n
FROM generate_series(1, 30) AS n;

INSERT INTO catalog.variants (id, colorway_id, sku, size, size_order) VALUES
  ('00000000-0000-7000-9003-000000000001', '00000000-0000-7000-9002-000000000001', 'RA-DEN-42',   '42', 42),
  ('00000000-0000-7000-9003-000000000002', '00000000-0000-7000-9002-000000000001', 'RA-DEN-43',   '43', 43),
  ('00000000-0000-7000-9003-000000000003', '00000000-0000-7000-9002-000000000002', 'RA-TRANG-40', '40', 40),
  ('00000000-0000-7000-9003-000000000004', '00000000-0000-7000-9002-000000000003', 'RA-DO-42',    '42', 42),
  ('00000000-0000-7000-9003-000000000005', '00000000-0000-7000-9002-000000000003', 'RA-DO-43',    '43', 43),
  ('00000000-0000-7000-9003-000000000006', '00000000-0000-7000-9002-000000000004', 'RB-DEN-40',   '40', 40),
  ('00000000-0000-7000-9003-000000000007', '00000000-0000-7000-9002-000000000005', 'RC-XANH-42',  '42', 42),
  ('00000000-0000-7000-9003-000000000008', '00000000-0000-7000-9002-000000000008', 'SP-XL-41',    '41', 41),
  ('00000000-0000-7000-9003-000000000009', '00000000-0000-7000-9002-000000000006', 'DX-DEN-44',   '44', 44),
  ('00000000-0000-7000-9003-000000000010', '00000000-0000-7000-9002-000000000007', 'SF-DEN-42',   '42', 42);

-- RA-DO-42 is flagged sold out and RA-DO-43 has no flag at all: both count as sold out.
-- Hidden products are in stock on purpose: stock must not make them visible.
INSERT INTO catalog.variant_stock_flags (variant_id, sku, in_stock, updated_at) VALUES
  ('00000000-0000-7000-9003-000000000001', 'RA-DEN-42',   true,  now()),
  ('00000000-0000-7000-9003-000000000002', 'RA-DEN-43',   false, now()),
  ('00000000-0000-7000-9003-000000000003', 'RA-TRANG-40', true,  now()),
  ('00000000-0000-7000-9003-000000000004', 'RA-DO-42',    false, now()),
  ('00000000-0000-7000-9003-000000000006', 'RB-DEN-40',   true,  now()),
  ('00000000-0000-7000-9003-000000000007', 'RC-XANH-42',  true,  now()),
  ('00000000-0000-7000-9003-000000000008', 'SP-XL-41',    true,  now()),
  ('00000000-0000-7000-9003-000000000009', 'DX-DEN-44',   true,  now()),
  ('00000000-0000-7000-9003-000000000010', 'SF-DEN-42',   true,  now());

-- run-a/den has two images; the one at position 0 is inserted second to prove ordering is by position.
INSERT INTO catalog.product_images (id, colorway_id, storage_key, alt, width, height, position) VALUES
  ('00000000-0000-7000-9004-000000000001', '00000000-0000-7000-9002-000000000001', 'test/run-a-den-2.jpg', 'Run A đen, mặt sau', 1200, 1200, 1),
  ('00000000-0000-7000-9004-000000000002', '00000000-0000-7000-9002-000000000001', 'test/run-a-den-1.jpg', 'Run A đen, bên hông', 1200, 1200, 0),
  ('00000000-0000-7000-9004-000000000003', '00000000-0000-7000-9002-000000000002', 'test/run-a-trang-1.jpg', 'Run A trắng', 1200, 1200, 0),
  ('00000000-0000-7000-9004-000000000004', '00000000-0000-7000-9002-000000000004', 'test/run-b-den-1.jpg', 'Run B đen', 1200, 1200, 0),
  ('00000000-0000-7000-9004-000000000005', '00000000-0000-7000-9002-000000000005', 'test/run-c-xanh-1.jpg', 'Run C xanh', 1200, 1200, 0),
  ('00000000-0000-7000-9004-000000000006', '00000000-0000-7000-9002-000000000008', 'test/sched-past-1.jpg', 'Sched Past xanh lá', 1200, 1200, 0);
