# Bước 1: Phạm vi

> **Trạng thái:** Đã chốt (2026-11-16) · Thuộc [tài liệu thiết kế](../README.md) · Sau: [Bước 2](02-strategic-design.md)

## 1.1 Bối cảnh

Website bán hàng trực tiếp (D2C) của **một thương hiệu giày & quần áo thể thao giả định**, thị trường Việt Nam: VND, địa chỉ VN, COD, VNPay.

Đây là dự án portfolio làm một mình, nhưng được xây như hệ thống sẽ chạy thật: đúng domain, nhất quán dữ liệu, có test, quan sát được, có deploy.

## 1.2 Ràng buộc

| Ràng buộc | Giá trị |
|---|---|
| Thời gian giai đoạn 1 | ~3 tháng × 15h/tuần ≈ **180 giờ** |
| Nhân lực | 1 người |
| Backend | Java, OOP, DDD + Hexagonal + Clean Architecture (theo template) |
| Frontend | Next.js (SSR/ISR cho SEO) |
| Thị trường | Chỉ Việt Nam, chỉ VND, chỉ tiếng Việt |
| Tích hợp thật (sandbox) | VNPay |
| Tích hợp giả lập | Vận chuyển (GHN thật ở GĐ2), email gửi qua SMTP dev |

## 1.3 Mục tiêu: dự án phải chứng minh được gì

| # | Năng lực | Bằng chứng cụ thể |
|---|---|---|
| G1 | Mô hình domain đúng | Catalog 3 tầng, đơn hàng snapshot, trạng thái tách trục, state machine có guard |
| G2 | Không bán quá số hàng | Test đồng thời: N request tranh M sản phẩm → đúng M đơn, 0 oversell |
| G3 | Thanh toán an toàn | IPN có xác thực chữ ký, idempotent; job đối soát; xử lý thanh toán đến muộn |
| G4 | Kiến trúc có kỷ luật | Module theo bounded context; luật phụ thuộc được ép bằng ArchUnit |
| G5 | Chất lượng | Unit test domain, test tích hợp DB thật (Testcontainers), E2E luồng mua hàng, CI |
| G6 | Vận hành được | Log có cấu trúc + correlation id, metrics, health check, deploy được, có dữ liệu mẫu |
| G7 | SEO | PLP/PDP render phía server, metadata, sitemap, structured data `Product` |

## 1.4 Tác nhân trong giai đoạn 1

| Tác nhân | Mô tả |
|---|---|
| **Guest** | Mua hàng không cần tài khoản, tra cứu đơn bằng mã đơn + email/SĐT |
| **Customer** | Có tài khoản email/mật khẩu |
| **Staff** | Nhân viên vận hành: xử lý đơn, kho, giao hàng |
| **Admin** | Toàn quyền, gồm quản lý sản phẩm, giá, tài khoản staff |
| **VNPay** | Hệ thống ngoài: thanh toán + IPN |
| **Scheduler** | Tác nhân hệ thống: job hết hạn thanh toán, job đối soát |

## 1.5 Use case TRONG phạm vi giai đoạn 1

Mức ưu tiên: **M** = Must (bắt buộc cho GĐ1) · **S** = Should (làm nếu kịp, cắt đầu tiên khi trễ).

### Catalog
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| CAT-01 | Xem PLP theo category, mỗi ô là một colorway | Guest/Customer | M |
| CAT-02 | Lọc PLP theo size còn hàng, nhóm màu, khoảng giá, giới tính; sắp xếp theo mới nhất/giá | Guest/Customer | M |
| CAT-03 | Xem PDP: đổi colorway, chọn size (size hết hàng bị khoá), bảng size | Guest/Customer | M |
| CAT-04 | Tìm kiếm tiếng Việt không dấu (Postgres full-text; search engine riêng ở GĐ2) | Guest/Customer | S |
| CAT-05 | Admin tạo/sửa Product → Colorway → Variant, upload ảnh theo colorway | Admin | M |
| CAT-06 | Admin publish/archive sản phẩm; hẹn giờ ra mắt | Admin | M |
| CAT-07 | Admin đặt giá niêm yết và giá sale có thời gian hiệu lực | Admin | M |
| CAT-08 | Quản lý category (cây phân cấp) | Admin | M |

