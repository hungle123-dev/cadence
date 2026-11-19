-- Phòng riêng của module catalog (ADR-0014)
CREATE SCHEMA IF NOT EXISTS catalog;

-- Nhân viên (cadence_app) được nhìn thấy phòng catalog
GRANT USAGE ON SCHEMA catalog TO cadence_app;

-- Mọi bảng thủ kho tạo trong catalog: nhân viên tự có quyền đọc/ghi dữ liệu (không có quyền sửa bảng)
ALTER DEFAULT PRIVILEGES IN SCHEMA catalog
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO cadence_app;

-- Bảng category theo thiết kế 5.10
CREATE TABLE catalog.categories (
    id          uuid        PRIMARY KEY,
    parent_id   uuid        REFERENCES catalog.categories (id),
    slug        text        NOT NULL UNIQUE,
    name        text        NOT NULL,
    depth       smallint    NOT NULL CHECK (depth BETWEEN 1 AND 3),
    position    int         NOT NULL DEFAULT 0,
    created_at  timestamptz NOT NULL,
    updated_at  timestamptz NOT NULL,
    version     bigint      NOT NULL DEFAULT 0
);