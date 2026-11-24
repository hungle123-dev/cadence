# ADR-0002: Giữ hàng bằng UPDATE có điều kiện

- **Trạng thái:** Chấp nhận
- **Ngày:** 2026-11-24
- **Liên quan:** SRS G2, BR-INV-01…05, NFR-04, NFR-07; kiến trúc mục 8.1, 9.1

## Bối cảnh

Nhiều khách có thể đặt cùng một SKU sắp hết vào cùng thời điểm. Nếu làm theo kiểu "đọc ATS → kiểm tra đủ → ghi", hai request có thể cùng đọc thấy còn 1 cái rồi cùng ghi, dẫn tới bán quá số hàng (lỗi *lost update* / *race condition*). SRS yêu cầu với ATS = 5 và 200 request đồng thời thì đúng 5 đơn thành công.

## Các phương án

| Phương án | Cách làm | Đánh giá |
|---|---|---|
| **A. Khoá bi quan** | `SELECT … FOR UPDATE` rồi kiểm tra trong Java, rồi `UPDATE` | Đúng. Nhưng giữ khoá dòng lâu hơn (qua một vòng gọi DB), dễ deadlock nếu không sắp thứ tự |
| **B. Khoá lạc quan** | Cột `@Version`; xung đột thì ném lỗi và thử lại | Đúng. Nhưng với hàng "hot" (đợt mở bán), phần lớn request xung đột và phải thử lại nhiều lần → chậm, tốn tài nguyên |
| **C. UPDATE có điều kiện** | `UPDATE … SET reserved = reserved + q WHERE … AND on_hand - reserved - safety_stock >= q`, xem số dòng bị ảnh hưởng | Đúng. Một câu lệnh, DB tự khoá dòng trong thời gian ngắn nhất. Không cần thử lại |
| **D. Khoá phân tán (Redis)** | Lấy khoá theo SKU trước khi thao tác | Thêm một hệ thống, thêm các kiểu lỗi mới (khoá hết hạn giữa chừng). Không cần khi đã chỉ có một DB |

## Quyết định

Chọn **C**, kèm:

1. Ràng buộc `CHECK (reserved >= 0 AND reserved <= on_hand)` trong DB làm lớp phòng thủ cuối.
2. Khi một đơn có nhiều SKU, giữ hàng theo thứ tự `variant_id` tăng dần để tránh deadlock.
3. Tất cả nằm trong giao dịch tạo đơn: một SKU thất bại thì rollback toàn bộ.
4. Áp dụng cùng kỹ thuật cho lượt dùng voucher (`used < usage_limit`).
5. Câu lệnh viết bằng `NamedParameterJdbcTemplate` trong một adapter của module `inventory` (`InventoryReservationJdbcAdapter`, cài đặt `InventoryReservationPort`), không qua JPA, để SQL rõ ràng và không bị Hibernate đổi thành đọc-rồi-ghi.

**Đánh đổi với DDD:** theo DDD thuần, luật "ATS ≥ số lượng" phải nằm trong aggregate `InventoryItem` (tải lên, gọi `reserve()`, lưu xuống). Làm vậy chính là "đọc rồi ghi" và cần khoá lạc quan, tức phương án B. Ở đây chấp nhận để luật này nằm trong câu SQL vì tính đúng dưới tải quan trọng hơn sự thuần khiết của mô hình. Các thao tác ít tranh chấp (nhập kho, điều chỉnh kiểm kê) vẫn đi qua aggregate như bình thường.

## Hệ quả

- **Tốt:** đúng theo cách DB đảm bảo, không phụ thuộc vào code Java viết cẩn thận tới đâu. Nhanh dưới tải cao.
- **Tốt:** dễ giải thích và dễ chứng minh bằng test đồng thời.
- **Xấu:** logic kiểm tra tồn kho nằm một phần trong SQL. Bù lại bằng việc gom các câu lệnh này vào một repository duy nhất của module `inventory`, có test riêng.
- **Xấu:** khi thất bại chỉ biết "không đủ hàng", không biết còn bao nhiêu. Nếu cần báo con số cho khách thì đọc lại sau khi thất bại (chấp nhận được vì chỉ để hiển thị).

## Khi nào xem xét lại

Khi tồn kho tách ra nhiều kho hoặc cần giữ hàng ở hệ thống khác DB chính.
