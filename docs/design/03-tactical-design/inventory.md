# 3.3 inventory (core)

> **Trạng thái:** Đã chốt (2026-11-17) · Thuộc [Bước 3: Thiết kế chiến thuật](README.md)

## Aggregate

```
StockItem (root, khoá: sku + locationId)
 ├─ onHand, reserved, safetyStock, version
 └─ ats() = onHand − reserved − safetyStock

Reservation (root, khoá: orderId — mỗi đơn một reservation)
 ├─ lines: (sku, quantity)[]
 └─ status: ACTIVE → RELEASED | COMMITTED

StockMovement (bản ghi chỉ thêm — sổ cái)
 └─ sku, locationId, type, quantity, reason?, refType/refId (đơn, phiếu điều chỉnh), actor, occurredAt
    type ∈ RECEIVE, ADJUST, RESERVE, RELEASE, COMMIT, RETURN_RESTOCK
```

**H3, quyết định:** `Reservation` là **aggregate riêng theo đơn**, không nằm trong `StockItem`.
- Nhờ biết đơn đã giữ những gì, nhả hoặc trừ kho chỉ cần `orderId`, và làm lại lần hai không có tác dụng (idempotent).
- `StockItem` chỉ giữ con số, nên khoá theo từng SKU nhỏ và nhanh.

## Bất biến

| # | Bất biến |
|---|---|
| I1 | `onHand ≥ 0`, `reserved ≥ 0`, `safetyStock ≥ 0` |
| I2 | Giữ hàng chỉ thành công khi `ats() ≥ quantity`, kiểm tra **nguyên tử** ở DB (cách khoá: H4, ADR) |
| I3 | Giữ hàng cho một đơn là **all-or-nothing**: thiếu một dòng thì không dòng nào được giữ. Cập nhật các SKU theo **thứ tự sku tăng dần** để tránh deadlock |
| I4 | Mỗi đơn có tối đa một `Reservation` (unique `orderId`); giữ lại lần hai trả kết quả cũ |
| I5 | `Reservation` chỉ nhả hoặc trừ **một lần**; trạng thái cuối không đổi |
| I6 | Điều chỉnh giảm không được làm `onHand < reserved`. Muốn giảm hơn nữa phải huỷ đơn đang giữ trước |
| I7 | Mỗi thay đổi `onHand`/`reserved` đi kèm đúng một `StockMovement` trong cùng transaction; ADJUST bắt buộc có lý do |
| I8 | `onHand` và `reserved` luôn đối chiếu được với tổng sổ cái (có test và có job kiểm tra) |

## Thời hạn giữ hàng (H5, quyết định)
- **VNPay:** reservation **không có đồng hồ riêng**. Đồng hồ duy nhất là `Payment.expiresAt` (15 phút). Hết hạn → huỷ đơn → nhả hàng qua sự kiện. Chỉ một nguồn thời hạn nên không thể lệch nhau.
- **COD:** giữ tới khi xuất kho hoặc huỷ, không có hạn. Danh sách đơn của staff làm nổi đơn chờ xác nhận quá 24 giờ. Tự động huỷ đơn COD quá hạn **ngoài GĐ1**; rate limit đặt hàng (N5) chặn việc giữ hàng ảo hàng loạt.

## Thời điểm trừ kho thật (H6, quyết định)
Trừ lúc **giao cho hãng** (`COMMIT` khi kiện chuyển `HANDED_OVER`). Lúc đóng gói hàng vẫn trong kho, còn có thể huỷ.


## Use case chi tiết

### UC INV-01: Nhập kho và điều chỉnh
Nhập kho: `quantity > 0`, sinh `RECEIVE`. Điều chỉnh: số âm hoặc dương, **bắt buộc lý do**, chặn bởi I6. Cả hai ghi người làm vào sổ cái.

