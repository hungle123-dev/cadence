-- Catalog module (database doc 4.1). Runs as cadence_owner; init.sh's default privileges give
-- cadence_app SELECT, INSERT, UPDATE, DELETE on every table created here.
CREATE SCHEMA catalog;
GRANT USAGE ON SCHEMA catalog TO cadence_app;

CREATE TABLE catalog.categories (
    id          uuid    PRIMARY KEY,
    slug        text    NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),
    name        text    NOT NULL,
    sort_order  integer NOT NULL DEFAULT 0
);

CREATE TABLE catalog.products (
    id               uuid        PRIMARY KEY,
    slug             text        NOT NULL UNIQUE CHECK (slug ~ '^[a-z0-9]+(-[a-z0-9]+)*$'),   -- BR-CAT-02
    name             text        NOT NULL,
    category_id      uuid        NOT NULL REFERENCES catalog.categories (id),
    gender           text        NOT NULL CHECK (gender IN ('MEN', 'WOMEN', 'UNISEX', 'KIDS')),
    sport            text        NOT NULL CHECK (sport IN ('RUNNING', 'TRAIL', 'LIFESTYLE', 'TRAINING', 'FOOTBALL')),
    description      text        NOT NULL DEFAULT '',
    specs            jsonb       NOT NULL DEFAULT '[]',     -- [{"name": "Trọng lượng", "value": "228 g"}], in display order
    status           text        NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')),   -- SRS 6.5
    launch_at        timestamptz,                           -- giờ mở bán; = lúc đăng nếu không lên lịch
    list_price       bigint      NOT NULL CHECK (list_price > 0),                                 -- BR-PRC-08
    sale_price       bigint      CHECK (sale_price > 0 AND sale_price < list_price),
    sale_starts_at   timestamptz,                           -- khuyến mãi hiệu lực trong [starts, ends) (BR-PRC-09)
    sale_ends_at     timestamptz,                           -- null = không thời hạn
    featured_rank    integer     CHECK (featured_rank > 0), -- null = không nổi bật (FR-CAT-03)
    fit              text        NOT NULL DEFAULT 'TRUE_TO_SIZE' CHECK (fit IN ('SMALL', 'TRUE_TO_SIZE', 'LARGE')),
    rating           numeric(2,1) CHECK (rating BETWEEN 0 AND 5),    -- dữ liệu mẫu (SRS A-03)
    review_count     integer     NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    seo_title        text,                                  -- ghi đè (FR-ADM-PRD-05); null = mặc định NFR-SEO-03
    seo_description  text,
    created_at       timestamptz NOT NULL,
    updated_at       timestamptz NOT NULL,
    version          bigint      NOT NULL DEFAULT 0,
    CONSTRAINT products_launch_at_required CHECK (status = 'DRAFT' OR launch_at IS NOT NULL),
    CONSTRAINT products_sale_complete CHECK ((sale_price IS NULL) = (sale_starts_at IS NULL)),
    CONSTRAINT products_sale_window_order
        CHECK (sale_ends_at IS NULL OR (sale_starts_at IS NOT NULL AND sale_ends_at > sale_starts_at))
);
CREATE INDEX products_listing_idx ON catalog.products (category_id, featured_rank) WHERE status = 'PUBLISHED';

CREATE TABLE catalog.colorways (
    id            uuid    PRIMARY KEY,
    product_id    uuid    NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    name          text    NOT NULL,
    color_family  text    NOT NULL CHECK (color_family IN ('BLACK', 'WHITE', 'GREY', 'GREEN', 'BLUE', 'PINK', 'BROWN')),
    sort_order    integer NOT NULL DEFAULT 0,
    UNIQUE (product_id, name)
);
CREATE INDEX colorways_product_idx ON catalog.colorways (product_id, sort_order);

CREATE TABLE catalog.product_images (
    id           uuid    PRIMARY KEY,
    colorway_id  uuid    NOT NULL REFERENCES catalog.colorways (id) ON DELETE CASCADE,
    image_key    text    NOT NULL CHECK (image_key !~ '^[a-z]+://'),   -- khoá ảnh, không chứa host (BR-CAT-01)
    width        integer NOT NULL CHECK (width > 0),
    height       integer NOT NULL CHECK (height > 0),
    alt          text    NOT NULL CHECK (alt <> ''),
    sort_order   integer NOT NULL DEFAULT 0
);
CREATE INDEX product_images_colorway_id_idx ON catalog.product_images (colorway_id, sort_order);

CREATE TABLE catalog.variants (
    id           uuid        PRIMARY KEY,
    product_id   uuid        NOT NULL REFERENCES catalog.products (id) ON DELETE CASCADE,
    colorway_id  uuid        NOT NULL REFERENCES catalog.colorways (id) ON DELETE CASCADE,
    size         text        NOT NULL,                  -- "42", "M", "OS" (một size)
    size_order   integer     NOT NULL,                  -- để sắp 39 < 40 và S < M < L
    sku          text        NOT NULL UNIQUE,
    gtin         text        UNIQUE,
    available    boolean     NOT NULL DEFAULT false,    -- bản sao "ATS > 0" từ inventory (kiến trúc 7.8)
    created_at   timestamptz NOT NULL,
    updated_at   timestamptz NOT NULL,
    version      bigint      NOT NULL DEFAULT 0,
    UNIQUE (colorway_id, size)
);
CREATE INDEX variants_available_idx ON catalog.variants (product_id, size) WHERE available;
CREATE INDEX variants_product_idx ON catalog.variants (product_id);
