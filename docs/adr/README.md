# Architecture Decision Records (ADR)

Chỉ quyết định **khó đảo ngược** mới có ADR riêng: những thứ định hình cấu trúc code hoặc tốn nhiều công để đổi. Quyết định nhỏ, dễ đổi được ghi gọn trong bảng quyết định ở [mục 13 của tài liệu kiến trúc](../02-architecture.md#13-bảng-quyết-định).

ADR không bị sửa nội dung sau khi được chấp nhận. Nếu đổi ý, viết ADR mới và đánh dấu ADR cũ là `Bị thay thế bởi ADR-xxxx`.

## Mẫu

```markdown
# ADR-xxxx: <Tên quyết định>

- **Trạng thái:** Đề xuất | Chấp nhận | Bị thay thế bởi ADR-yyyy
- **Ngày:** YYYY-MM-DD
- **Liên quan:** <mã yêu cầu SRS, mục tài liệu kiến trúc>

## Bối cảnh
## Các phương án
## Quyết định
## Hệ quả
## Khi nào xem xét lại
```

## Danh sách

| Mã | Quyết định | Trạng thái |
|---|---|---|
| [0001](0001-modular-monolith.md) | Modular monolith theo DDD + Hexagonal, không dùng microservices | Chấp nhận |
| [0002](0002-giu-hang-bang-update-co-dieu-kien.md) | Giữ hàng bằng UPDATE có điều kiện | Chấp nhận |
| [0003](0003-phien-cookie-thay-vi-jwt.md) | Phiên đăng nhập bằng cookie thay vì JWT | Chấp nhận |
| [0004](0004-nextjs-render-phia-server.md) | Frontend Next.js, render phía server cho trang công khai | Chấp nhận |
