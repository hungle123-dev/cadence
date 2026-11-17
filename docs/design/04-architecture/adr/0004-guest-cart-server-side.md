# ADR-0004: Giỏ guest lưu ở server

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** H7, CART-01, CART-02, CART-03, bổ sung A1 (bước 3)

## Bối cảnh
Guest mua không cần tài khoản. Bổ sung A1 ở bước 3 yêu cầu đặt hàng **đọc giỏ từ server**, không tin dòng hàng client gửi. Khi đăng nhập phải gộp giỏ guest vào giỏ tài khoản.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. **Giỏ lưu trong PostgreSQL**, cookie HttpOnly chỉ chứa mã giỏ ngẫu nhiên | Server là nguồn sự thật; gộp giỏ, kiểm tra lại, đặt hàng đều đơn giản | Mỗi thao tác giỏ là một lần ghi DB; giỏ bỏ dở chiếm chỗ |
| B. Toàn bộ giỏ trong cookie / localStorage | Không tốn DB | Phải tin dữ liệu client; cookie giới hạn 4 KB; không gộp được nếu client không gửi |
| C. Redis | Nhanh, có TTL sẵn | Thêm một hạ tầng chỉ cho giỏ; lượng truy cập GĐ1 không cần |

## Quyết định
Chọn **A**. Cookie `cart_token` (HttpOnly, Secure, SameSite=Lax, 30 ngày). Giỏ guest không dùng 30 ngày bị job xoá. Đăng nhập → sự kiện `CustomerLoggedIn` → gộp giỏ (bất biến trong [supporting-contexts.md](../../03-tactical-design/supporting-contexts.md)).

## Hệ quả
- Không cần Redis ở GĐ1.
- Mã giỏ phải đủ ngẫu nhiên (≥128 bit) vì ai có mã là xem được giỏ.
