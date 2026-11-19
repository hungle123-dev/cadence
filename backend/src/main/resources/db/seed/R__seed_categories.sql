-- Dữ liệu mẫu cho máy cá nhân. Chỉ profile local nạp thư mục db/seed (application-local.yaml).
-- id cố định để category con trỏ được tới category cha.
INSERT INTO catalog.categories (id, parent_id, slug, name, depth, position, created_at, updated_at) VALUES
  ('00000000-0000-7000-8000-000000000001', NULL, 'giay',     'Giày',     1, 1, now(), now()),
  ('00000000-0000-7000-8000-000000000002', NULL, 'quan-ao',  'Quần áo',  1, 2, now(), now()),
  ('00000000-0000-7000-8000-000000000011', '00000000-0000-7000-8000-000000000001', 'giay-chay-bo',  'Giày chạy bộ',  2, 1, now(), now()),
  ('00000000-0000-7000-8000-000000000012', '00000000-0000-7000-8000-000000000001', 'giay-lifestyle','Giày lifestyle',2, 2, now(), now()),
  ('00000000-0000-7000-8000-000000000021', '00000000-0000-7000-8000-000000000002', 'ao-thun',       'Áo thun',       2, 1, now(), now()),
  ('00000000-0000-7000-8000-000000000022', '00000000-0000-7000-8000-000000000002', 'ao-chay-bo',    'Áo chạy bộ',    2, 2, now(), now())
ON CONFLICT (slug) DO NOTHING;