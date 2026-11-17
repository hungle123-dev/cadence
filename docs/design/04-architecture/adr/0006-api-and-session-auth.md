# ADR-0006: REST + OpenAPI, cùng tên miền, đăng nhập bằng session cookie

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** G7, N4, N5, CUS-01, CUS-05

## Bối cảnh
Next.js cần gọi API khi render phía server (SEO) và từ trình duyệt (giỏ, checkout, trang quản trị). Cần đăng nhập cho customer, staff, admin; phải đăng xuất và thu hồi quyền được ngay.

## Các lựa chọn
| Câu hỏi | Lựa chọn | Chọn |
|---|---|---|
| Kiểu API | REST + JSON / GraphQL | **REST**, mô tả bằng OpenAPI, sinh client TypeScript cho Next.js |
| Đường đi | Trình duyệt gọi thẳng API ở tên miền khác (cần CORS) / **cùng tên miền qua Caddy** (`/api/*`) | **Cùng tên miền**: không CORS, cookie đơn giản |
| Đăng nhập | JWT lưu ở client / **session phía server + cookie HttpOnly** | **Session** lưu trong PostgreSQL (Spring Session JDBC) |

JWT khó thu hồi trước hạn (đăng xuất, khoá tài khoản staff) và dễ bị lưu sai chỗ (localStorage). Một backend, một tên miền thì session đơn giản và an toàn hơn.

## Quyết định
- REST + OpenAPI; lỗi nghiệp vụ trả mã ổn định (`OUT_OF_STOCK`, `PRICE_CHANGED`…) theo dạng Problem Details (RFC 9457).
- Session cookie `HttpOnly; Secure; SameSite=Lax`; chống CSRF bằng token cho các request ghi.
- Trang quản trị nằm trong cùng app Next.js (`/admin`), kiểm quyền ở **API** theo hành động (CUS-05), Next.js chỉ ẩn/hiện giao diện.
- Rate limit đăng nhập, đặt hàng, tra cứu đơn (N5) ở API.

## Hệ quả
- Không có app mobile ở GĐ1 nên không cần token cho client ngoài trình duyệt; nếu có sau này, thêm luồng token riêng.
- Next.js render phía server phải chuyển tiếp cookie của người dùng khi gọi API.
