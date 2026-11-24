# ADR-0003: Phiên đăng nhập bằng cookie thay vì JWT

- **Trạng thái:** Chấp nhận
- **Ngày:** 2026-11-24
- **Liên quan:** SRS FR-ACC, FR-ADM-03, FR-ADM-04, NFR-SEC; kiến trúc mục 5.2, 10

## Bối cảnh

Khách hàng và nhân viên cần đăng nhập. Frontend Next.js và API Spring Boot được Nginx phục vụ **cùng một domain**. Chỉ có trình duyệt gọi API, không có app di động hay đối tác. Admin cần **khoá nhân viên có hiệu lực ngay** (FR-ADM-04), và đổi mật khẩu phải đăng xuất các phiên khác.

Next.js render phía server chỉ cho trang công khai (danh mục, sản phẩm), không cần biết người dùng là ai. Mọi trang cần đăng nhập đều chạy ở trình duyệt.

## Các phương án

| Phương án | Ưu | Nhược |
|---|---|---|
| **JWT trong `localStorage`** | Phổ biến trong tutorial; server không lưu trạng thái | Mã JS nào trên trang cũng đọc được token: một lỗi XSS là mất tài khoản. Không thu hồi được trước khi hết hạn |
| **JWT access token ngắn + refresh token trong cookie** | Thu hồi được qua refresh token | Phức tạp: xoay vòng refresh token, phát hiện dùng lại, đồng bộ giữa các tab. Vẫn phải lưu trạng thái để thu hồi, mất lợi thế "không trạng thái" |
| **BFF: Next.js giữ token, chuyển tiếp request** | Token không ra trình duyệt | Thêm một chặng cho mọi request; Next.js phải giữ bí mật và quản lý phiên. Chỉ đáng khi API nằm ở domain khác hoặc dùng OIDC |
| **Phiên server + cookie `HttpOnly`** | JS không đọc được cookie; thu hồi tức thì; Spring Security hỗ trợ sẵn | Phải lưu phiên ở server; phải chống CSRF |

## Quyết định

Dùng **phiên server với cookie `HttpOnly`, `Secure`, `SameSite=Lax`**, lưu phiên trong PostgreSQL bằng Spring Session JDBC (không mất phiên khi khởi động lại, và thêm instance sau này không cần sticky session). Trình duyệt gọi thẳng API qua Nginx; Next.js không đụng tới phiên. Bật CSRF token cho mọi request thay đổi dữ liệu. Khách hàng và nhân viên dùng hai `SecurityFilterChain`, hai bảng tài khoản, thời hạn phiên khác nhau.

## Hệ quả

- **Tốt:** bề mặt tấn công nhỏ hơn; khoá tài khoản, đăng xuất, đổi mật khẩu có hiệu lực ngay.
- **Tốt:** không cần CORS, không cần BFF.
- **Xấu:** mỗi request đọc phiên từ DB. Chấp nhận được ở quy mô này.
- **Xấu:** nếu có app di động hoặc đối tác, cần thêm cơ chế token cho các client đó.

## Khi nào xem xét lại

Khi có client không phải trình duyệt, khi cần SSO cho nhân viên (chuyển sang OIDC, backend thành resource server), hoặc khi API phải tách sang domain khác.