### Inventory
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| INV-01 | Nhập kho, điều chỉnh tồn (bắt buộc ghi lý do); mọi thay đổi ghi vào sổ cái | Staff | M |
| INV-02 | Giữ hàng nguyên tử cho cả đơn (all-or-nothing) khi đặt hàng | Hệ thống | M |
| INV-03 | Nhả hàng khi đơn huỷ hoặc hết hạn thanh toán | Hệ thống | M |
| INV-04 | Trừ kho thật khi xuất kho | Hệ thống | M |
| INV-05 | Xem tồn kho và lịch sử biến động theo SKU | Staff | M |

> Một kho duy nhất, nhưng mô hình vẫn có `location` để mở rộng.

### Cart & Checkout
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| CART-01 | Thêm/sửa/xoá dòng giỏ; giỏ guest bằng cookie | Guest/Customer | M |
| CART-02 | Gộp giỏ guest vào giỏ tài khoản khi đăng nhập | Customer | M |
| CART-03 | Kiểm tra lại giỏ mỗi lần đọc: hết hàng, đổi giá, ngừng bán → cảnh báo | Hệ thống | M |
| CHK-01 | Nhập thông tin liên hệ + địa chỉ VN (tỉnh/thành → phường/xã, sau sắp xếp 2025) | Guest/Customer | M |
| CHK-02 | Tính phí ship theo bảng cố định (giả lập), freeship theo ngưỡng | Hệ thống | M |
| CHK-03 | Đặt hàng: tính lại giá, phát hiện đổi giá, giữ hàng, tạo đơn snapshot; idempotency key | Guest/Customer | M |

### Payment
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| PAY-01 | Thanh toán COD | Guest/Customer | M |
| PAY-02 | Tạo giao dịch VNPay (URL có chữ ký, thời hạn ≤ TTL giữ hàng) | Hệ thống | M |
| PAY-03 | Xử lý IPN VNPay: xác thực chữ ký, khớp số tiền, idempotent, outbox | VNPay | M |
| PAY-04 | Trang kết quả (Return URL), chỉ đọc trạng thái từ DB | Guest/Customer | M |
| PAY-05 | Job hết hạn: Payment pending quá TTL → hết hạn, huỷ đơn, nhả hàng | Scheduler | M |
| PAY-06 | Job đối soát: hỏi VNPay các giao dịch pending quá lâu | Scheduler | M |
| PAY-07 | Thanh toán đến sau khi đơn hết hạn → ghi nhận tiền và đưa vào hàng đợi hoàn tiền | Hệ thống | M |
| PAY-08 | Hoàn tiền qua API VNPay sandbox (khi huỷ đơn đã trả tiền) | Staff | S |
| PAY-09 | Ghi nhận hoàn tiền thủ công (phương án dự phòng cho PAY-08) | Staff | M |

### Order
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| ORD-01 | Tạo đơn snapshot với mã đơn đọc được; trạng thái tách trục (order / payment / fulfillment) | Hệ thống | M |
| ORD-02 | Staff xác nhận đơn COD (bước "chờ xác nhận") | Staff | M |
| ORD-03 | Khách tự huỷ đơn khi chưa đóng gói | Guest/Customer | M |
| ORD-04 | Staff huỷ đơn (kèm lý do) | Staff | M |
| ORD-05 | Timeline đơn: ai, lúc nào, làm gì | Staff | M |
| ORD-06 | Danh sách + chi tiết đơn cho staff (lọc theo trạng thái, tìm theo mã/SĐT) | Staff | M |
| ORD-07 | Khách xem "Đơn của tôi"; guest tra cứu bằng mã đơn + email/SĐT | Guest/Customer | M |

### Fulfillment (giả lập vận chuyển)
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| FUL-01 | Tạo kiện hàng cho đơn (GĐ1: mỗi đơn 1 kiện) | Staff | M |
| FUL-02 | Cập nhật kiện: đóng gói → giao hãng (nhập mã vận đơn) → đã giao / giao thất bại → hoàn về | Staff | M |
| FUL-03 | Giao thành công đơn COD → ghi nhận đã thu tiền | Hệ thống | M |
| FUL-04 | Hoàn về → huỷ đơn, nhập lại kho | Hệ thống | S |

