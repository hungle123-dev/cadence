# ADR-0002: Đặt hàng dùng một transaction DB

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** H1, UC CHK-03, bất biến I3, I4, O4, P1; ADR-0001, ADR-0003

## Bối cảnh
Đặt hàng chạm 3 module: giữ hàng (`inventory`), tạo đơn (`order`), tạo payment (`payment`). Yêu cầu từ UC CHK-03: **không bao giờ để lại hàng giữ mà không có đơn**, và gửi lại cùng idempotency key chỉ tạo một đơn.

Tạo URL VNPay chỉ là ký HMAC trên máy, **không gọi mạng**, nên cả 3 bước đều chỉ ghi vào cùng một PostgreSQL.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. **Một transaction cục bộ** bao cả 3 bước | Đúng tuyệt đối: hoặc có đủ, hoặc không có gì. Không cần bù trừ | Gắn 3 module vào cùng transaction; tách service sau này phải đổi |
| B. Saga có bù trừ: giữ hàng (commit) → tạo đơn (commit) → payment; lỗi thì nhả hàng | Module độc lập về transaction, giống microservices | Thêm trạng thái trung gian; tiến trình chết giữa chừng để lại hàng giữ mồ côi → cần job dọn; khó test hơn nhiều |

## Quyết định
Chọn **A**. Use case `PlaceOrder` ở tầng application của `order` mở một transaction, gọi port `StockReservation` và `PaymentInitiation` (adapter gọi thẳng use case của module kia trong cùng tiến trình), lưu `Order`, ghi outbox, lưu kết quả idempotency.

Luật đi kèm:
- Đây là **chỗ duy nhất** một transaction đi qua nhiều module. Test ArchUnit/review phải giữ điều này.
- **Không gọi mạng trong transaction** (không gọi VNPay, không gửi mail). Việc gì cần mạng đi qua outbox.
- Transaction ngắn: đọc giá và phí ship **trước** khi mở transaction (bước 4–6 của UC CHK-03).

## Hệ quả
- Không cần job dọn reservation mồ côi ở GĐ1.
- Nếu sau này tách `inventory` ra service, `PlaceOrder` chuyển sang B; nhờ port đã có nên chỉ phải đổi adapter và thêm bù trừ.
- Thời gian giữ khoá phụ thuộc số dòng đơn; đo trong test đồng thời (ADR-0003).
