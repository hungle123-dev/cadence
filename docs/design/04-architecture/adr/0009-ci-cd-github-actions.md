# ADR-0009: CI/CD bằng GitHub Actions, image trên GHCR, migration bằng Flyway

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** N7, N8, N9, G5; ADR-0008

## Bối cảnh
N8 yêu cầu mỗi lần push chạy build + test, deploy staging tự động hoặc bằng một lệnh. Code để trên GitHub.

## Các lựa chọn
| Câu hỏi | Lựa chọn | Chọn |
|---|---|---|
| CI | **GitHub Actions** / GitLab CI / Jenkins tự dựng | **GitHub Actions**: cùng chỗ với code, miễn phí cho repo public |
| Nơi chứa image | **GHCR** / Docker Hub | **GHCR**: gắn với repo, không giới hạn kéo image như Docker Hub |
| Deploy | Kéo code về VPS rồi build tại chỗ / **build trong CI, VPS chỉ kéo image** | **Build trong CI**: VPS không cần công cụ build, deploy nhanh và lặp lại được |
| Migration DB | **Flyway** / Liquibase / tay | **Flyway**, chạy khi API khởi động (chỉ có một instance) |

## Quyết định
- **Pull request:** build, unit test, test ArchUnit, test tích hợp với Testcontainers, lint frontend. Đỏ thì không được merge.
- **Merge vào `main`:** chạy lại toàn bộ → build image API và Web, gắn tag theo commit → đẩy lên GHCR → SSH vào VPS → `docker compose pull && docker compose up -d` → **smoke test** (gọi health check và một trang PLP) → báo kết quả.
- Migration Flyway chỉ **thêm**, không sửa file đã chạy; thay đổi phá vỡ chia thành nhiều bước (thêm cột → chép dữ liệu → bỏ cột cũ).
- Rollback: deploy lại tag image trước đó (một lệnh). Migration không tự rollback, nên phải viết tương thích ngược.

## Hệ quả
- Cần SSH key riêng cho CI, quyền hạn chế trên VPS.
- Test Testcontainers làm CI chậm hơn (vài phút); chấp nhận để có test DB thật (N7).
