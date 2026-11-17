# 3.6 Sự kiện (Published Language)

> **Trạng thái:** Đã chốt (2026-11-17) · Thuộc [Bước 3: Thiết kế chiến thuật](README.md)

Mọi sự kiện có vỏ chung: `eventId`, `type`, `version`, `occurredAt`, `aggregateId`. Payload chỉ chứa những gì bên nhận cần, để bên nhận **không phải gọi ngược**.

| Sự kiện | Phát bởi | Payload chính | Ai nghe |
|---|---|---|---|
| `OrderPlaced` | order | orderId, orderNumber, buyer email, dòng (tên, size, SL, giá), tổng, phương thức thanh toán | cart (xoá giỏ), notification |
| `OrderConfirmed` | order | orderId, orderNumber | notification (tuỳ chọn) |
| `OrderCancelled` | order | orderId, orderNumber, lý do, ai huỷ, `paymentStatus` lúc huỷ | inventory, payment, fulfillment, notification |
| `PaymentSucceeded` | payment | paymentId, orderId, amount, paidAt | order, notification |
| `PaymentFailed` | payment | paymentId, orderId | order (chỉ ghi timeline) |
| `PaymentExpired` | payment | paymentId, orderId | order |
| `LatePaymentReceived` | payment | paymentId, orderId, amount | notification (báo staff) |
| `CodCollected` | payment | paymentId, orderId, amount | order |
| `RefundRequested` / `RefundCompleted` | payment | refundId, orderId, amount | order |
| `ShipmentHandedOver` | fulfillment | shipmentId, orderId, trackingCode, dòng (sku, SL) | inventory, order, notification |
| `ShipmentDelivered` | fulfillment | shipmentId, orderId, codAmount | order, payment |
| `ShipmentDeliveryFailed` | fulfillment | shipmentId, orderId, lần thứ | order |
| `ShipmentReturned` | fulfillment | shipmentId, orderId, dòng (sku, SL) | order, inventory |
| `StockChanged` | inventory | sku, ats | catalog (cờ còn size) |
| `CustomerLoggedIn` | identity | accountId, guest cart token | cart |
| `CustomerEmailVerified` | identity | accountId, email | order (gắn đơn guest cùng email vào tài khoản, CUS-03). Bổ sung ở bước 5 |

