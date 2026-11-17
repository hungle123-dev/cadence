# 3.8 BC supporting và generic (mức CRUD)

> **Trạng thái:** Đã chốt (2026-11-17) · Thuộc [Bước 3: Thiết kế chiến thuật](README.md)

| BC | Thực thể chính | Luật chính |
|---|---|---|
| catalog | `Product` → `Colorway` (tên màu, nhóm màu, ảnh, giá niêm yết, giá sale + hiệu lực) → `Variant` (sku, size). `Category` (cây). `ProductImage` (khoá lưu trữ, alt, rộng, cao, thứ tự) | SKU duy nhất. Publish cần ≥1 colorway có ảnh và ≥1 variant. Giá sale < giá niêm yết, `from < to`. Giá đặt theo **colorway** (mọi size cùng giá). Category không có vòng, tối đa 3 cấp. Ảnh lưu như tài nguyên của mình (Q2), không lưu URL Unsplash |
| cart | `Cart` (chủ: token guest hoặc accountId) → `CartLine` (variantId, SL, giá lúc thêm) | SL mỗi dòng 1–10. Gộp giỏ: cộng SL, cắt ở 10. Giỏ guest hết hạn sau 30 ngày không dùng. Cảnh báo khi đọc (CART-03) tính lại, không lưu |
| fulfillment | `Shipment` (orderId — GĐ1 một đơn một kiện, người nhận snapshot, dòng, tiền thu hộ, mã vận đơn). `ShippingRateTable` | Trạng thái GĐ1: `CREATED → PACKED → HANDED_OVER → DELIVERED`; `HANDED_OVER → DELIVERY_FAILED → HANDED_OVER` (giao lại) hoặc `→ RETURNED`; `CREATED/PACKED → CANCELLED`. Chỉ đi tới, không lùi. Phí: bảng cố định theo phương thức, miễn phí từ 1.500.000 ₫ |
| identity | `Account` (email duy nhất, chữ thường; mật khẩu hash; vai trò; trạng thái). `Address` (sổ địa chỉ). `PasswordResetToken` (lưu hash, hạn 30 phút, dùng một lần) | Quyền theo hành động: staff được xem/xác nhận/huỷ đơn, cập nhật kiện, nhập và điều chỉnh kho; admin thêm sản phẩm, giá, category, tài khoản staff. (Prototype cho staff sửa nội dung sản phẩm; GĐ1 theo scope: chỉ admin). Chi tiết: [access-control.md](access-control.md) |
| notification | `EmailMessage` (khoá chống trùng = eventId + mẫu email) | Gửi sau commit; thử lại 3 lần, giãn cách tăng dần; lỗi không ảnh hưởng nghiệp vụ |

