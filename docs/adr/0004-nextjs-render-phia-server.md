# ADR-0004: Frontend Next.js, render phía server cho trang công khai

- **Trạng thái:** Chấp nhận
- **Ngày:** 2026-11-24
- **Liên quan:** SRS NFR-SEO, NFR-03, A-02; kiến trúc mục 5, 6

## Bối cảnh

Một cửa hàng online sống nhờ người tìm thấy sản phẩm trên Google. Trang chủ, danh mục và chi tiết sản phẩm cần trả về **HTML đã có nội dung** (tên, giá, mô tả, dữ liệu có cấu trúc) ngay trong response đầu tiên. Prototype là HTML/JS thuần, dữ liệu được dựng bằng JavaScript sau khi tải trang: bot thấy trang gần như trống.

Đồng thời giỏ hàng, checkout, tài khoản và admin là giao diện tương tác, dữ liệu riêng từng người, không cần SEO.

Quyết định này khó đảo ngược vì nó quyết định toàn bộ code frontend.

## Các phương án

| Phương án | Ưu | Nhược |
|---|---|---|
| **Giữ HTML/JS thuần của prototype** | Không phải viết lại | Không có SSR → SEO kém; quản lý trạng thái giao diện thủ công khi phức tạp dần |
| **SPA (React + Vite)** | Đơn giản, quen thuộc | Render ở trình duyệt → cùng vấn đề SEO |
| **Thymeleaf (Spring render HTML)** | Một codebase Java; SEO tốt | Phần tương tác (giỏ, checkout, admin) khó làm mượt; trộn giao diện vào backend, phá ranh giới Hexagonal |
| **Next.js (App Router)** | Chọn cách render theo từng trang: SSR/ISR cho trang công khai, client cho trang tương tác; có sẵn metadata, sitemap, tối ưu ảnh | Thêm một runtime Node.js phải chạy; phải học mô hình Server / Client Component |

## Quyết định

Dùng **Next.js với TypeScript**:

- Trang công khai: Server Component + ISR (tạo sẵn, làm mới định kỳ và khi admin sửa sản phẩm).
- Dữ liệu đổi từng giây (tồn kho theo size) và trang riêng của người dùng: Client Component gọi API.
- Next.js **chỉ hiển thị**: không tính tiền, không giữ phiên đăng nhập. Mọi luật nằm ở backend.
- Tái dùng thiết kế của prototype: CSS chuyển thành CSS Modules, bố cục chuyển thành component.

## Hệ quả

- **Tốt:** SEO đạt yêu cầu; trang sản phẩm tải nhanh nhờ HTML tạo sẵn.
- **Tốt:** API backend độc lập với frontend (có OpenAPI); kiểu TypeScript sinh từ OpenAPI.
- **Xấu:** phải viết lại giao diện từ prototype; thêm một container để vận hành.
- **Xấu:** HTML tạo sẵn có thể hiển thị giá cũ tối đa vài phút. Chấp nhận vì backend luôn tính lại tiền khi vào giỏ và khi đặt hàng.

## Khi nào xem xét lại

Khi SEO không còn quan trọng (ví dụ chỉ bán qua app), hoặc khi chi phí vận hành Node.js trở thành vấn đề.
