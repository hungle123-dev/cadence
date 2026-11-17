# ADR-0005: Outbox và inbox tự viết, giao sự kiện trong cùng tiến trình

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** N2, nguyên tắc 6 (bước 3 mục 3.1), [events.md](../../03-tactical-design/events.md), ADR-0001

## Bối cảnh
Các module nói chuyện qua sự kiện (huỷ đơn → nhả hàng, thanh toán xong → xác nhận đơn, gửi email…). Hai lỗi phải tránh: thay đổi đã commit nhưng sự kiện mất (tiến trình chết đúng lúc), và sự kiện được xử lý hai lần.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. `@TransactionalEventListener` của Spring | Không cần bảng | Tiến trình chết sau commit là **mất sự kiện**; không thử lại |
| B. Spring Modulith event publication registry | Có sẵn lưu sự kiện và thử lại | Thêm framework; ít cơ hội hiểu cơ chế |
| C. **Outbox + inbox tự viết**, giao trong cùng tiến trình | Không mất sự kiện, idempotent, hiểu rõ cơ chế; chuyển sang broker sau này dễ | Phải tự viết relay, thử lại, theo dõi |
| D. Kafka / RabbitMQ | Chuẩn cho hệ phân tán | Quá nặng cho một tiến trình trên một máy |

## Quyết định
Chọn **C**.
- **Outbox:** bảng `outbox_event` (eventId, type, version, aggregateId, payload JSON, occurredAt, trạng thái). Ghi **cùng transaction** với thay đổi nghiệp vụ.
- **Relay:** chạy trong API, mỗi giây lấy sự kiện chưa giao (`FOR UPDATE SKIP LOCKED`), gọi từng listener đã đăng ký.
- **Inbox:** bảng `processed_event` (eventId, listener). Listener chỉ chạy nếu cặp này chưa có, và ghi cặp này trong cùng transaction với việc nó làm → chạy lại không có tác dụng.
- Thử lại với giãn cách tăng dần; quá 10 lần → trạng thái `DEAD`, hiện trên metrics và cảnh báo.

## Hệ quả
- Sự kiện đến muộn khoảng 1 giây; các listener hiện tại (nhả hàng, email, cập nhật trạng thái) chấp nhận được.
- Thứ tự sự kiện không được bảo đảm tuyệt đối; listener phải chịu được thứ tự lệch (ví dụ dựa vào state machine để bỏ qua chuyển trạng thái không hợp lệ).
- Muốn tách service sau này: relay gửi lên broker thay vì gọi listener trực tiếp.
