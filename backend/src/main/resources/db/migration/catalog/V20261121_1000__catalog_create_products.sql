-- Product aggregate (5.10): products -> colorways -> variants, images per colorway.
-- Only products carries version/created_at/updated_at: the children change with it.
-- cadence_app gets read/write on these tables through the default privileges of V20261119_1000.

CREATE TABLE catalog.products (
    id           uuid        PRIMARY KEY,
    slug         text        NOT NULL UNIQUE,
    name         text        NOT NULL,
    description  text        NOT NULL DEFAULT '',
    type         text        NOT NULL CHECK (type IN ('SHOES', 'APPAREL', 'ACCESSORIES')),
    gender       text        NOT NULL CHECK (gender IN ('MEN', 'WOMEN', 'UNISEX')),
    sport        text,
    category_id  uuid        NOT NULL REFERENCES catalog.categories (id),
    status       text        NOT NULL CHECK (status IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED')),
    publish_at   timestamptz,
    specs        jsonb       NOT NULL DEFAULT '{}',
    created_at   timestamptz NOT NULL,
    updated_at   timestamptz NOT NULL,
    version      bigint      NOT NULL DEFAULT 0,
    -- R-VIS: scheduled or published products always have a publish time
    CONSTRAINT products_publish_at_required
        CHECK (status NOT IN ('SCHEDULED', 'PUBLISHED') OR publish_at IS NOT NULL)
);

CREATE TABLE catalog.colorways (
    id              uuid    PRIMARY KEY,
    product_id      uuid    NOT NULL REFERENCES catalog.products (id),
    slug            text    NOT NULL,
    name            text    NOT NULL,
    color_family    text    NOT NULL,
    list_price      bigint  NOT NULL,
    sale_price      bigint,
    sale_starts_at  timestamptz,
    sale_ends_at    timestamptz,
    position        int     NOT NULL DEFAULT 0,
    CONSTRAINT colorways_product_slug_key UNIQUE (product_id, slug),
    CONSTRAINT colorways_product_name_key UNIQUE (product_id, name),
    CONSTRAINT colorways_color_family_check CHECK (color_family IN (
        'BLACK', 'WHITE', 'GREY', 'BEIGE', 'BROWN', 'RED', 'PINK',
        'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE', 'MULTI')),
    -- R-PRICE, same rules as ColorwayPricing and Sale
    CONSTRAINT colorways_list_price_positive CHECK (list_price > 0),
    CONSTRAINT colorways_sale_below_list CHECK (sale_price IS NULL OR (sale_price > 0 AND sale_price < list_price)),
    CONSTRAINT colorways_sale_window_order CHECK (sale_starts_at < sale_ends_at),
    CONSTRAINT colorways_sale_complete CHECK (
        (sale_price IS NULL) = (sale_starts_at IS NULL)
        AND (sale_price IS NULL) = (sale_ends_at IS NULL))
);

CREATE TABLE catalog.variants (
    id           uuid  PRIMARY KEY,
    colorway_id  uuid  NOT NULL REFERENCES catalog.colorways (id),
    sku          text  NOT NULL UNIQUE,
    size         text  NOT NULL,
    size_order   int   NOT NULL,
    CONSTRAINT variants_colorway_size_key UNIQUE (colorway_id, size)
);

CREATE TABLE catalog.product_images (
    id             uuid  PRIMARY KEY,
    colorway_id    uuid  NOT NULL REFERENCES catalog.colorways (id),
    storage_key    text  NOT NULL UNIQUE,
    alt            text  NOT NULL,
    width          int   NOT NULL,
    height         int   NOT NULL,
    position       int   NOT NULL DEFAULT 0,
    source         text,
    source_credit  text,
    CONSTRAINT product_images_size_positive CHECK (width > 0 AND height > 0)
);

-- Copy of "in stock" per variant, kept up to date from inventory (Q-1a-1), so the PLP
-- size filter (CAT-02) never reads the inventory schema.
CREATE TABLE catalog.variant_stock_flags (
    variant_id  uuid        PRIMARY KEY REFERENCES catalog.variants (id),
    sku         text        NOT NULL,
    in_stock    boolean     NOT NULL,
    updated_at  timestamptz NOT NULL
);

-- PLP indexes (5.10); measure with k6 (N3) before adding more
CREATE INDEX products_status_category_idx ON catalog.products (status, category_id);
CREATE INDEX colorways_product_idx ON catalog.colorways (product_id);
CREATE INDEX variants_colorway_idx ON catalog.variants (colorway_id);
CREATE INDEX product_images_colorway_idx ON catalog.product_images (colorway_id);
CREATE INDEX variant_stock_flags_in_stock_idx ON catalog.variant_stock_flags (variant_id) WHERE in_stock;
