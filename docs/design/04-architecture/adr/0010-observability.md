# ADR-0010: Quan sát hệ thống: log JSON, Prometheus, Grafana, Loki

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** N6, G6; ADR-0005 (sự kiện DEAD), ADR-0008

## Bối cảnh
N6 yêu cầu log có cấu trúc + correlation id, metrics nghiệp vụ, health check. Khi có lỗi thanh toán, phải lần được từ một đơn ra mọi dòng log liên quan.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. Chỉ đọc `docker logs` | Không tốn gì | Không tìm kiếm, không biểu đồ, không cảnh báo |
| B. **Prometheus + Grafana + Loki** tự chạy | Chuẩn phổ biến, miễn phí, học được nhiều | Tốn khoảng 0,5–1 GB RAM trên VPS |
| C. Dịch vụ ngoài (Grafana Cloud gói free, Datadog…) | Không tốn RAM máy | Phụ thuộc giới hạn gói free; ít học về vận hành |

## Quyết định
Chọn **B**.
- **Log:** JSON, mỗi dòng có `correlationId` (lấy từ header hoặc tạo mới ở Nginx/API, chuyển tiếp sang listener của outbox), `orderId` khi có. Không ghi mật khẩu, số thẻ, token.
- **Metrics:** Micrometer → Prometheus. Ngoài metrics kỹ thuật (JVM, HTTP, pool DB), có metrics nghiệp vụ: số đơn mỗi phút, tỷ lệ thanh toán thành công, số payment chờ quá hạn, số sự kiện outbox `DEAD`, số lần `OUT_OF_STOCK`.
- **Health:** `/actuator/health` (liveness, readiness có kiểm tra DB) cho Docker và smoke test.
- **Cảnh báo** cơ bản qua Grafana: API chết, outbox có sự kiện DEAD, payment chờ quá hạn tăng.

## Hệ quả
- Làm sau cùng trong lộ trình DevOps (D8, lát 2), sau khi đã có luồng mua hàng.
- Nếu RAM VPS không đủ, chuyển Loki sang C (Grafana Cloud free) trước.
