# Bước 3: Thiết kế chiến thuật

> **Trạng thái:** Đã chốt (2026-11-17) · Thuộc [tài liệu thiết kế](../../README.md) · Trước: [Bước 2](../02-strategic-design.md) · Sau: [Bước 4](../04-architecture/README.md)

> Theo cách đã thống nhất: **domain-first, làm kỹ cho 3 BC core** (`order`, `inventory`, `payment`): aggregate, bất biến, state machine, use case chi tiết. Các BC còn lại chỉ ghi thực thể và luật chính, làm theo kiểu CRUD.
> Ký hiệu bất biến: `O` = order, `I` = inventory, `P` = payment. Mã này dùng lại trong test (mỗi bất biến có ít nhất một test domain).

## Các tài liệu của bước này

| Tài liệu | Nội dung | Mức chi tiết |
|---|---|---|
| [order.md](order.md) | 3.2 · Aggregate `Order`, bất biến O1–O8, state machine, mã đơn; UC CHK-03, ORD-02, ORD-03/04 | Core, đầy đủ |
| [inventory.md](inventory.md) | 3.3 · `StockItem`, `Reservation`, sổ cái, bất biến I1–I8, thời hạn giữ hàng; UC INV-01 | Core, đầy đủ |
| [payment.md](payment.md) | 3.4 · `Payment`, `Refund`, state machine, bất biến P1–P9; UC PAY-03, PAY-05/06 | Core, đầy đủ |
| [events.md](events.md) | 3.6 · Danh sách sự kiện giữa các BC (hợp đồng) | Dùng chung |
| [supporting-contexts.md](supporting-contexts.md) | 3.8 · catalog, cart, fulfillment, identity, notification | CRUD, chỉ luật chính |
| [access-control.md](access-control.md) | 3.10 · Quyền theo hành động, ma trận vai trò × quyền, luật sở hữu cho khách, luật phiên AC1–AC10 (bổ sung sau khi chốt) | Dùng chung |

## 3.1 Nguyên tắc chung

1. Aggregate tham chiếu nhau **bằng id**, không giữ object của aggregate khác.
2. Mọi thay đổi trạng thái đi qua **phương thức có tên nghiệp vụ** (`order.confirm(by)`, `payment.capture(ipn)`), không có setter `setStatus`. Chuyển trạng thái không hợp lệ ném lỗi domain có mã.
3. Mỗi transaction sửa **một aggregate**. Ngoại lệ duy nhất có chủ đích: giữ hàng cho cả đơn sửa nhiều `StockItem` trong một transaction (mục 3.3).
4. Tiền là `Money` (số nguyên VND, không âm, không số lẻ). Thời gian lưu UTC, lấy qua `Clock` để test được.
5. Mỗi aggregate có `version` (optimistic lock) để phát hiện hai người sửa cùng lúc.
6. Sự kiện được ghi vào outbox **cùng transaction** với thay đổi; listener phải idempotent theo `eventId`.


## 3.7 Điều chỉnh context map của bước 2

Khi viết use case chi tiết, lộ ra ba chỗ bước 2 còn thiếu. Bổ sung ở đây, không đổi ranh giới BC:

| # | Bổ sung | Lý do |
|---|---|---|
| A1 | **cart → order**: port `CartSnapshot` (đồng bộ); **order → cart**: sự kiện `OrderPlaced` để xoá giỏ | Đặt hàng phải đọc giỏ từ server, không tin dòng hàng client gửi |
| A2 | Port `OrderForShipping` từ fulfillment sang order là **lệnh** `startFulfillment` (trả về snapshot người nhận + dòng), không chỉ là truy vấn | Chặn tranh chấp huỷ đơn / đóng gói (3.2.2) |
| A3 | payment nghe `OrderCancelled` để chuyển Payment `PENDING` → `VOIDED` (ngoài việc hoàn tiền ở P6 của bước 2) | Tiền đến sau khi huỷ đi đúng đường hoàn tiền |

## 3.9 Trạng thái các điểm nóng

| # | Kết quả |
|---|---|
| H1 | **Còn mở** → ADR bước 4 (một transaction hay bù trừ cho đặt hàng). Use case CHK-03 đã ghi yêu cầu: không để lại hàng giữ mà không có đơn |
| H2 | Đã quyết: đơn giữ bản sao trục payment và fulfillment (3.2.2) |
| H3 | Đã quyết: `Reservation` là aggregate riêng theo đơn (3.3) |
| H4 | **Còn mở** → ADR bước 4. Đề xuất: UPDATE có điều kiện; ADR so sánh với `FOR UPDATE` và optimistic lock bằng số đo của test đồng thời |
| H5 | Đã quyết: VNPay dùng một đồng hồ (`Payment.expiresAt`); COD không hạn, làm nổi sau 24 giờ (3.3) |
| H6 | Đã quyết: trừ kho lúc giao cho hãng (3.3) |
| H7 | **Còn mở** → ADR bước 4 (giỏ guest lưu ở server hay cookie) |
| H8 | Đã quyết: mã đơn ngẫu nhiên + tra cứu cần mã và email/SĐT + rate limit (3.2) |
