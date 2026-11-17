# ADR-0013: GĐ1 không dùng Redis và message broker; PostgreSQL làm cả hàng đợi

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** N3, N5, ADR-0004, ADR-0005, ADR-0006, ADR-0008

## Bối cảnh
Nhiều hệ thống thương mại điện tử có Redis (cache, session, rate limit), message queue (RabbitMQ, Kafka) và worker riêng. Mỗi thành phần thêm vào là thêm RAM trên VPS 4 GB, thêm việc vận hành, thêm chỗ hỏng. Câu hỏi: GĐ1 có **vấn đề cụ thể** nào cần chúng không?

## Từng nhu cầu và cách đáp ứng ở GĐ1
| Nhu cầu | Thường dùng | GĐ1 dùng | Vì sao đủ |
|---|---|---|---|
| Cache trang sản phẩm | Redis | Cache của Next.js (render sẵn trang PLP/PDP, làm mới theo thời gian hoặc khi đổi sản phẩm) + index PostgreSQL | Trang đọc nhiều đã được cache ở tầng web; N3 (p95 < 300 ms) đo bằng k6 để kiểm chứng |
| Session đăng nhập | Redis | PostgreSQL (Spring Session JDBC) | Một instance API, lượng người dùng nhỏ |
| Rate limit | Redis | Bộ đếm trong bộ nhớ (Bucket4j) | Chỉ có một instance API |
| Hàng đợi sự kiện | RabbitMQ / Kafka | Bảng outbox + `SKIP LOCKED` (ADR-0005) | Mọi module ở cùng một tiến trình |
| Worker chạy nền | Container riêng | Scheduler và outbox relay chạy trong API | Ít việc nền; tách ra được bất cứ lúc nào (cùng image, chạy với profile `worker`) |
| Giỏ hàng | Redis | PostgreSQL (ADR-0004) | Giỏ cần bền, cần gộp, cần đọc lúc đặt hàng |

## Quyết định
GĐ1 **không** có Redis, không có message broker. PostgreSQL là nơi lưu duy nhất (cộng SeaweedFS cho ảnh).

**Khi nào thêm** (ghi sẵn để không phải tranh luận lại):
- **Redis:** khi chạy hơn một instance API (session, rate limit dùng chung); khi đo k6 thấy N3 không đạt mà cache Next.js không đủ; ở GĐ3 cho launch/bốc thăm (bộ đếm SKU nóng, hàng chờ).
- **Message broker:** khi tách một module thành service riêng.
- **Worker riêng:** khi việc nền làm chậm API (đo bằng metrics), chỉ cần chạy thêm một container cùng image.

## Hệ quả
- Ít container, ít RAM, ít thứ phải học cùng lúc; tập trung vào phần lõi (G1–G3).
- Phải có số đo (k6, metrics) để chứng minh không cần cache, thay vì đoán.
- Nếu muốn học Redis sớm, việc hợp lý nhất là rate limit dùng chung, khi tập chạy 2 instance API sau load balancer (GĐ2).
