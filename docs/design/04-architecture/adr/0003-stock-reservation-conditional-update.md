# ADR-0003: Giữ hàng bằng UPDATE có điều kiện

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** H4, G2, N1, bất biến I2, I3; ADR-0002

## Bối cảnh
Nhiều người cùng mua những đôi cuối. Yêu cầu N1: 200 yêu cầu đồng thời tranh 10 sản phẩm → đúng 10 đơn. Cách "đọc ATS rồi mới UPDATE" sai vì hai yêu cầu cùng đọc thấy còn hàng.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. **UPDATE có điều kiện**: `UPDATE stock_item SET reserved = reserved + :q WHERE sku = :s AND on_hand - reserved - safety_stock >= :q`; 0 dòng bị ảnh hưởng = hết hàng | Nguyên tử ở DB, một câu lệnh, khoá dòng rất ngắn | Luật ATS nằm cả trong SQL lẫn trong domain; phải test cả hai |
| B. `SELECT … FOR UPDATE` rồi kiểm tra trong Java | Luật chỉ ở domain | Giữ khoá lâu hơn; dễ nghẽn khi tranh chấp cao |
| C. Optimistic lock (`version`) + thử lại | Không khoá | Tranh chấp cao thì thử lại liên tục, nhiều yêu cầu thất bại oan |
| D. Redis `DECRBY` | Rất nhanh | Thêm hạ tầng, phải đối soát về DB; dành cho launch (GĐ3) |

## Quyết định
Chọn **A**, trong transaction của ADR-0002:
- Các dòng đơn được **sắp theo SKU tăng dần** trước khi UPDATE, để hai đơn cùng chứa SKU X và Y không khoá chéo nhau (deadlock).
- Một dòng trả về 0 → ném lỗi `OUT_OF_STOCK` kèm SKU → cả transaction rollback (all-or-nothing, I3).
- `StockItem` trong domain vẫn có phương thức `reserve()` kiểm tra I2 để test domain; adapter persistence dùng UPDATE có điều kiện làm **chốt chặn cuối** ở DB.

## Hệ quả
- Phải có **test đồng thời** với Testcontainers (Postgres thật): 200 luồng / 10 sản phẩm, kiểm tra số đơn, `ats` cuối = 0, không deadlock. Kết quả ghi vào báo cáo (G2).
- Ghi số đo p95 của đặt hàng trong test; nếu xấu, so sánh với B bằng cùng test rồi cập nhật ADR này.
