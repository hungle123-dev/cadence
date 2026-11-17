# ADR-0012: Cổng thanh toán online GĐ1 là VNPay sandbox

- **Trạng thái:** Đã chấp nhận (2026-11-17)
- **Ngày:** 2026-11-17
- **Liên quan:** G3, PAY-02…PAY-08, mục 1.2 (tích hợp thật: VNPay sandbox), mục 1.7 (MoMo/ZaloPay ở GĐ2)

## Bối cảnh
Cần một cổng thanh toán online để chứng minh G3: URL có chữ ký, IPN, idempotency, hết hạn, đối soát, tiền đến muộn, hoàn tiền. Dự án là portfolio, không có giấy phép kinh doanh, không thu tiền thật.

## Các lựa chọn
| Lựa chọn | Ưu | Nhược |
|---|---|---|
| A. **VNPay sandbox** | Miễn phí, đăng ký môi trường test không cần doanh nghiệp, có thẻ test; quy trình đầy đủ: redirect, IPN có checksum, API truy vấn giao dịch, API hoàn tiền; cổng phổ biến nhất ở thương mại điện tử VN | Chỉ là môi trường test: không ai trả tiền thật; chạy thật cần doanh nghiệp + hợp đồng + phí giao dịch |
| B. PayOS (chuyển khoản qua VietQR vào tài khoản ngân hàng của mình) | Nhận tiền thật được, API đơn giản, webhook có chữ ký | Chỉ chuyển khoản QR (không thẻ, không ví); nhận tiền thật thì dính tới tiền và thuế thật; cách test và phí phải kiểm tra trên payos.vn |
| C. MoMo / ZaloPay test | Ví phổ biến | Đã xếp ở GĐ2 |

## Quyết định
Chọn **A** cho GĐ1. Đăng ký tại trang sandbox của VNPay để lấy mã website (`vnp_TmnCode`) và chuỗi bí mật (`vnp_HashSecret`); hai giá trị này là bí mật, chỉ để trong `.env` và GitHub Secrets.

Port `PaymentGateway` ở `payment` không nhắc tới VNPay; adapter VNPay nằm ở `infrastructure`. **PayOS** là ứng viên tốt cho adapter thứ hai ở GĐ2 nếu muốn demo nhận tiền thật bằng chuyển khoản, cùng nhóm với MoMo/ZaloPay.

## Hệ quả
- Người review thử thanh toán bằng thẻ test của VNPay; README dự án phải ghi cách lấy thẻ test.
- IPN cần địa chỉ công khai: staging (ADR-0008), hoặc đường hầm khi chạy trên máy.
- Một số tính năng của sandbox (ví dụ QR) có thể khác bản thật; kiểm tra khi tích hợp và ghi lại.
