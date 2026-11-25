-- Cases the sample data does not have, loaded after db/seed/dev/R__seed_catalog.sql.
-- Tests run at the fixed time 2026-11-25T03:00:00Z (CatalogTestClock).
--   future-runner   published, launches at 13:00 the same day: coming soon (FR-CAT-07)
--   draft-runner    draft: hidden (SRS 6.5)
--   archived-runner archived: hidden
--   flash-runner    on sale, with a sale active only on 25/11 (BR-PRC-09)
INSERT INTO catalog.products (id, slug, name, category_id, gender, sport, description, status, launch_at,
                              list_price, sale_price, sale_starts_at, sale_ends_at, featured_rank,
                              created_at, updated_at)
SELECT v.id::uuid, v.slug, v.name, c.id, 'MEN', 'RUNNING', 'Giày chạy thử nghiệm', v.status, v.launch_at::timestamptz,
       v.list_price, v.sale_price, v.sale_starts_at::timestamptz, v.sale_ends_at::timestamptz, NULL,
       '2026-11-01T00:00:00Z', '2026-11-01T00:00:00Z'
FROM (VALUES
    ('00000000-0000-7000-8000-000000000001', 'future-runner', 'Future Runner', 'PUBLISHED', '2026-11-25T13:00:00Z',
     2500000, NULL::bigint, NULL, NULL),
    ('00000000-0000-7000-8000-000000000002', 'draft-runner', 'Draft Runner', 'DRAFT', NULL,
     2500000, NULL, NULL, NULL),
    ('00000000-0000-7000-8000-000000000003', 'archived-runner', 'Archived Runner', 'ARCHIVED', '2026-01-01T00:00:00Z',
     2500000, NULL, NULL, NULL),
    ('00000000-0000-7000-8000-000000000004', 'flash-runner', 'Flash Runner', 'PUBLISHED', '2026-10-01T00:00:00Z',
     2000000, 1500000, '2026-11-25T00:00:00Z', '2026-11-26T00:00:00Z')
) AS v (id, slug, name, status, launch_at, list_price, sale_price, sale_starts_at, sale_ends_at)
JOIN catalog.categories c ON c.slug = 'giay-chay-bo';

INSERT INTO catalog.colorways (id, product_id, name, color_family, sort_order)
SELECT ('00000000-0000-7000-8001-00000000000' || n)::uuid, ('00000000-0000-7000-8000-00000000000' || n)::uuid,
       'Xanh', 'BLUE', 0
FROM generate_series(1, 4) AS n;

INSERT INTO catalog.product_images (id, colorway_id, image_key, width, height, alt, sort_order)
SELECT ('00000000-0000-7000-8002-00000000000' || n)::uuid, ('00000000-0000-7000-8001-00000000000' || n)::uuid,
       'test/runner-' || n || '.jpg', 1200, 1500, 'Giày thử nghiệm ' || n, 0
FROM generate_series(1, 4) AS n;

INSERT INTO catalog.variants (id, product_id, colorway_id, size, size_order, sku, available, created_at, updated_at)
SELECT ('00000000-0000-7000-8003-00000000000' || n)::uuid, ('00000000-0000-7000-8000-00000000000' || n)::uuid,
       ('00000000-0000-7000-8001-00000000000' || n)::uuid, '42', 14, 'TEST-RUNNER-' || n, true,
       '2026-11-01T00:00:00Z', '2026-11-01T00:00:00Z'
FROM generate_series(1, 4) AS n;
