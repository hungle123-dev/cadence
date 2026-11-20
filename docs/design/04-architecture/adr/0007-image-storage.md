# ADR-0007: Ảnh lưu trong SeaweedFS (API chuẩn S3), resize bằng Next.js Image

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** Q2, CAT-05, `ProductImage` (supporting-contexts.md), ADR-0008

## Bối cảnh
Trước mắt dùng ảnh Unsplash, về sau tự tạo và **tự host** (Q2). Ảnh hiện ở nhiều kích thước (thẻ sản phẩm, trang chi tiết, zoom, ảnh nhỏ) và ảnh hưởng mạnh tới tốc độ trang và SEO.

Bản nháp đầu chọn MinIO. MinIO bản cộng đồng chuyển sang chế độ bảo trì (12/2025), không còn phát hành bản build sẵn, và repo GitHub bị archive, ghi "no longer maintained" (02/2026). Dùng phần mềm lưu trữ không còn được vá lỗi là rủi ro, nên đổi.

## Các lựa chọn
| Câu hỏi | Lựa chọn | Chọn |
|---|---|---|
| Nơi lưu | Thư mục trên ổ đĩa / MinIO (đã ngừng phát triển) / **SeaweedFS** (Apache 2.0, có cổng S3) / Garage / Cloudflare R2 (dịch vụ ngoài) | **SeaweedFS**, chạy một container `weed server -s3` |
| Resize | Lưu sẵn nhiều cỡ khi upload / imgproxy / **Next.js Image** | **Next.js Image** (có sẵn, cache kết quả) |
| Ảnh Unsplash | Trỏ thẳng URL Unsplash / **tải về một lần khi seed** | **Tải về khi seed**, ghi nguồn và tác giả |

## Quyết định
- Code backend dùng **AWS SDK (API S3)**, không dùng thư viện riêng của SeaweedFS. Đổi nơi lưu (SeaweedFS khác, R2, S3) chỉ là đổi cấu hình endpoint + khoá.
- DB chỉ lưu **khoá lưu trữ** + alt + rộng + cao + thứ tự; không lưu URL đầy đủ.
- Admin upload ảnh → API kiểm tra loại và kích thước → lưu vào bucket → trả khoá.
- Ảnh phục vụ qua Nginx (ADR-0017) tại `/media/…`; Next.js Image resize và cache theo kích thước cần.
- Script seed tải ảnh Unsplash vào bucket, nên khi thay bằng ảnh tự tạo chỉ cần đổi file, không đổi code.

## Hệ quả
- Thêm một container SeaweedFS; dữ liệu của nó phải nằm trong kế hoạch backup (ADR-0008).
- SeaweedFS cấu hình khoá S3 qua `weed shell` hoặc file cấu hình; phần này nằm trong lộ trình DevOps (D2).
- Bài học ghi lại: quyết định "chỉ phụ thuộc API S3" giúp đổi MinIO → SeaweedFS mà không ảnh hưởng thiết kế. Đây chính là lý do có port/adapter.
- Next.js Image dùng CPU của máy chủ khi resize lần đầu; nếu chậm, chuyển sang imgproxy hoặc đặt CDN (Cloudflare) phía trước.
