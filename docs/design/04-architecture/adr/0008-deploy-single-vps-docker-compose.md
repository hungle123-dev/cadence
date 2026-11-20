# ADR-0008: Deploy trên 1 VPS bằng Docker Compose, Caddy làm HTTPS

- **Trạng thái:** Đã chấp nhận (2026-11-17). Phần "Caddy làm HTTPS" được thay bởi [ADR-0017](0017-reverse-proxy-nginx.md) (Nginx), các quyết định còn lại giữ nguyên
- **Ngày:** 2026-11-17
- **Liên quan:** Q3, N8, mục 1.9 (định nghĩa xong cần link staging), lộ trình DevOps D1–D7

## Bối cảnh
Người review cần một link chạy thật. VNPay sandbox cần địa chỉ công khai để gửi IPN. Bạn muốn **tự làm DevOps để học**. Ngân sách nhỏ.

## Các lựa chọn
| Lựa chọn | Chi phí/tháng | Ưu | Nhược |
|---|---|---|---|
| A. **1 VPS + Docker Compose + Caddy** | ~5–7 USD | Rẻ, link luôn nhanh, môi trường giống hệt local, học được nhiều nhất | Tự lo bảo mật máy, backup, cập nhật |
| B. Dịch vụ có gói miễn phí (Vercel, Render/Fly, Neon, R2) | 0–5 USD | Ít vận hành | Gói free "ngủ" khi không ai dùng; nhiều nhà cung cấp; ít học được |
| C. AWS/GCP (ECS, RDS, S3) | 20–50 USD | Giống doanh nghiệp lớn | Đắt, cấu hình nhiều, quá sức cho GĐ1 |

## Quyết định
Chọn **A**.
- VPS khoảng **2 vCPU, 4 GB RAM** (đủ cho API Java, Next.js, PostgreSQL, SeaweedFS và bộ quan sát). Chọn nhà cung cấp lúc làm bước 6.
- Một file `docker-compose.yml` cho local và một file cho staging dùng chung image.
- ~~Caddy lo HTTPS tự động~~ → Nginx + Certbot (ADR-0017); tên miền riêng (~10–15 USD/năm) hoặc tên miền miễn phí lúc đầu.
- Bí mật (mật khẩu DB, khoá VNPay) để trong file `.env` trên VPS và GitHub Secrets; **không commit**.
- Bảo mật máy: đăng nhập SSH bằng khoá, tắt mật khẩu, firewall chỉ mở 22/80/443, user deploy riêng.
- Backup: `pg_dump` mỗi đêm + dữ liệu SeaweedFS, gửi ra **ngoài VPS** (ví dụ R2 gói miễn phí), giữ 7 bản; **thử khôi phục** ít nhất một lần.

## Hệ quả
- Một máy = một điểm hỏng. Chấp nhận được cho demo; ghi rõ trong README dự án.
- Deploy có vài giây gián đoạn khi khởi động lại container; deploy không gián đoạn để GĐ2 (D10).
- Không tăng ngân sách giờ (mục 1.8): việc DevOps nằm trong 20 giờ walking skeleton và các lát; nếu trễ, bỏ Loki (D8) trước.