> Thao tác thủ công của staff ở FUL-02 sau này được thay bằng webhook GHN qua **cùng một port**.

### Customer & Identity
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| CUS-01 | Đăng ký, đăng nhập, đăng xuất bằng email/mật khẩu | Customer | M |
| CUS-02 | Sổ địa chỉ | Customer | S |
| CUS-03 | Gắn đơn guest vào tài khoản sau khi xác minh email | Customer | S |
| CUS-04 | Quên mật khẩu qua email | Customer | S |
| CUS-05 | Phân quyền staff/admin theo hành động | Admin | M |

### Notification
| ID | Use case | Tác nhân | Ưu tiên |
|---|---|---|---|
| NOT-01 | Email: đặt hàng, đã thanh toán, đã giao cho hãng, đã huỷ (gửi sau commit, lỗi gửi không ảnh hưởng nghiệp vụ) | Hệ thống | M |

## 1.6 Yêu cầu phi chức năng (NFR)

| # | Yêu cầu | Mục tiêu đo được |
|---|---|---|
| N1 | Nhất quán tồn kho | Test đồng thời ≥ 200 request cho 10 sản phẩm → đúng 10 đơn |
| N2 | Idempotency | Gửi lại đặt hàng/IPN N lần → đúng 1 tác dụng |
| N3 | Hiệu năng đọc | API catalog p95 < 300 ms ở tải vừa phải (đo bằng k6, ghi kết quả) |
| N4 | SEO | PLP/PDP SSR/ISR; title/description/canonical; sitemap; JSON-LD `Product` |
| N5 | Bảo mật | Mật khẩu hash bằng bcrypt/argon2; rate limit đăng nhập và đặt hàng; không commit secret; HMAC cho VNPay |
| N6 | Quan sát | Log JSON + correlation id; metrics nghiệp vụ (đơn/phút, tỷ lệ thanh toán thành công, payment pending quá hạn); health check |
| N7 | Kiểm thử | Domain unit test; test tích hợp Postgres thật; ArchUnit cho mọi module; E2E cho luồng mua COD và VNPay |
| N8 | CI/CD | Mỗi push chạy build + test; deploy staging tự động hoặc bằng một lệnh |
| N9 | Dữ liệu | Migration có phiên bản; seed ~30 sản phẩm × vài colorway × size |
| N10 | Thời gian | Lưu UTC, hiển thị giờ VN; tiền là số nguyên VND |

## 1.7 NGOÀI phạm vi giai đoạn 1

| Tính năng | Lý do để sau | Dự kiến |
|---|---|---|
| Khuyến mãi / voucher / phân bổ giảm giá | Engine lớn; cần lõi đơn hàng ổn định trước. Mô hình đơn vẫn chừa chỗ cho giảm giá | GĐ2 |
| Đổi trả & hoàn tiền một phần | Phụ thuộc phân bổ giảm giá | GĐ2 |
| GHN thật (tính phí, vận đơn, webhook) | Giả lập trước qua cùng port | GĐ2 |
| MoMo / ZaloPay | Thêm adapter cho port có sẵn | GĐ2 |
| Search engine riêng (OpenSearch/Meilisearch) | GĐ1 dùng Postgres full-text | GĐ2 |
| Wishlist, sản phẩm đã xem, báo khi có hàng lại | Không thuộc luồng lõi | GĐ2 |
| Membership, hạng, điểm thưởng | Cần đổi trả trước (để trừ điểm) | GĐ3 |
| Launch / Draw + waiting room + load test lớn | Điểm nhấn cuối, cần lõi vững | GĐ3 |
| Đối soát COD với hãng vận chuyển | Cần GHN thật | GĐ3 |
| Đối soát toàn bộ với báo cáo của cổng thanh toán (chuyển các ca lệch cho người xem) | GĐ1 chỉ hỏi các giao dịch chưa biết kết quả (PAY-06); cần báo cáo từ cổng, sandbox chưa chắc có | GĐ2–3 |
| Hoá đơn điện tử (mock provider) | Không ảnh hưởng luồng mua | GĐ3 |
| Headless CMS | GĐ1 trang chủ/banner cấu hình tĩnh | Chưa định |
| Đăng nhập OTP SĐT, Google | Tốn công tích hợp | Chưa định |
| Nhiều kho, chọn kho xuất | Một kho đủ cho demo | Chưa định |
| Review, đa ngôn ngữ, analytics | Ngoài trọng tâm | Chưa định |

