# ADR-0017: Reverse proxy là Nginx thay cho Caddy

- **Trạng thái:** Đã chấp nhận (2026-11-20)
- **Ngày:** 2026-11-20
- **Liên quan:** ADR-0006 (cùng tên miền, `/api/*` vào backend), ADR-0008 (thay phần "Caddy làm HTTPS"), S9, S12

## Bối cảnh
ADR-0008 chọn Caddy vì Caddy tự xin và gia hạn chứng chỉ HTTPS. Khi làm S9, bạn muốn dùng Nginx, vì dự án sắp tới (NestJS) dùng Nginx và Nginx phổ biến hơn ở các công ty. Vai trò của reverse proxy không đổi: là cửa duy nhất mở ra ngoài, chia `/api/*` vào backend và phần còn lại vào frontend.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. Caddy (như ADR-0008) | HTTPS tự động, cấu hình ngắn, tự gửi header `X-Forwarded-*` | Ít gặp khi đi làm |
| B. **Nginx** | Phổ biến, nhiều tài liệu, dùng lại được kiến thức cho dự án NestJS | HTTPS phải tự làm (Certbot + gia hạn); phải tự khai báo header; cấu hình dài hơn |

## Quyết định
Chọn **B**: image `nginx:1.28-alpine`, cấu hình ở `infra/nginx/default.conf`.
- `location /api/` → `proxy_pass http://api:8080;` (không có `/` ở cuối, để giữ nguyên đường dẫn `/api/...`, khớp `context-path` của backend).
- `location /` → `proxy_pass http://web:3000;` với `proxy_buffering off`, để HTML của Next.js truyền từng phần (Suspense).
- Tự gửi `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto`.

## Hệ quả
- S12: HTTPS dùng Let's Encrypt qua Certbot; phải lo việc gia hạn tự động và mở cổng 443 trong Nginx. Tốn công hơn Caddy, bù lại học được cách làm thủ công.
- Nginx tra địa chỉ `api`/`web` lúc khởi động: cần `depends_on`. Khi container phía sau được tạo lại thì có thể trả 502 vì vẫn nhớ IP cũ; xử lý khi làm CD (S13), bằng `resolver` của Docker hoặc reload Nginx.
- Lúc backend đang khởi động, Nginx trả 502 vài giây; thêm healthcheck cho `api` ở S13.
- Các tài liệu đã ghi "Caddy" được đổi thành Nginx; ADR-0008 vẫn giữ các quyết định còn lại.