## 1.8 Ước lượng giai đoạn 1

| Hạng mục | Giờ |
|---|---|
| Thiết kế (bước 2–5) | 25 |
| Walking skeleton: repo, CI, docker-compose, ArchUnit, deploy staging | 20 |
| Catalog + admin catalog + PLP/PDP SEO | 35 |
| Inventory | 15 |
| Cart + Checkout | 25 |
| Order + COD + staff xử lý đơn | 20 |
| VNPay (URL, IPN, hết hạn, đối soát, thanh toán muộn) | 20 |
| Fulfillment giả lập + email | 10 |
| Identity + phân quyền | 10 |
| **Tổng** | **180** |

Ước lượng không có dư. Khi trễ, cắt theo thứ tự: CAT-04 → CUS-03 → CUS-02 → CUS-04 → PAY-08 → FUL-04.

**Giữ 180 giờ (quyết định 2026-11-17).** Phần CI/CD/DevOps bạn tự làm (lộ trình D1–D8, [bước 4](04-architecture/README.md) mục 4.6) không thêm giờ: D1–D6 nằm trong 20 giờ walking skeleton; D7 (backup) và D8 (giám sát) làm trong các lát, tính vào phần N6/N8. Nếu trễ, cắt theo thứ tự trên; riêng DevOps thì bỏ Loki (D8) trước, giữ log JSON + metrics + health check để vẫn đạt N6.

## 1.9 Định nghĩa "xong" cho giai đoạn 1

Một người review, chỉ với link staging và README, có thể:
1. Duyệt PLP, lọc, xem PDP; trang có metadata SEO và JSON-LD.
2. Mua bằng **COD** với tư cách guest; nhận email xác nhận; tra cứu đơn.
3. Mua bằng **VNPay sandbox** với tư cách customer; thấy đơn chuyển sang đã thanh toán qua IPN.
4. Đăng nhập staff: xác nhận đơn COD, tạo kiện, cập nhật tới "đã giao"; xem timeline và lịch sử tồn kho.
5. Đọc trong repo: tài liệu thiết kế này, ADR, ERD, **báo cáo test đồng thời** (0 oversell), và thấy CI xanh với ArchUnit + Testcontainers.

## 1.10 Quyết định

| # | Câu hỏi | Quyết định |
|---|---|---|
| Q1 | Tên thương hiệu giả định | **Cadence** (không dùng tên/logo Nike, adidas, Puma) |
| Q2 | Ảnh sản phẩm | Không lấy ảnh từ site các hãng. Trước mắt dùng Unsplash (giấy phép Unsplash). Về sau tự tạo ảnh (AI, đồng bộ nền và góc chụp, không logo) và **tự host**. Vì vậy hệ thống lưu ảnh như tài nguyên của mình (khoá lưu trữ, kích thước, alt), không gắn cứng vào URL Unsplash; nơi lưu và cách resize quyết định ở bước 4 |
| Q3 | Nơi deploy & ngân sách | Đã chốt ở [ADR-0008](04-architecture/adr/0008-deploy-single-vps-docker-compose.md): 1 VPS (~5–7 USD/tháng) + Docker Compose + Nginx ([ADR-0017](04-architecture/adr/0017-reverse-proxy-nginx.md), thay Caddy). Bạn tự làm phần CI/CD/DevOps để học |
| Q4 | Số lượng dữ liệu mẫu | ~30 sản phẩm là đủ demo lọc/PLP |

## 1.11 Prototype và phạm vi

Prototype HTML (`prototype/`) là **tầm nhìn sản phẩm**, không phải phạm vi giai đoạn 1. Nó minh hoạ cả một số tính năng thuộc mục 1.7: voucher, wishlist, bốc thăm ra mắt, Cadence Club, báo khi có hàng lại, đánh giá. Khi viết code thật, giai đoạn 1 chỉ làm các use case ở 1.5; các phần trên chưa làm (ẩn khỏi giao diện hoặc để dạng "sắp ra mắt").

