# Đặc tả yêu cầu phần mềm (SRS) — Cadence

| | |
|---|---|
| **Sản phẩm** | Cadence: website bán giày và đồ thể thao (storefront + trang quản trị) |
| **Phiên bản tài liệu** | 1.5 |
| **Trạng thái** | Đã duyệt (baseline). Mọi thay đổi sau này ghi vào lịch sử thay đổi |
| **Nguồn tham khảo** | Prototype trong `prototype/` (16 trang HTML, dữ liệu giả lập). Chỉ là bản phác để tham khảo, xem A-05 |
| **Tài liệu liên quan** | `02-architecture.md`, `adr/`, `03-database.md`, `04-test-plan.md` |

### Lịch sử thay đổi

| Phiên bản | Ngày | Thay đổi |
|---|---|---|
| 0.1 | 2026-11-24 | Bản đầu, rút yêu cầu từ prototype |
| 1.0 | 2026-11-24 | Bỏ mục kịch bản demo. Chốt 8 câu hỏi mở (mục 10); thêm BR-PRC-07, BR-VCH-06, BR-ORD-05, BR-ORD-06, FR-ACC-08; cập nhật FR-ORD-06, FR-CART-02, BR-VCH-05, FR-ADM-01, mục 6.1 |
| 1.1 | 2026-11-24 | Frontend chuyển sang Next.js: sửa A-02, thêm nhóm NFR-SEO (mục 7.7). Bổ sung yêu cầu IAM: NFR-SEC-07, NFR-SEC-08; FR-ADM-04 và FR-ACC-06 thu hồi phiên ngay; MFA cho ADMIN vào giai đoạn 2 |
| 1.2 | 2026-11-24 | Sau review: FR-CAT-02 ghi rõ lọc size chỉ tính size còn hàng; thêm FR-PAY-12 (đối soát tự động hằng đêm); mục 8 tách PaymentTransaction / Refund, trạng thái thanh toán thuộc Order; thêm StaffLoginLog cho NFR-SEC-07 |
| 1.3 | 2026-11-24 | Ghi rõ ảnh hiện tại là ảnh mock (A-04); upload ảnh riêng lên object storage ở giai đoạn 2; FR-ADM-PRD-02 và mục 2.2 cập nhật theo |
| 1.4 | 2026-11-24 | Review độ rõ ràng: chốt thêm 30 quyết định D-09…D-38 (mục 10). Prototype chỉ là bản tham khảo (A-05). Thêm C-08, C-09, FR-CART-08, FR-CHK-12, FR-CHK-13, FR-ADM-05, nhóm FR-CNT (trang nội dung), nhóm NFR-DEMO (môi trường demo), BR-PRC-08, BR-ORD-07; sửa FR-CAT-02, FR-CART-03, FR-CHK-02, FR-CHK-03, FR-PAY-07, FR-PAY-10, FR-PAY-11, FR-ORD-01, FR-ACC-01, FR-ACC-05, FR-ADM-01, FR-ADM-02, FR-ADM-ORD-07, FR-ADM-ORD-08, FR-ADM-ORD-10, FR-ADM-PRD-03, BR-PRC-03, BR-VCH-01, BR-VCH-04, BR-INV-01, NFR-SEC-02, NFR-SEC-08, mục 2.1, 3, 6, 9 |
| 1.5 | 2026-11-24 | Kiểm chéo toàn bộ tài liệu (mỗi yêu cầu đối chiếu với mọi yêu cầu cùng đối tượng). Chốt D-39…D-60 (mục 10). Thêm vòng đời sản phẩm (mục 6.5), khuyến mãi có thời hạn (BR-PRC-09), ảnh lưu theo khoá (BR-CAT-01), link truy cập đơn cho khách vãng lai (FR-ORD-07), nhãn suy ra (FR-CAT-09), slug cố định (BR-CAT-02), FR-ADM-06, FR-ADM-PRD-08, FR-ADM-PRD-09. Sửa mâu thuẫn FR-CAT-07 × NFR-SEO-06, BR-INV-04, mục 6.2 (thêm `EXPIRED → REFUND_PENDING`, `FAILED → PENDING`), mục 2.1, 2.2, 3.1, 8; đổi chỗ mục 4.11 và 4.12 |

### Cách đọc tài liệu này

- Mỗi yêu cầu có mã để truy vết: `FR-` (chức năng), `BR-` (quy tắc nghiệp vụ), `NFR-` (phi chức năng).
- **Ưu tiên** theo MoSCoW: **M** = phải có trong MVP, **S** = nên có, **C** = có thì tốt, **W** = không làm lần này.
- Mục 2.4 liệt kê các giả định nền của dự án. Mục 10 ghi lại các quyết định đã chốt và lý do.

---

## 1. Giới thiệu

### 1.1 Mục đích

Tài liệu này mô tả hệ thống Cadence **cần làm gì**, để làm căn cứ cho thiết kế kiến trúc, thiết kế dữ liệu, lập kế hoạch và kiểm thử. Tài liệu không mô tả **làm như thế nào** (công nghệ, cấu trúc code). Phần đó thuộc tài liệu kiến trúc và các ADR.

### 1.2 Bối cảnh và mục tiêu dự án

Cadence là thương hiệu giả định bán giày và đồ thể thao cho người Việt. Đây là **dự án cá nhân để đưa vào portfolio**, nên có hai nhóm mục tiêu:

| Mục tiêu | Cách đo |
|---|---|
| **G1.** Hệ thống chạy thật đầu đến cuối: khách đặt hàng, thanh toán qua VNPay sandbox, admin xử lý đến lúc giao xong | Có bản demo online kèm tài khoản dùng thử |
| **G2.** Không bao giờ bán quá số hàng trong kho, kể cả khi nhiều người mua cùng lúc | Test đồng thời ở NFR-04 đạt |
| **G3.** Tiền luôn khớp: tổng đơn, giảm giá phân bổ và trạng thái thanh toán đúng tới từng đồng, kể cả khi cổng thanh toán gọi lại nhiều lần | Test ở NFR-05, NFR-06, NFR-07 đạt |
| **G4.** Chất lượng như sản phẩm thật: có test tự động, CI, tài liệu, log | Mục 7 đạt |

Vì là dự án portfolio, phạm vi được **cắt hẹp có chủ đích**: làm ít tính năng nhưng làm sâu những luồng khó (giữ hàng, thanh toán, vòng đời đơn).

### 1.3 Thuật ngữ

| Thuật ngữ | Nghĩa |
|---|---|
| **Storefront** | Phần website khách hàng dùng |
| **Admin** | Trang quản trị cho nhân viên |
| **PLP / PDP** | Trang danh sách sản phẩm / trang chi tiết sản phẩm |
| **Product → Colorway → Variant** | Sản phẩm → phối màu → biến thể theo size. Variant là đơn vị bán và tồn kho nhỏ nhất, có mã **SKU** |
| **On hand** | Tồn thực tế đang nằm trong kho |
| **Reserved** | Số lượng đang được giữ cho các đơn chưa xuất kho |
| **Safety stock** | Tồn an toàn: số lượng giữ lại, không bán, để phòng sai lệch kiểm kê |
| **ATS** (Available to sell) | Số lượng còn bán được = On hand − Reserved − Safety stock |
| **Ledger** | Sổ cái tồn kho: ghi lại mọi biến động, chỉ thêm, không sửa, không xoá |
| **COD** | Thanh toán tiền mặt khi nhận hàng |
| **IPN** | Instant Payment Notification: VNPay gọi từ server của họ sang server của ta để báo kết quả thanh toán |
| **Return URL** | Trang VNPay chuyển trình duyệt của khách về sau khi thanh toán. **Không** dùng để xác nhận tiền |
| **Idempotency** | Gửi cùng một yêu cầu nhiều lần thì kết quả vẫn như gửi một lần |
| **Snapshot** | Bản chụp dữ liệu tại thời điểm đặt hàng (tên, giá, size…), không đổi theo dữ liệu sản phẩm về sau |
| **Đang bán / Sắp mở bán** | Hai cách hiển thị của một sản phẩm ở trạng thái `PUBLISHED`, phân biệt bằng giờ mở bán (mục 6.5) |
| **Khuyến mãi** (sale) | Giá khuyến mãi của sản phẩm kèm khoảng thời gian hiệu lực (BR-PRC-09). Khác với **voucher** là mã khách tự nhập |
| **Khoá ảnh** (image key) | Đường dẫn tương đối của ảnh bên trong nguồn ảnh đang cấu hình. URL đầy đủ được ghép lúc hiển thị (BR-CAT-01) |
| **Giao dịch thanh toán** | Một lần thử thanh toán với VNPay (`PaymentTransaction`). Khác với **trạng thái thanh toán của đơn** (mục 6.2) |
| **Yêu cầu hoàn tiền** | Một bản ghi `Refund`: đang xử lý, đã hoàn, hoặc thất bại |

---

## 2. Mô tả tổng quan

### 2.1 Vai trò người dùng

| Vai trò | Mô tả | Quyền chính |
|---|---|---|
| **Khách vãng lai** | Chưa đăng nhập | Xem, tìm sản phẩm; dùng giỏ hàng; đặt hàng bằng email + số điện thoại; tra cứu và huỷ đơn của mình |
| **Hội viên** (Cadence Club) | Đã đăng ký và đăng nhập | Như khách vãng lai, cộng thêm: lưu địa chỉ, xem lịch sử đơn, miễn phí giao Tiêu chuẩn (BR-PRC-07). Danh sách yêu thích: MVP lưu trong trình duyệt nên khách vãng lai cũng dùng được; lưu theo tài khoản ở giai đoạn 2 |
| **Nhân viên vận hành** (`STAFF`) | Xử lý đơn và kho hằng ngày | Xác nhận, đóng gói, giao, huỷ đơn; hoàn tiền đơn có số tiền ≤ 2.000.000 ₫; nhập và điều chỉnh tồn kho; tạo và sửa nội dung sản phẩm. **Không** được đặt giá, khuyến mãi, đăng bán hay ngừng bán sản phẩm |
| **Quản trị viên** (`ADMIN`) | Chủ cửa hàng | Toàn quyền, gồm đặt giá và khuyến mãi, đăng bán và ngừng bán sản phẩm, hoàn tiền không giới hạn, quản lý tài khoản nhân viên |
| **Hệ thống** | Tác vụ tự động | Huỷ đơn VNPay hết hạn thanh toán (FR-PAY-09), huỷ đơn COD quá 48 giờ chưa xác nhận (BR-ORD-05), chuyển đơn `COMPLETED` (BR-ORD-04), đối soát thanh toán (FR-PAY-12), gửi email (FR-NTF), dựng lại dữ liệu demo (NFR-DEMO-02) |

### 2.2 Hệ thống bên ngoài

| Hệ thống | Dùng để | Ghi chú |
|---|---|---|
| **VNPay sandbox** | Thanh toán online | Tạo URL thanh toán, nhận IPN, truy vấn giao dịch (querydr), hoàn tiền (refund) |
| **Dịch vụ email** | Gửi email giao dịch | Mọi môi trường gửi qua SMTP. Dev và demo trỏ tới Mailpit chạy nội bộ: thư bị giữ lại, không ra Internet, và trang quản lý của Mailpit **không** mở công khai (NFR-DEMO-03). Gửi thư thật chỉ cần đổi cấu hình SMTP, không đổi code |
| **Nguồn ảnh** | Ảnh sản phẩm | Hệ thống chỉ lưu **khoá ảnh** (BR-CAT-01). MVP: nguồn ảnh là ảnh mock (A-04). Giai đoạn 2: object storage tương thích S3 cho ảnh riêng; chỉ đổi cấu hình nguồn ảnh, không sửa dữ liệu |

Đơn vị vận chuyển (GHN, GHTK…) **không** tích hợp trong MVP. Nhân viên tự nhập mã vận đơn.

### 2.3 Ràng buộc

- **C-01.** Chỉ bán trong Việt Nam. Tiền tệ duy nhất là VND, lưu dạng **số nguyên** (không có phần lẻ).
- **C-02.** Giá niêm yết **đã gồm VAT 10%**.
- **C-03.** Địa chỉ theo đơn vị hành chính mới từ 01/07/2025: **tỉnh/thành → phường/xã** (34 tỉnh/thành, không còn cấp quận/huyện).
- **C-04.** Giao diện tiếng Việt. Tìm kiếm phải hiểu được tiếng Việt không dấu.
- **C-05.** Chỉ dùng môi trường **sandbox** của VNPay. Không có giao dịch tiền thật.
- **C-06.** Một kho duy nhất (Kho HCM).
- **C-07.** Hạ tầng chạy được trên gói miễn phí hoặc chi phí thấp (một máy chủ nhỏ hoặc PaaS).
- **C-08.** Tuân thủ Nghị định 13/2023/NĐ-CP về bảo vệ dữ liệu cá nhân ở mức cơ bản: có chính sách bảo mật công khai (FR-CNT-01); xin đồng ý rõ ràng khi đăng ký; chỉ gửi email quảng cáo cho người đã đồng ý; chỉ thu thập dữ liệu cần cho việc giao hàng và hoá đơn. Yêu cầu xoá tài khoản để giai đoạn 2.
- **C-09.** Mọi mốc **theo lịch** tính theo **giờ Việt Nam** (UTC+7): ngày trong mã đơn, mốc 14:00 của hoả tốc, giờ mở bán, thời hạn khuyến mãi và voucher, doanh thu theo ngày. Các hạn tính bằng khoảng thời gian (15 phút, 48 giờ, 7 ngày) đếm từ một thời điểm cụ thể nên không phụ thuộc múi giờ. Hệ thống lưu mọi thời điểm dạng tuyệt đối (UTC); giờ Việt Nam chỉ dùng khi nhập, hiển thị và gom theo ngày.

### 2.4 Giả định

- **A-01.** Lượng truy cập nhỏ (demo), nhưng hệ thống phải **đúng** dưới tải đồng thời. Đây là chỗ để thể hiện năng lực.
- **A-02.** Frontend viết lại bằng Next.js, tái dùng thiết kế của prototype (CSS, bố cục, nội dung). Mọi tính toán tiền, tồn kho, trạng thái chuyển về backend.
- **A-03.** Nội dung đánh giá sản phẩm (rating, review) là dữ liệu mẫu, chỉ hiển thị.
- **A-04.** Ảnh sản phẩm trong prototype và MVP là **ảnh mock** (Unsplash, giấy phép miễn phí), chỉ để demo. Ảnh riêng của Cadence sẽ thay thế ở giai đoạn 2. Hệ thống không được giả định ảnh nằm ở một host cố định: đổi nguồn ảnh không được đòi sửa code hay dữ liệu đơn hàng cũ.
- **A-05.** Prototype là **bản phác** giao diện và luồng, dùng để tham khảo, không phải bản cuối. Khi prototype khác SRS thì theo SRS. Giao diện thật được thiết kế lại cho đẹp và hợp lý hơn; nội dung hiển thị (câu chữ, cam kết với khách) phải khớp với những gì hệ thống thật sự làm được. Dữ liệu mẫu (sản phẩm, giá) vẫn lấy từ prototype để demo và test.

---

## 3. Phạm vi

### 3.1 Trong phạm vi MVP

| Module | Nội dung |
|---|---|
| Danh mục và tìm kiếm | PLP có lọc, sắp xếp, phân trang; PDP; tìm kiếm không dấu |
| Giỏ hàng | Thêm, sửa, xoá; giữ giỏ cho khách vãng lai và hội viên; cảnh báo hết hàng hoặc đổi giá |
| Checkout | Checkout một trang, voucher, phương thức giao hàng, COD và VNPay, yêu cầu xuất hoá đơn |
| Thanh toán | VNPay: tạo giao dịch, IPN, hết hạn sau 15 phút, thanh toán lại, đối soát |
| Đơn hàng | Tra cứu không cần đăng nhập, huỷ đơn, lịch sử trạng thái |
| Tài khoản | Đăng ký, đăng nhập, sổ địa chỉ, lịch sử đơn |
| Admin: đơn hàng | Danh sách, lọc, xử lý theo vòng đời, ghi chú nội bộ, hoàn tiền |
| Admin: sản phẩm | Tạo, sửa sản phẩm, phối màu, size, giá, khuyến mãi; đăng bán và ngừng bán (mục 6.5). Lên lịch mở bán là mức S (FR-ADM-PRD-04) |
| Admin: tồn kho | Xem on hand / reserved / ATS; nhập kho, điều chỉnh, sổ cái |
| Admin: tổng quan | Doanh thu thuần 7 ngày, danh sách việc cần làm |
| Thông báo | Email đặt hàng, thanh toán, huỷ đơn, giao hàng |
| Nội dung | Trang điều khoản, chính sách bảo mật, giao hàng và thanh toán (FR-CNT) |

Danh mục sản phẩm và voucher **không** có màn hình quản lý trong MVP: chúng được tạo bằng dữ liệu mẫu hoặc migration.

### 3.2 Giai đoạn 2 (làm nếu còn thời gian)

Đổi size và trả hàng sau khi giao, màn hình quản lý danh mục và voucher, sản phẩm dành riêng cho hội viên, danh mục phường/xã chuẩn, yêu cầu xoá tài khoản, danh sách yêu thích lưu ở server, email "báo khi có hàng", quên mật khẩu, xác minh email (kèm gắn đơn vãng lai vào tài khoản), giới hạn lượt dùng voucher mỗi khách, tính phí ship qua API hãng vận chuyển, upload ảnh sản phẩm riêng lên object storage (thay ảnh mock, xem A-04), tìm kiếm bằng search engine riêng (Meilisearch/OpenSearch), xác thực hai lớp (MFA) cho `ADMIN`.

### 3.3 Ngoài phạm vi

Đổi trả hàng sau khi giao trong MVP (ngoài trường hợp giao thất bại); vì vậy giao diện MVP **không** được hứa với khách chính sách đổi trả (A-05). Nhiều kho, nhiều tiền tệ, đăng nhập mạng xã hội, chương trình tích điểm, viết đánh giá sản phẩm, app di động, xuất hoá đơn điện tử thật (chỉ ghi nhận thông tin yêu cầu).

---

## 4. Yêu cầu chức năng

### 4.1 Danh mục và tìm kiếm (`FR-CAT`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-CAT-01 | Khách xem được danh sách sản phẩm **đang bán** (mục 6.5) ở ba nơi: tất cả sản phẩm (`/san-pham`), theo danh mục (`/danh-muc/{slug}`), kết quả tìm kiếm. Mỗi lần tải 12 sản phẩm, có nút "Xem thêm" | M |
| FR-CAT-02 | Lọc nhiều lựa chọn cùng lúc theo: danh mục (chỉ có ở trang tất cả sản phẩm và trang tìm kiếm; trang danh mục đã cố định danh mục theo URL), giới tính, môn thể thao, nhóm màu, size, khoảng giá, đang giảm giá. Lọc theo size **chỉ tính size còn hàng** (ATS > 0). Khoảng giá chia thành các mức cố định, cấu hình được (mặc định: dưới 1 triệu, 1–2 triệu, 2–3 triệu, trên 3 triệu), so theo đơn giá đang bán (BR-PRC-01). "Đang giảm giá" = có khuyến mãi đang hiệu lực (BR-PRC-09). Mỗi lựa chọn hiển thị số sản phẩm khớp (facet count) | M |
| FR-CAT-03 | Sắp xếp theo: **nổi bật** (thứ hạng nổi bật tăng dần, sản phẩm không có thứ hạng xếp sau, cùng hạng thì mới hơn trước), **phù hợp nhất** (chỉ khi đang tìm kiếm, là mặc định lúc đó), **mới nhất** (giờ mở bán giảm dần), giá tăng dần, giá giảm dần. Mặc định khi không tìm kiếm: nổi bật. Mọi cách sắp xếp đều có thứ tự cố định khi bằng nhau, để "Xem thêm" không lặp hay sót sản phẩm | M |
| FR-CAT-04 | Trạng thái lọc, sắp xếp, tìm kiếm nằm trên URL, để gửi link cho người khác vẫn ra đúng kết quả | S |
| FR-CAT-05 | PDP hiển thị: ảnh, tên, giá (giá gốc gạch ngang nếu có khuyến mãi đang hiệu lực), các phối màu, size kèm trạng thái còn hàng, mô tả, thông số, gợi ý chọn size theo **form dáng** của sản phẩm (FR-ADM-PRD-02): form nhỏ → "nên chọn lớn hơn nửa size", form rộng → "nên chọn nhỏ hơn nửa size", đúng size → không gợi ý | M |
| FR-CAT-06 | Size có ATS = 0 hiển thị nhưng **không chọn được**, kèm lựa chọn "Báo cho tôi khi có hàng" (lưu đăng ký; gửi email ở giai đoạn 2) | M / C |
| FR-CAT-07 | Sản phẩm **sắp mở bán** (mục 6.5) không xuất hiện ở PLP, tìm kiếm và sitemap. Vào thẳng PDP thì trang trả HTTP 200, hiện đếm ngược tới giờ mở bán, **không** chọn size hay thêm vào giỏ được, và có `noindex` (NFR-SEO-06). Tới giờ mở bán, sản phẩm tự chuyển sang đang bán mà không cần thao tác nào | S |
| FR-CAT-09 | Nhãn trên thẻ sản phẩm được **suy ra**, không nhập tay: "Mới" khi giờ mở bán nằm trong 30 ngày gần nhất; "−N%" khi có khuyến mãi đang hiệu lực (N làm tròn xuống); "Hết hàng" khi mọi variant có ATS = 0 | S |

**Tìm kiếm (FR-CAT-08, ưu tiên M):**
- Không phân biệt hoa thường và dấu: "giay chay bo" khớp "Giày chạy bộ", "đ" tương đương "d".
- Có bảng từ đồng nghĩa: `sneaker → giày`, `tee → áo thun`, `hoodie → áo nỉ`, `running → chạy bộ`…
- Mỗi từ khoá phải khớp **đầu** một từ trong dữ liệu ("ni" không khớp "organic").
- Tìm trên: tên, danh mục, môn thể thao, giới tính, tên màu.
- Chỉ tìm trong sản phẩm đang bán.
- Kết quả xếp theo độ liên quan: khớp đầu tên > tên chứa cả cụm > hàng mới (nhãn "Mới", FR-CAT-09) > thứ hạng nổi bật.
- Ô tìm kiếm gợi ý ngay khi gõ, tô đậm phần khớp.

**Tiêu chí chấp nhận (ví dụ):**
- *Cho* sản phẩm "Club Hoodie", *khi* tìm "hoodie" hoặc "ao ni", *thì* sản phẩm xuất hiện trong kết quả.
- *Cho* size 42 của Tempo 2 có ATS = 0, *khi* khách mở PDP, *thì* nút size 42 bị khoá và nút "Thêm vào giỏ" không gửi được size này.
- *Cho* sản phẩm có giờ mở bán 20:00 hôm nay, *khi* khách mở PDP lúc 19:59, *thì* trang trả 200 kèm đếm ngược và `noindex`; *khi* mở lúc 20:00, *thì* trang hiện bình thường và sản phẩm có trong PLP.

### 4.2 Giỏ hàng (`FR-CART`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-CART-01 | Thêm vào giỏ bắt buộc chọn size. Thêm trùng variant thì cộng dồn số lượng | M |
| FR-CART-02 | Sửa số lượng (1 đến tối đa `min(ATS, 5)`/dòng, tổng giỏ tối đa 10 sản phẩm, xem BR-ORD-06), đổi size ngay trong giỏ, xoá dòng | M |
| FR-CART-03 | Khách vãng lai có giỏ gắn với mã ẩn danh trong cookie. Khi đăng nhập, giỏ ẩn danh **gộp** vào giỏ của tài khoản: cùng variant thì cộng dồn; vượt giới hạn (5 mỗi dòng, 10 mỗi giỏ) thì cắt về mức tối đa và báo cho khách dòng nào bị cắt | M |
| FR-CART-04 | Mỗi lần mở giỏ, hệ thống kiểm tra lại từng dòng: hết hàng, hoặc sản phẩm không còn đang bán (ngừng bán, hoặc bị dời giờ mở bán về tương lai) → báo lỗi và **chặn thanh toán** cho đến khi khách sửa; giá đã đổi so với lúc thêm (sửa giá, khuyến mãi bắt đầu hoặc kết thúc) → báo "Giá đã thay đổi" kèm giá cũ, giá mới | M |
| FR-CART-05 | Giỏ hiển thị tạm tính, tiết kiệm được, thanh tiến độ tới mốc miễn phí giao hàng | M |
| FR-CART-06 | Nhập voucher ngay trong giỏ, báo lỗi rõ lý do (không tồn tại, chưa đủ giá trị tối thiểu, còn thiếu bao nhiêu tiền) | M |
| FR-CART-07 | Giỏ hàng **không** giữ hàng. Hàng chỉ được giữ khi đặt hàng (xem BR-INV-02). Giỏ phải ghi rõ điều này cho khách | M |
| FR-CART-08 | Dòng có số lượng lớn hơn ATS nhưng ATS > 0: báo "Chỉ còn N", **chặn thanh toán** tới khi khách giảm số lượng. Hệ thống không tự giảm | M |

### 4.3 Checkout (`FR-CHK`)

Checkout là **một trang** gồm 5 phần: liên hệ → địa chỉ → giao hàng → thanh toán → hoá đơn.

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-CHK-01 | Không bắt buộc đăng nhập. Bắt buộc email và số điện thoại (10 số, bắt đầu bằng 0). Hội viên được điền sẵn email và có thể chọn địa chỉ đã lưu | M |
| FR-CHK-02 | Địa chỉ gồm: họ tên, tỉnh/thành (chọn từ danh sách 34), phường/xã (tự nhập, có gợi ý), số nhà và tên đường, ghi chú (không bắt buộc) | M |
| FR-CHK-03 | Phương thức giao hàng: **Tiêu chuẩn** 2–4 ngày, 30.000 ₫; **Nhanh** 1–2 ngày, 45.000 ₫; **Hoả tốc** trong 4 giờ, 60.000 ₫. Hoả tốc chỉ cho TP. HCM và Hà Nội, chỉ với thanh toán **VNPay** (đơn COD phải chờ gọi xác nhận nên không kịp 4 giờ), mọi ngày trong tuần, đặt trước 14:00 giờ Việt Nam tính **lúc bấm đặt hàng** | M |
| FR-CHK-04 | Thanh toán: **COD** hoặc **VNPay** | M |
| FR-CHK-05 | Tuỳ chọn xuất hoá đơn công ty: tên công ty, mã số thuế (10 hoặc 13 số), email nhận hoá đơn | S |
| FR-CHK-06 | Tự lưu nháp form để khách quay lại không phải nhập lại | C |
| FR-CHK-07 | Kiểm tra lỗi từng ô ngay trên trang, thông báo cụ thể, đưa con trỏ tới ô lỗi đầu tiên | M |
| FR-CHK-08 | Khi bấm "Đặt hàng", **server tính lại toàn bộ giá** (không tin số liệu từ trình duyệt), kiểm tra tồn kho, giữ hàng và tạo đơn **trong một giao dịch** | M |
| FR-CHK-09 | Mỗi lần bấm đặt hàng gửi kèm một **idempotency key**. Gửi lại cùng key (bấm hai lần, mạng chập chờn) trả về đúng đơn đã tạo, không tạo đơn thứ hai | M |
| FR-CHK-10 | Nếu một dòng hết hàng ngay lúc đặt: không tạo đơn, không giữ hàng dòng nào, báo rõ sản phẩm và size nào hết, khẳng định "chưa có khoản tiền nào bị trừ" | M |
| FR-CHK-11 | Sau khi đặt: COD → trang kết quả; VNPay → chuyển sang cổng VNPay | M |
| FR-CHK-12 | Trình duyệt gửi kèm **tổng tiền khách đang thấy**. Nếu server tính ra khác (giá vừa đổi, khuyến mãi vừa bắt đầu hoặc kết thúc theo BR-PRC-09, khách vừa đăng nhập hoặc đăng xuất làm đổi phí giao theo BR-PRC-07): **không tạo đơn**, báo khách tổng mới và các dòng thay đổi để khách xác nhận lại | M |
| FR-CHK-13 | Voucher không còn dùng được ngay lúc đặt (hết lượt, hết hạn): **không tạo đơn**, báo lý do; khách bỏ voucher hoặc chọn voucher khác rồi đặt lại. Hệ thống không tự bỏ voucher | M |

**Tiêu chí chấp nhận (ví dụ):**
- *Cho* khách bấm "Đặt hàng" hai lần liên tiếp, *thì* chỉ có một đơn được tạo và cả hai lần đều nhận về cùng mã đơn.
- *Cho* trình duyệt gửi giá 1 ₫ cho một dòng, *thì* đơn vẫn được tính theo giá đang bán trên server.

### 4.4 Thanh toán (`FR-PAY`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-PAY-01 | Đơn VNPay có **15 phút** để thanh toán. Trong thời gian đó hàng được giữ | M |
| FR-PAY-02 | Tạo URL thanh toán VNPay có chữ ký (HMAC-SHA512) theo tài liệu VNPay | M |
| FR-PAY-03 | **Chỉ IPN** được quyền chuyển trạng thái thanh toán sang `PAID`. Return URL chỉ dùng để hiển thị cho khách | M |
| FR-PAY-04 | Xử lý IPN: kiểm tra chữ ký → kiểm tra đơn tồn tại → kiểm tra **số tiền khớp** → kiểm tra trạng thái hiện tại → cập nhật. Trả mã phản hồi đúng chuẩn VNPay cho từng trường hợp | M |
| FR-PAY-05 | IPN gửi lặp (cùng giao dịch) không gây cập nhật lần hai, không gửi email lần hai | M |
| FR-PAY-06 | IPN báo thành công tới **sau** khi đơn đã bị huỷ (trạng thái thanh toán của đơn là `EXPIRED`): ghi nhận giao dịch là đã thu tiền, chuyển trạng thái thanh toán của đơn `EXPIRED → REFUND_PENDING` (mục 6.2), tạo yêu cầu hoàn tiền và đưa vào danh sách việc cần làm của admin. **Không** tự mở lại đơn, **không** giữ lại hàng | M |
| FR-PAY-07 | Thanh toán thất bại (`FAILED`) mà còn trong 15 phút: khách được **thanh toán lại** cho cùng đơn, tạo giao dịch mới. Hạn 15 phút tính từ lúc đặt đơn; thanh toán lại **không** gia hạn | M |
| FR-PAY-08 | Trang kết quả khi đang chờ IPN: hiển thị "Đang chờ xác nhận thanh toán" và tự kiểm tra lại trạng thái định kỳ | M |
| FR-PAY-09 | Tác vụ nền mỗi phút: đơn `PENDING_PAYMENT` quá hạn → hỏi lại VNPay (querydr). Nếu chưa thanh toán thì chuyển `EXPIRED`, huỷ đơn và **trả hàng về ATS** | M |
| FR-PAY-10 | Admin bấm "Đối soát" trên đơn có **thanh toán treo** để hỏi lại VNPay ngay lập tức. Thanh toán treo = giao dịch đã quá hạn nhưng hệ thống chưa biết kết quả vì querydr chưa trả lời được (lỗi mạng, timeout) | S |
| FR-PAY-11 | Hoàn tiền VNPay: gọi API refund của sandbox; nếu không dùng được thì nhân viên đánh dấu "Đã hoàn" thủ công, có ghi lý do. Thao tác thủ công cũng bị giới hạn hạn mức như FR-ADM-ORD-08 | S |
| FR-PAY-12 | Tác vụ đối soát tự động hằng đêm: hỏi lại VNPay mọi **giao dịch thanh toán** còn `PENDING` và mọi **yêu cầu hoàn tiền** còn đang xử lý, tạo trong 48 giờ qua; cập nhật theo kết quả; bản ghi lệch với VNPay thì đưa vào việc cần làm của admin | S |

### 4.5 Đơn hàng phía khách (`FR-ORD`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-ORD-01 | Mã đơn dạng `CAD-YYMMDD-XXXX` (ngày theo giờ Việt Nam, 4 số ngẫu nhiên), duy nhất. Trùng mã thì sinh mã khác, khách không thấy lỗi | M |
| FR-ORD-02 | Tra cứu đơn không cần đăng nhập bằng **mã đơn + (số điện thoại hoặc email)** đã dùng khi đặt. Sai thông tin thì báo chung "Không tìm thấy đơn", không tiết lộ mã đơn có tồn tại hay không | M |
| FR-ORD-03 | Trang chi tiết đơn chỉ mở được bằng một trong ba cách: hội viên đã đăng nhập xem đơn của chính mình; tra cứu thành công theo FR-ORD-02; hoặc link truy cập đơn theo FR-ORD-07. Biết mã đơn thôi **không** đủ để xem đơn (NFR-SEC-03). Trang hiển thị: 3 trạng thái (đơn hàng / thanh toán / giao hàng), dòng thời gian sự kiện, sản phẩm theo **snapshot**, địa chỉ, tổng tiền chi tiết | M |
| FR-ORD-04 | Khách tự huỷ được đơn khi đơn **chưa đóng gói** (`UNFULFILLED`) và chưa `CANCELLED`/`COMPLETED`. Bắt buộc chọn lý do | M |
| FR-ORD-05 | Huỷ đơn đã thanh toán → thanh toán chuyển `REFUND_PENDING` và báo khách "hoàn tiền trong 3–7 ngày làm việc" | M |
| FR-ORD-06 | Hội viên xem được tất cả đơn đặt khi đã đăng nhập. Đơn khách vãng lai cùng email **không** tự gắn vào tài khoản trong MVP; chỉ gắn sau khi có xác minh email (giai đoạn 2, xem D-03) | S |
| FR-ORD-07 | Mỗi đơn có một **link truy cập đơn** chứa mã bí mật ngẫu nhiên (ít nhất 128 bit), dùng ở trang kết quả sau khi đặt và trong mọi email của đơn. Server chỉ lưu bản băm của mã. Link hết hiệu lực sau 90 ngày kể từ lúc đặt; sau đó khách tra cứu theo FR-ORD-02. Link chỉ cho xem đơn và huỷ đơn (FR-ORD-04), không cho xem đơn khác | M |

### 4.6 Tài khoản (`FR-ACC`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-ACC-01 | Đăng ký bằng họ tên, email (không phân biệt hoa thường), mật khẩu (8–64 ký tự), đồng ý điều khoản và chính sách bảo mật (bắt buộc, có link tới FR-CNT-01), nhận tin khuyến mãi (không bắt buộc, mặc định không chọn) | M |
| FR-ACC-02 | Đăng nhập bằng email + mật khẩu. Sai thì báo chung, không nói sai email hay sai mật khẩu | M |
| FR-ACC-03 | Sau đăng nhập, quay về trang đang dở (`?next=`), **chỉ** chấp nhận đường dẫn nội bộ | M |
| FR-ACC-04 | Sổ địa chỉ: thêm, sửa, xoá, đặt địa chỉ mặc định | S |
| FR-ACC-05 | Trang tổng quan hội viên: đơn đang xử lý, tổng chi tiêu (tổng tiền các đơn `PLACED` / `COMPLETED`; đơn bị hoàn tiền luôn là `CANCELLED` nên tự bị loại), quyền lợi hội viên | S |
| FR-ACC-06 | Đăng xuất (M); đổi mật khẩu, phải nhập mật khẩu hiện tại, mật khẩu mới theo quy tắc FR-ACC-01, đổi xong thì mọi phiên khác bị đăng xuất (S) | M / S |
| FR-ACC-07 | Quên mật khẩu qua email | C (giai đoạn 2) |
| FR-ACC-08 | Xác minh email khi đăng ký. MVP **không** yêu cầu xác minh | C (giai đoạn 2) |

### 4.7 Admin: đơn hàng (`FR-ADM-ORD`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-ADM-ORD-01 | Danh sách đơn: tìm theo mã đơn, số điện thoại, email; lọc theo từng trục trạng thái và khoảng ngày; phân trang | M |
| FR-ADM-ORD-02 | Chi tiết đơn: thông tin khách, snapshot sản phẩm kèm **phần giảm giá phân bổ từng dòng**, các trạng thái, lịch sử thao tác (ai, lúc nào, làm gì) | M |
| FR-ADM-ORD-03 | Chỉ hiện những nút hành động hợp lệ với trạng thái hiện tại (theo [mục 6](#6-mô-hình-trạng-thái)) | M |
| FR-ADM-ORD-04 | Xác nhận đơn COD sau khi gọi điện cho khách, có ghi nhận cuộc gọi | M |
| FR-ADM-ORD-05 | Đóng gói → Giao cho vận chuyển (bắt buộc nhập mã vận đơn) → Đã giao. Bước giao cho vận chuyển **trừ tồn on hand** và ghi sổ cái `SHIP` | M |
| FR-ADM-ORD-06 | Giao thất bại: huỷ đơn, hàng hoàn về kho (`RETURN_RESTOCK`), nếu đã thanh toán thì tạo yêu cầu hoàn tiền | M |
| FR-ADM-ORD-07 | Huỷ đơn từ admin: bắt buộc nhập lý do, khách sẽ thấy lý do này. Nhân viên huỷ được cả đơn **đã đóng gói** (hàng chưa rời kho): trục giao hàng quay về `UNFULFILLED`, hàng được trả về ATS | M |
| FR-ADM-ORD-08 | Hoàn tiền: `STAFF` chỉ được hoàn đơn có số tiền **≤ 2.000.000 ₫**; trên mức đó cần `ADMIN` | M |
| FR-ADM-ORD-09 | Ghi chú nội bộ trên đơn (khách không thấy) | S |
| FR-ADM-ORD-10 | Đánh dấu nổi bật đơn có thanh toán treo (định nghĩa ở FR-PAY-10) để nhân viên đối soát | S |

### 4.8 Admin: sản phẩm (`FR-ADM-PRD`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-ADM-PRD-01 | Danh sách sản phẩm: tìm, lọc theo danh mục và cách hiển thị (nháp / sắp mở bán / đang bán / ngừng bán, mục 6.5) | M |
| FR-ADM-PRD-02 | Tạo, sửa nội dung sản phẩm (`STAFF` và `ADMIN`): tên, slug, danh mục, giới tính, môn thể thao, mô tả, thông số, form dáng (nhỏ / đúng size / rộng, dùng cho FR-CAT-05), thứ hạng nổi bật (số nguyên dương, bỏ trống = không nổi bật, dùng cho FR-CAT-03), phối màu (tên màu, nhóm màu), ảnh theo từng phối màu, danh sách size. Ảnh nhập bằng **khoá ảnh** kèm chiều rộng, chiều cao, mô tả ảnh (BR-CAT-01); upload ở giai đoạn 2. Mỗi variant có SKU và mã vạch GTIN (không bắt buộc) | M |
| FR-ADM-PRD-03 | Giá gốc, đặt ở **mức sản phẩm**: mọi size, mọi phối màu cùng giá (BR-PRC-08). **Chỉ `ADMIN`** được đặt và sửa giá | M |
| FR-ADM-PRD-04 | Lên lịch mở bán theo giờ Việt Nam (giờ mở bán ở tương lai khi đăng bán, hoặc dời giờ mở bán của sản phẩm sắp mở bán). **Chỉ `ADMIN`** | S |
| FR-ADM-PRD-05 | Ghi đè tiêu đề và mô tả SEO (NFR-SEO-03), xem trước kết quả hiển thị trên Google | C |
| FR-ADM-PRD-06 | Không xoá cứng sản phẩm đã từng có đơn, chỉ chuyển sang "Ngừng bán". Sản phẩm nháp (chưa từng đăng bán nên chưa thể có đơn) thì xoá được | M |
| FR-ADM-PRD-07 | Sửa giá không ảnh hưởng các đơn đã đặt (nhờ snapshot); giỏ hàng chứa sản phẩm đó sẽ báo "Giá đã thay đổi" (FR-CART-04) | M |
| FR-ADM-PRD-08 | Khuyến mãi (BR-PRC-09): giá khuyến mãi, thời điểm bắt đầu, thời điểm kết thúc (không bắt buộc). **Chỉ `ADMIN`**. Sửa hoặc kết thúc sớm khuyến mãi không ảnh hưởng đơn đã đặt | M |
| FR-ADM-PRD-09 | Đăng bán, ngừng bán, mở bán lại theo vòng đời ở mục 6.5. **Chỉ `ADMIN`**. Đăng bán bị từ chối, kèm danh sách lý do, nếu sản phẩm thiếu giá gốc, chưa có phối màu nào, hoặc có phối màu không có ảnh hay không có size | M |

### 4.9 Admin: tồn kho (`FR-INV`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-INV-01 | Bảng tồn theo SKU: on hand, đang giữ, tồn an toàn, ATS, vị trí kho; lọc theo sắp hết / hết hàng | M |
| FR-INV-02 | **Nhập kho** (`RECEIVE`): số lượng dương, bắt buộc ghi chứng từ (ví dụ `PO-2611-014`) | M |
| FR-INV-03 | **Điều chỉnh kiểm kê** (`ADJUST`): số lượng âm hoặc dương, bắt buộc ghi lý do. Không cho on hand xuống dưới số đang giữ | M |
| FR-INV-04 | Xem sổ cái biến động theo từng SKU: loại, số lượng, lý do, người thực hiện, thời điểm | M |
| FR-INV-05 | Sửa tồn an toàn từng SKU | S |

### 4.10 Admin: tổng quan và phân quyền (`FR-ADM`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-ADM-01 | Doanh thu thuần theo ngày trong 7 ngày, so sánh với 7 ngày trước. Doanh thu thuần = tổng tiền hàng sau giảm giá, **không** gồm phí giao hàng, chỉ tính đơn `PLACED` hoặc `COMPLETED` (đơn bị huỷ, kể cả đơn đã hoàn tiền, không được tính), gom theo **ngày đặt** (giờ Việt Nam) | S |
| FR-ADM-02 | Danh sách việc cần làm: đơn COD chờ xác nhận, đơn chờ đóng gói, thanh toán treo, yêu cầu hoàn tiền, SKU sắp hết. "Sắp hết" = 0 < ATS ≤ ngưỡng, ngưỡng cấu hình được (mặc định 2) | M |
| FR-ADM-03 | Đăng nhập admin tách biệt với tài khoản khách. Phân quyền theo vai trò `STAFF` / `ADMIN`, kiểm tra ở **server** cho mọi API | M |
| FR-ADM-04 | `ADMIN` tạo và khoá tài khoản nhân viên. Mật khẩu ban đầu theo quy tắc FR-ACC-01 và bắt buộc đổi ở lần đăng nhập đầu. Khoá có hiệu lực **ngay**: mọi phiên đang mở của người đó bị huỷ | S |
| FR-ADM-05 | Tài khoản `ADMIN` đầu tiên được tạo tự động từ biến môi trường ở lần khởi động đầu, **chỉ khi** chưa có `ADMIN` nào. Mật khẩu ban đầu bắt buộc đổi ở lần đăng nhập đầu | M |
| FR-ADM-06 | Nhân viên tự đổi mật khẩu của mình: nhập mật khẩu hiện tại, mật khẩu mới theo quy tắc FR-ACC-01; đổi xong thì mọi phiên khác bị huỷ. Tài khoản dùng thử công khai **không** được đổi mật khẩu (NFR-DEMO-01) | M |

### 4.11 Thông báo (`FR-NTF`)

| Mã | Sự kiện → Email gửi khách | Ưu tiên |
|---|---|---|
| FR-NTF-01 | Đặt đơn COD thành công (gửi ngay khi đặt, nói rõ nhân viên sẽ gọi xác nhận) / thanh toán VNPay thành công (gửi khi IPN chuyển sang `PAID`; đơn VNPay không gửi email lúc vừa đặt) | M |
| FR-NTF-02 | Đơn bị huỷ (kèm lý do; kèm thông tin hoàn tiền nếu có) | M |
| FR-NTF-03 | Đơn đã giao cho vận chuyển (kèm mã vận đơn) | S |
| FR-NTF-04 | Hoàn tiền xong | S |

Email được gửi **bất đồng bộ** (qua hàng đợi hoặc bảng outbox). Gửi email lỗi không được làm hỏng giao dịch đặt hàng. Mọi email của đơn có link truy cập đơn (FR-ORD-07). Nội dung email đã dựng được lưu lại để trang "Xem email (bản demo)" hiển thị (NFR-DEMO-03).

### 4.12 Nội dung (`FR-CNT`)

| Mã | Yêu cầu | Ưu tiên |
|---|---|---|
| FR-CNT-01 | Trang nội dung tĩnh: Điều khoản sử dụng, Chính sách bảo mật (C-08), Giao hàng và thanh toán. Có link ở chân trang và ở form đăng ký, checkout | M |
| FR-CNT-02 | Mọi cam kết hiển thị với khách (phí giao, thời gian giao, hoàn tiền) phải lấy từ cùng cấu hình với logic tính toán, không viết cứng một nơi riêng | S |

---

## 5. Quy tắc nghiệp vụ

### 5.1 Tính tiền (`BR-PRC`)

| Mã | Quy tắc |
|---|---|
| BR-PRC-01 | Đơn giá một dòng = giá khuyến mãi nếu khuyến mãi **đang hiệu lực tại thời điểm tính** (BR-PRC-09), ngược lại là giá gốc |
| BR-PRC-02 | **Thứ tự tính:** tạm tính các dòng → giảm giá cấp đơn (voucher) → cộng phí giao hàng → trừ giảm phí giao hàng |
| BR-PRC-03 | Giảm giá cấp đơn được **phân bổ xuống từng dòng** theo tỷ lệ thành tiền, làm tròn bằng phương pháp *largest remainder* để tổng phân bổ khớp đúng tới từng đồng. MVP chỉ hoàn tiền toàn bộ đơn; phần phân bổ được lưu để hiển thị trên chi tiết đơn và dùng cho đổi trả một phần ở giai đoạn sau |
| BR-PRC-04 | Mọi phép tính dùng số nguyên VND. Không dùng số thực dấu phẩy động cho tiền |
| BR-PRC-05 | Tiền VAT hiển thị = tổng − tổng / 1,1, làm tròn tới đồng (giá đã gồm VAT) |
| BR-PRC-06 | Miễn phí giao **Tiêu chuẩn** khi tạm tính ≥ 1.500.000 ₫ (tính trước voucher) |
| BR-PRC-07 | Hội viên đã đăng nhập được miễn phí giao **Tiêu chuẩn** cho mọi đơn, không cần đạt mốc 1.500.000 ₫. Phương thức Nhanh và Hoả tốc vẫn tính phí bình thường |
| BR-PRC-08 | Mọi variant của một sản phẩm có cùng giá gốc và cùng khuyến mãi (FR-ADM-PRD-03, FR-ADM-PRD-08) |
| BR-PRC-09 | Mỗi sản phẩm có tối đa **một** khuyến mãi: giá khuyến mãi (0 < giá khuyến mãi < giá gốc), thời điểm bắt đầu, thời điểm kết thúc (bỏ trống = không thời hạn). Khuyến mãi hiệu lực trong khoảng **[bắt đầu, kết thúc)**: tính cả lúc bắt đầu, không tính lúc kết thúc. Mọi nơi (PLP, PDP, giỏ, checkout, lọc giá, nhãn) dùng cùng một thời điểm "bây giờ" cho một yêu cầu |

### 5.2 Voucher (`BR-VCH`)

| Mã | Quy tắc |
|---|---|
| BR-VCH-01 | Ba loại: **phần trăm** (trên tạm tính, có mức giảm tối đa), **số tiền cố định**, **miễn phí giao hàng**. Loại miễn phí giao hàng giảm **tối đa bằng phí Tiêu chuẩn**: chọn Nhanh hoặc Hoả tốc thì khách trả phần chênh. Nếu phí giao của phương thức đã chọn đã bằng 0 (BR-PRC-06, BR-PRC-07), voucher miễn phí giao hàng **không áp dụng được** và báo "Đơn đã được miễn phí giao hàng"; không tốn lượt dùng. Mỗi voucher có thể có giá trị đơn tối thiểu, so với tạm tính trước giảm giá |
| BR-VCH-02 | Mỗi đơn dùng tối đa **một** voucher |
| BR-VCH-03 | Giảm cố định không vượt quá tạm tính; giảm phần trăm làm tròn xuống |
| BR-VCH-04 | Voucher mẫu: `CADENCE10` giảm 10%, tối đa 200.000 ₫, đơn từ 1.000.000 ₫; `RUN150` giảm 150.000 ₫, đơn từ 2.000.000 ₫; `FREESHIP` miễn phí giao hàng, tối đa 30.000 ₫ |
| BR-VCH-05 | Mỗi voucher có thời hạn (bắt đầu, kết thúc) và giới hạn **tổng** lượt dùng. Đếm lượt dùng phải chính xác khi nhiều người dùng cùng lúc. Giới hạn lượt dùng **mỗi khách** để giai đoạn 2 |
| BR-VCH-06 | Lượt dùng voucher được trả lại **mỗi khi đơn bị huỷ**, với mọi lý do: khách huỷ, nhân viên huỷ, hết hạn thanh toán, COD quá 48 giờ, giao thất bại. Đơn đã giao thành công thì không trả lượt. Trả lượt đúng một lần cho mỗi đơn |

### 5.3 Tồn kho (`BR-INV`)

| Mã | Quy tắc |
|---|---|
| BR-INV-01 | **ATS = max(0, On hand − Reserved − Safety stock)**. Giá trị bên trong có thể âm khi nhân viên tăng tồn an toàn hoặc điều chỉnh giảm, nhưng ATS hiển thị và dùng để bán không bao giờ âm. Storefront chỉ hiển thị và cho bán theo ATS |
| BR-INV-02 | Hàng được giữ (**reserve**) ngay khi tạo đơn, cho cả COD và VNPay |
| BR-INV-03 | Giải phóng phần giữ khi: đơn bị huỷ, thanh toán hết hạn, hoặc hàng đã xuất kho (lúc đó on hand giảm tương ứng) |
| BR-INV-04 | Hai bất biến cho mọi SKU, không có trình tự thao tác đồng thời nào được làm vi phạm: **(a)** luôn luôn `Reserved ≤ On hand`, kể cả khi điều chỉnh kiểm kê (FR-INV-03); **(b)** một lần giữ hàng chỉ thành công khi **sau khi giữ** vẫn còn `Reserved ≤ On hand − Safety stock`. (b) chỉ kiểm lúc giữ hàng: tăng tồn an toàn hoặc điều chỉnh giảm có thể làm `Reserved > On hand − Safety stock` trong một thời gian; khi đó ATS = 0 (BR-INV-01) và không giữ thêm được, còn các đơn đã giữ không bị ảnh hưởng |
| BR-INV-05 | Mọi thay đổi on hand phải có một dòng sổ cái tương ứng; tổng sổ cái của một SKU luôn bằng on hand hiện tại |
| BR-INV-06 | Loại biến động: `RECEIVE` (nhập), `SHIP` (xuất cho đơn), `ADJUST` (kiểm kê), `RETURN_RESTOCK` (hàng hoàn nhập lại) |

### 5.4 Đơn hàng (`BR-ORD`)

| Mã | Quy tắc |
|---|---|
| BR-ORD-01 | Đơn lưu **snapshot** của từng dòng: tên, phối màu, size, SKU, ảnh, đơn giá, phần giảm phân bổ |
| BR-ORD-02 | Đơn COD phải được nhân viên xác nhận qua điện thoại/Zalo trước khi đóng gói |
| BR-ORD-03 | Mọi thay đổi trạng thái ghi một sự kiện vào lịch sử đơn: thời điểm, người thực hiện (khách / nhân viên / hệ thống), nội dung |
| BR-ORD-04 | Đơn chuyển `COMPLETED` sau **7 ngày** kể từ khi giao thành công, do tác vụ nền thực hiện |
| BR-ORD-05 | Đơn COD giữ hàng ngay khi đặt (BR-INV-02). Nếu sau **48 giờ** nhân viên vẫn chưa xác nhận được với khách, hệ thống tự huỷ đơn và trả hàng về ATS |
| BR-ORD-06 | Tối đa **5** sản phẩm mỗi dòng và **10** sản phẩm mỗi đơn. Server kiểm tra lại khi đặt hàng |
| BR-ORD-07 | Số tiền hoàn = **số tiền đã thu** của giao dịch (gồm cả phí giao hàng). MVP không hoàn một phần |

### 5.5 Danh mục (`BR-CAT`)

| Mã | Quy tắc |
|---|---|
| BR-CAT-01 | Ảnh sản phẩm được lưu bằng **khoá ảnh** kèm chiều rộng, chiều cao (để trình duyệt giữ chỗ, tránh nhảy bố cục) và mô tả ảnh (cho trình đọc màn hình, NFR-UX-02). URL đầy đủ = gốc của nguồn ảnh đang cấu hình + khoá ảnh, ghép lúc trả dữ liệu ra. Snapshot của đơn (BR-ORD-01) cũng lưu khoá ảnh. Vì vậy đổi nguồn ảnh (A-04) chỉ đổi cấu hình |
| BR-CAT-02 | Slug sản phẩm và slug danh mục là duy nhất, chỉ gồm chữ thường không dấu, số và dấu gạch ngang. Slug của sản phẩm đã đăng bán **không** được sửa (NFR-SEO-02) |

---

## 6. Mô hình trạng thái

Một đơn có **ba trục trạng thái độc lập**. Tách như vậy để tránh một danh sách trạng thái gộp kiểu "đã thanh toán nhưng chưa giao nhưng đang hoàn tiền".

### 6.1 Trục đơn hàng (`order.status`)

| Từ | Sang | Khi nào | Ai |
|---|---|---|---|
| — | `PENDING_CONFIRMATION` | Đặt đơn COD | Khách |
| — | `PENDING_PAYMENT` | Đặt đơn VNPay | Khách |
| `PENDING_CONFIRMATION` | `PLACED` | Nhân viên gọi xác nhận xong | Nhân viên |
| `PENDING_PAYMENT` | `PLACED` | IPN báo thanh toán thành công | Hệ thống |
| `PENDING_PAYMENT` | `CANCELLED` | Hết 15 phút chưa thanh toán | Hệ thống |
| `PENDING_CONFIRMATION` | `CANCELLED` | Quá 48 giờ chưa xác nhận được đơn COD (BR-ORD-05) | Hệ thống |
| `PENDING_*`, `PLACED` | `CANCELLED` | Khách huỷ khi chưa đóng gói (FR-ORD-04) | Khách |
| `PENDING_*`, `PLACED` | `CANCELLED` | Nhân viên huỷ khi hàng chưa rời kho (FR-ADM-ORD-07), hoặc giao thất bại | Nhân viên |
| `PLACED` | `COMPLETED` | Giao thành công đủ 7 ngày | Hệ thống |

### 6.2 Trục thanh toán (`payment.status`)

| Từ | Sang | Khi nào |
|---|---|---|
| — | `UNPAID` | Đơn COD |
| — | `PENDING` | Đơn VNPay, đang chờ thanh toán |
| `PENDING` | `PAID` | IPN thành công, số tiền khớp |
| `PENDING` | `FAILED` | IPN báo thất bại |
| `FAILED` | `PENDING` | Khách bấm thanh toán lại khi còn trong 15 phút (FR-PAY-07); tạo giao dịch mới |
| `PENDING`, `FAILED` | `EXPIRED` | Hết 15 phút, **hoặc** đơn bị huỷ khi chưa thanh toán |
| `EXPIRED` | `REFUND_PENDING` | IPN báo thành công tới sau khi đơn đã bị huỷ (FR-PAY-06) |
| `UNPAID` | `PAID` | Giao COD thành công, shipper đã thu tiền |
| `PAID` | `REFUND_PENDING` | Huỷ đơn đã thanh toán (FR-ORD-05, FR-ADM-ORD-06, FR-ADM-ORD-07) |
| `REFUND_PENDING` | `REFUNDED` | Hoàn tiền xong (FR-PAY-11) |

Đơn COD chưa thu tiền mà bị huỷ thì trục thanh toán giữ nguyên `UNPAID`.

### 6.3 Trục giao hàng (`fulfillment.status`)

`UNFULFILLED` → `PACKED` → `SHIPPED` → `DELIVERED`, hoặc `SHIPPED` → `FAILED_DELIVERY` (hàng hoàn về kho). Ngoài ra `PACKED` → `UNFULFILLED` khi nhân viên huỷ đơn đã đóng gói (bỏ kiện, hàng về ATS).

### 6.4 Ràng buộc giữa các trục

- Chỉ đóng gói (`PACKED`) khi đơn ở `PLACED`.
- `CANCELLED` chỉ hợp lệ khi giao hàng ở `UNFULFILLED` hoặc `FAILED_DELIVERY`. Khi nhân viên huỷ đơn đã đóng gói, trục giao hàng quay về `UNFULFILLED` **trong cùng thao tác**.
- Hoả tốc (`SAME_DAY`) chỉ đi với thanh toán VNPay (FR-CHK-03).
- Mọi chuyển trạng thái **không có trong bảng** đều bị server từ chối (HTTP 409) và không thay đổi dữ liệu.

### 6.5 Vòng đời sản phẩm (`product.status`)

| Từ | Sang | Khi nào | Ai |
|---|---|---|---|
| — | `DRAFT` | Tạo sản phẩm | `STAFF`, `ADMIN` |
| `DRAFT` | `PUBLISHED` | Đăng bán, qua kiểm tra ở FR-ADM-PRD-09. Giờ mở bán bỏ trống = mở bán ngay | `ADMIN` |
| `PUBLISHED` | `ARCHIVED` | Ngừng bán | `ADMIN` |
| `ARCHIVED` | `PUBLISHED` | Mở bán lại | `ADMIN` |
| `DRAFT` | (xoá) | Xoá sản phẩm nháp (FR-ADM-PRD-06) | `ADMIN` |

Một sản phẩm `PUBLISHED` đã đăng bán thì không quay về `DRAFT`.

**Cách hiển thị** được suy ra từ trạng thái và giờ mở bán, tại thời điểm "bây giờ" của yêu cầu, không lưu thành trạng thái riêng:

| Cách hiển thị | Điều kiện | PLP, tìm kiếm, sitemap | PDP | Mua được |
|---|---|---|---|---|
| **Đang bán** | `PUBLISHED`, giờ mở bán trống hoặc ≤ bây giờ | Có | 200, được index | Có (theo ATS) |
| **Sắp mở bán** | `PUBLISHED`, giờ mở bán > bây giờ | Không | 200, đếm ngược, `noindex` (FR-CAT-07) | Không |
| **Nháp**, **Ngừng bán** | `DRAFT`, `ARCHIVED` | Không | 404 (NFR-SEO-06) | Không |

---

## 7. Yêu cầu phi chức năng

### 7.1 Hiệu năng

| Mã | Yêu cầu | Cách kiểm |
|---|---|---|
| NFR-01 | API danh sách sản phẩm có lọc: p95 < 300 ms với 50 người dùng đồng thời, dữ liệu 1.000 sản phẩm / 10.000 SKU | Test tải (k6), ghi số liệu vào README |
| NFR-02 | API đặt hàng: p95 < 800 ms với 50 người dùng đồng thời | Test tải |
| NFR-03 | Trang storefront: LCP < 2,5 s trên mạng 4G mô phỏng | Lighthouse |

### 7.2 Tính đúng đắn dưới tải đồng thời

| Mã | Yêu cầu | Cách kiểm |
|---|---|---|
| NFR-04 | SKU còn ATS = 5, gửi **200 yêu cầu đặt hàng đồng thời** cho SKU đó: đúng 5 đơn thành công, 195 yêu cầu nhận lỗi hết hàng, ATS cuối = 0, BR-INV-04 và BR-INV-05 vẫn đúng | Test tích hợp với DB thật, chạy trong CI |
| NFR-05 | Gửi cùng một IPN 10 lần đồng thời: trạng thái chuyển đúng một lần, đúng một email, đúng một sự kiện trong lịch sử | Test tích hợp |
| NFR-06 | Gửi cùng idempotency key 10 lần đồng thời: đúng một đơn được tạo | Test tích hợp |
| NFR-07 | Voucher giới hạn N lượt, N + 50 người dùng cùng lúc: đúng N đơn được áp dụng | Test tích hợp |

### 7.3 Bảo mật

| Mã | Yêu cầu |
|---|---|
| NFR-SEC-01 | Mật khẩu băm bằng bcrypt hoặc Argon2. Không ghi mật khẩu, token hay dữ liệu thẻ ra log |
| NFR-SEC-02 | Giới hạn số lần thử: đăng nhập 5 lần / 15 phút / tài khoản + IP; tra cứu đơn 10 lần / 15 phút / IP; áp voucher 10 lần / 15 phút / giỏ. Vượt giới hạn trả HTTP 429 kèm thời gian được thử lại |
| NFR-SEC-03 | Kiểm tra quyền ở server cho mọi API admin. Khách chỉ xem được đơn của mình (chống IDOR): quyền xem đơn đến từ phiên hội viên, tra cứu thành công hoặc link truy cập đơn (FR-ORD-03), không bao giờ từ mã đơn hay id trên URL |
| NFR-SEC-04 | Kiểm tra chữ ký mọi dữ liệu từ VNPay. Khoá bí mật lấy từ biến môi trường, không commit vào repo |
| NFR-SEC-05 | Chống các lỗi OWASP Top 10 cơ bản: SQL injection (truy vấn có tham số), XSS (escape đầu ra), CSRF (nếu dùng cookie phiên), open redirect (FR-ACC-03) |
| NFR-SEC-06 | Chạy HTTPS trên môi trường demo |
| NFR-SEC-07 | Ghi nhật ký đăng nhập của nhân viên (thành công, thất bại, IP, thời điểm). Mọi thao tác thay đổi dữ liệu của nhân viên ghi được người thực hiện |
| NFR-SEC-08 | Phiên: đổi session id khi đăng nhập; phiên khách hết sau 30 ngày không hoạt động; phiên nhân viên hết sau 30 phút không thao tác, tối đa 12 giờ. Khách hàng và nhân viên là hai loại tài khoản tách biệt, không dùng chung phiên |

### 7.4 Độ tin cậy và vận hành

| Mã | Yêu cầu |
|---|---|
| NFR-REL-01 | Tác vụ nền (hết hạn thanh toán, đối soát, huỷ đơn COD quá 48 giờ, chuyển đơn `COMPLETED`, gửi email) chạy lại an toàn; server khởi động lại không làm mất việc |
| NFR-REL-02 | Log có cấu trúc (JSON), mỗi request có `requestId`. Log nghiệp vụ quan trọng: đặt đơn, IPN, chuyển trạng thái, biến động kho |
| NFR-REL-03 | Có endpoint kiểm tra sức khoẻ (health check) |
| NFR-REL-04 | Migration DB có phiên bản, chạy tự động khi deploy |

### 7.5 Khả năng bảo trì

| Mã | Yêu cầu |
|---|---|
| NFR-MNT-01 | Test tự động: unit test cho quy tắc tính tiền và máy trạng thái; integration test với DB thật (Testcontainers) cho các luồng ở mục 7.2 |
| NFR-MNT-02 | CI chạy lint + test cho mỗi pull request; không merge khi test đỏ |
| NFR-MNT-03 | Chạy toàn bộ hệ thống ở máy local bằng một lệnh (`docker compose up`), có dữ liệu mẫu |
| NFR-MNT-04 | Tài liệu API sinh tự động (OpenAPI) |

### 7.6 Khả năng sử dụng và truy cập

| Mã | Yêu cầu |
|---|---|
| NFR-UX-01 | Giao diện dùng tốt từ màn hình 360 px trở lên |
| NFR-UX-02 | Đạt WCAG 2.1 mức AA cho luồng mua hàng: dùng được bằng bàn phím, có nhãn cho trình đọc màn hình, đủ tương phản màu |
| NFR-UX-03 | Mọi thông báo lỗi nói rõ chuyện gì xảy ra và khách cần làm gì tiếp |

### 7.7 SEO

| Mã | Yêu cầu |
|---|---|
| NFR-SEO-01 | Trang chủ, danh mục và chi tiết sản phẩm trả về HTML **đã có nội dung** (tên, giá, mô tả) ngay trong response đầu tiên, không phụ thuộc JavaScript |
| NFR-SEO-02 | URL thân thiện, ổn định: `/san-pham/{slug}`, `/danh-muc/{slug}` (BR-CAT-02); mỗi trang có canonical URL. Trang danh sách có tham số lọc, sắp xếp có canonical trỏ về trang **không** có tham số |
| NFR-SEO-03 | Mỗi trang công khai có tiêu đề và mô tả riêng, có Open Graph. Mặc định sinh tự động: sản phẩm → "{tên} – {danh mục} \| Cadence" và 155 ký tự đầu của mô tả; danh mục → tên danh mục. FR-ADM-PRD-05 (mức C) chỉ cho ghi đè, nên yêu cầu này không phụ thuộc FR-ADM-PRD-05 |
| NFR-SEO-04 | `sitemap.xml` tự sinh, chỉ gồm trang chủ, các danh mục và các sản phẩm **đang bán** (mục 6.5); `robots.txt` chặn giỏ hàng, checkout, tài khoản, trang đơn hàng, admin |
| NFR-SEO-05 | Trang sản phẩm có dữ liệu có cấu trúc `Product` (JSON-LD): tên, ảnh, giá VND, tình trạng còn hàng |
| NFR-SEO-06 | Sản phẩm không tồn tại, nháp hoặc ngừng bán trả HTTP 404 thật. Sản phẩm sắp mở bán trả 200 kèm `noindex` (FR-CAT-07). Trang kết quả tìm kiếm và trang riêng của người dùng có `noindex` |

### 7.8 Môi trường demo

Bản demo công khai (G1) ai cũng truy cập được, nên cần thêm các giới hạn:

| Mã | Yêu cầu |
|---|---|
| NFR-DEMO-01 | Tài khoản dùng thử công khai có vai trò `STAFF`, **không** phải `ADMIN`. Tài khoản `ADMIN` chỉ chủ dự án biết. Tài khoản dùng thử được đánh dấu là tài khoản demo: không đổi được mật khẩu (FR-ADM-06), để người dùng trước không khoá được người dùng sau |
| NFR-DEMO-02 | Dữ liệu demo tự dựng lại từ dữ liệu mẫu mỗi đêm |
| NFR-DEMO-03 | Demo **không gửi email ra ngoài** (tránh bị dùng để gửi thư tới địa chỉ người khác): SMTP trỏ tới Mailpit nội bộ (mục 2.2). Thay vào đó, trang kết quả và trang chi tiết đơn có link "Xem email (bản demo)", đọc nội dung email đã lưu (mục 4.11) và chỉ hiện email của **chính đơn đó**, với cùng quyền xem đơn như FR-ORD-03 |
| NFR-DEMO-04 | Thanh toán dùng VNPay sandbox; trang demo ghi rõ đây là môi trường thử, kèm thông tin thẻ thử của sandbox |

---

## 8. Dữ liệu chính

Danh sách thực thể ở mức khái niệm. Chi tiết bảng, khoá, index nằm ở `03-database.md`.

| Thực thể | Thuộc tính chính |
|---|---|
| Category | slug, tên, thứ tự |
| Product | slug, tên, danh mục, giới tính, môn thể thao, mô tả, thông số, form dáng, thứ hạng nổi bật, tiêu đề và mô tả SEO (không bắt buộc), giá gốc, khuyến mãi (giá, bắt đầu, kết thúc; BR-PRC-09), trạng thái (mục 6.5), giờ mở bán, thời điểm đăng bán |
| Colorway | sản phẩm, tên màu, nhóm màu, thứ tự |
| ProductImage | phối màu, khoá ảnh, chiều rộng, chiều cao, mô tả ảnh, thứ tự (BR-CAT-01) |
| Variant (SKU) | phối màu, size, mã SKU, GTIN |
| InventoryItem | SKU, on hand, reserved, safety stock, vị trí kho, version |
| InventoryMovement | SKU, loại, số lượng, lý do, người thực hiện, thời điểm, đơn liên quan |
| Customer | email, họ tên, mật khẩu băm, đồng ý nhận tin |
| Address | khách hàng, người nhận, số điện thoại, tỉnh, phường, đường, mặc định |
| Cart / CartItem | chủ sở hữu (khách hoặc mã ẩn danh), SKU, số lượng, giá lúc thêm |
| Voucher / VoucherRedemption | mã, loại, giá trị, mức tối đa, đơn tối thiểu, thời hạn, giới hạn lượt / lượt đã dùng |
| Order / OrderLine | mã đơn, liên hệ, địa chỉ, ba trục trạng thái, tổng tiền chi tiết, idempotency key, bản băm mã truy cập đơn và hạn (FR-ORD-07); snapshot từng dòng, gồm khoá ảnh |
| OrderEvent | đơn, thời điểm, người thực hiện, nội dung |
| PaymentTransaction | đơn, số tiền, trạng thái, `txnRef`, hạn; mỗi lần thử thanh toán với VNPay là một bản ghi, kèm dữ liệu thô của IPN. (Phương thức và trạng thái thanh toán **của đơn** là một trục của Order, xem mục 6) |
| Refund | giao dịch gốc, số tiền, trạng thái, người thực hiện, lý do |
| StaffUser | email, họ tên, vai trò, trạng thái, bắt buộc đổi mật khẩu, là tài khoản demo |
| StaffLoginLog | nhân viên (hoặc email đã nhập), kết quả, IP, thời điểm |
| OutboxMessage | loại email, đơn liên quan, người nhận, tiêu đề và nội dung đã dựng, trạng thái gửi, số lần thử |

---

## 9. Nguồn tham khảo trong prototype

Bảng này cho biết yêu cầu nào được gợi ý từ trang nào của prototype, để tiện tra lại. Prototype không ràng buộc thiết kế cuối (A-05).

| Trang prototype | Yêu cầu |
|---|---|
| `index.html`, `plp.html`, `pdp.html` | FR-CAT |
| `cart.html` | FR-CART, BR-VCH |
| `checkout.html` | FR-CHK, BR-PRC |
| `gateway.html` (mô phỏng) | FR-PAY (thay bằng VNPay sandbox thật) |
| `order-result.html`, `order-lookup.html` | FR-PAY-08, FR-ORD |
| `account.html` | FR-ACC, FR-ORD-06 |
| `admin/index.html` | FR-ADM-01, FR-ADM-02 |
| `admin/orders.html`, `admin/order.html` | FR-ADM-ORD, mục 6 |
| `admin/products.html`, `admin/product.html` | FR-ADM-PRD |
| `admin/inventory.html` | FR-INV, BR-INV |

---

## 10. Quyết định đã chốt

Các câu hỏi mở của bản nháp đã được chốt ở v1.0. Bảng này giữ lại để biết vì sao tài liệu viết như vậy.

| Mã | Câu hỏi | Quyết định | Áp dụng tại |
|---|---|---|---|
| D-01 | Prototype hứa hội viên được miễn phí giao hàng nhưng phần tính tiền chưa làm. Có giữ không? | Giữ, chỉ áp cho phương thức Tiêu chuẩn | BR-PRC-07 |
| D-02 | Khi nào đơn chuyển `COMPLETED`? | 7 ngày sau khi giao thành công, do tác vụ nền | BR-ORD-04, mục 6.1 |
| D-03 | Đơn khách vãng lai có tự gắn vào tài khoản cùng email không? | Chỉ gắn sau khi xác minh email. MVP chưa có xác minh nên **không** gắn | FR-ORD-06 |
| D-04 | Voucher có thời hạn và giới hạn lượt dùng không? | Có thời hạn và giới hạn tổng lượt; giới hạn mỗi khách để giai đoạn 2 | BR-VCH-05, BR-VCH-06 |
| D-05 | Doanh thu thuần tính thế nào? | Không gồm phí giao hàng; không tính đơn huỷ (đơn đã hoàn tiền luôn là đơn huỷ, xem D-58) | FR-ADM-01 |
| D-06 | Đơn COD giữ hàng khi nào, giữ bao lâu? | Giữ ngay khi đặt; tự huỷ sau 48 giờ nếu chưa xác nhận | BR-ORD-05, mục 6.1 |
| D-07 | Giới hạn số lượng mỗi dòng / mỗi đơn? | 5 mỗi dòng, 10 mỗi đơn | BR-ORD-06, FR-CART-02 |
| D-08 | MVP có xác minh email khi đăng ký không? | Không; đưa vào giai đoạn 2 cùng quên mật khẩu | FR-ACC-08, mục 3.2 |

D-09 trở đi được chốt ở v1.4 sau một lượt review độ rõ ràng.

| Mã | Câu hỏi | Quyết định | Áp dụng tại |
|---|---|---|---|
| D-09 | Prototype có ràng buộc thiết kế cuối không? | Không; chỉ là bản tham khảo | A-05, mục 9 |
| D-10 | Có hứa đổi size / trả hàng trong 30 ngày như prototype không? | Không trong MVP, vì hệ thống chưa hỗ trợ. Đổi trả để giai đoạn 2 | Mục 3.2, 3.3 |
| D-11 | Danh sách yêu thích trong MVP? | Lưu trong trình duyệt: đủ dùng, không cần API. Đổi máy thì mất; lưu theo tài khoản ở giai đoạn 2 | Mục 2.1 |
| D-12 | Hai size của cùng sản phẩm có được khác giá không? | Không; giá đặt ở mức sản phẩm | FR-ADM-PRD-03, BR-PRC-08 |
| D-13 | Phân bổ giảm giá dùng để làm gì khi MVP không hoàn một phần? | Hiển thị trên chi tiết đơn; dành cho đổi trả một phần sau này | BR-PRC-03 |
| D-14 | Tài khoản dùng thử trên demo có quyền gì? | `STAFF`; dữ liệu dựng lại mỗi đêm | NFR-DEMO-01, 02 |
| D-15 | Demo có gửi email thật không? | Không; xem email của chính đơn đó ngay trên trang | NFR-DEMO-03 |
| D-16 | Giá đổi giữa lúc khách xem checkout và lúc bấm đặt? | Không tạo đơn; báo tổng mới để khách xác nhận | FR-CHK-12 |
| D-17 | Voucher hết lượt / hết hạn đúng lúc đặt? | Không tạo đơn, không tự bỏ voucher | FR-CHK-13 |
| D-18 | Voucher miễn phí giao hàng giảm bao nhiêu? | Tối đa bằng phí Tiêu chuẩn; chọn phương thức đắt hơn thì trả phần chênh | BR-VCH-01, 04 |
| D-19 | Số lượng trong giỏ lớn hơn ATS nhưng vẫn còn hàng? | Báo "chỉ còn N", chặn thanh toán, không tự giảm | FR-CART-08 |
| D-20 | Gộp giỏ khi đăng nhập làm vượt giới hạn? | Cắt về mức tối đa và báo khách | FR-CART-03 |
| D-21 | Huỷ đơn VNPay chưa trả tiền: trục thanh toán ra sao? Thanh toán lại có gia hạn không? | `EXPIRED`; không gia hạn | Mục 6.2, FR-PAY-07 |
| D-22 | Nhân viên có huỷ được đơn đã đóng gói không? | Có, khi hàng chưa rời kho; khách thì không | FR-ADM-ORD-07, mục 6.1, 6.3 |
| D-23 | ATS có thể âm không? | Hiển thị và bán theo max(0, …) | BR-INV-01 |
| D-24 | Hoàn bao nhiêu tiền? Thao tác hoàn thủ công có hạn mức không? "Tới 2 triệu" là ≤ hay <? | Hoàn đúng số đã thu, gồm phí ship; có hạn mức; ≤ | BR-ORD-07, FR-PAY-11, FR-ADM-ORD-08 |
| D-25 | "Sắp hết" là bao nhiêu? | 0 < ATS ≤ 2, cấu hình được (tồn mỗi SKU chỉ khoảng một chữ số) | FR-ADM-02 |
| D-26 | Giới hạn số lần tra cứu đơn và áp voucher? | 10 lần / 15 phút | NFR-SEC-02 |
| D-27 | Phiên khách 30 ngày tính thế nào? | Từ lần hoạt động cuối | NFR-SEC-08 |
| D-28 | Múi giờ của các mốc nghiệp vụ? | Giờ Việt Nam | C-09 |
| D-29 | Doanh thu thuần gom theo ngày nào, gồm đơn nào? | Ngày đặt; đơn `PLACED` / `COMPLETED` | FR-ADM-01 |
| D-30 | "Tổng chi tiêu" của hội viên? | Tổng đơn `PLACED` / `COMPLETED` (sửa câu chữ ở v1.5, xem D-58) | FR-ACC-05 |
| D-31 | Các mức lọc giá? | 4 mức, cấu hình được | FR-CAT-02 |
| D-32 | Giới hạn mật khẩu và email? | Mật khẩu 8–64 ký tự (BCrypt chỉ dùng 72 byte đầu); email không phân biệt hoa thường | FR-ACC-01 |
| D-33 | "Thanh toán treo" là gì? | Giao dịch quá hạn mà querydr chưa trả lời được | FR-PAY-10, FR-ADM-ORD-10 |
| D-34 | Phường/xã chọn hay gõ? | Gõ, có gợi ý; danh mục chuẩn ở giai đoạn 2 | FR-CHK-02 |
| D-35 | Tài khoản `ADMIN` đầu tiên tạo thế nào? Danh mục và voucher quản lý ở đâu? | `ADMIN` từ biến môi trường ở lần chạy đầu; danh mục và voucher qua dữ liệu mẫu | FR-ADM-05, mục 3.1 |
| D-36 | Có trang điều khoản, chính sách bảo mật không? Tuân thủ dữ liệu cá nhân tới đâu? | Có trang tĩnh; tuân thủ Nghị định 13/2023 ở mức cơ bản | FR-CNT-01, C-08 |
| D-37 | Hoả tốc áp cho đơn nào, ngày nào, kiểm 14:00 lúc nào? | Chỉ đơn VNPay; mọi ngày; kiểm lúc bấm đặt | FR-CHK-03 |
| D-38 | Nhãn sản phẩm "Thành viên" trong prototype nghĩa là gì? | Bỏ trong MVP; sản phẩm dành riêng hội viên để giai đoạn 2 | Mục 3.2 |

D-39 trở đi được chốt ở v1.5 sau một lượt kiểm chéo: mỗi yêu cầu được đặt cạnh mọi yêu cầu khác nói về cùng đối tượng, để tìm các cặp không thể cùng đúng.

| Mã | Câu hỏi | Quyết định | Áp dụng tại |
|---|---|---|---|
| D-39 | FR-CAT-07 đòi trang đếm ngược, NFR-SEO-06 đòi 404 cho sản phẩm chưa mở bán. Theo bên nào? | Sắp mở bán: 200, đếm ngược, `noindex`, không vào sitemap. Nháp và ngừng bán: 404 | FR-CAT-07, NFR-SEO-06, mục 6.5 |
| D-40 | Lưu URL ảnh đầy đủ thì đổi nguồn ảnh phải sửa dữ liệu, trái A-04. Lưu gì? | Lưu khoá ảnh kèm chiều rộng, chiều cao, mô tả; ghép URL lúc trả ra | BR-CAT-01, FR-ADM-PRD-02, mục 2.2, 8 |
| D-41 | Khuyến mãi không có thời hạn thì "khuyến mãi vừa hết" ở FR-CHK-12 nghĩa là gì? | Khuyến mãi có khoảng hiệu lực `[bắt đầu, kết thúc)`, vẫn đặt ở mức sản phẩm (D-12) | BR-PRC-01, BR-PRC-09, FR-ADM-PRD-08 |
| D-42 | IPN thành công tới khi trạng thái thanh toán đã là `EXPIRED`, nhưng bảng 6.2 chỉ có `PAID → REFUND_PENDING` | Thêm bước `EXPIRED → REFUND_PENDING` | Mục 6.2, FR-PAY-06 |
| D-43 | BR-INV-04 nói "tại mọi thời điểm", nhưng tăng tồn an toàn hợp lệ làm điều đó sai | Tách hai bất biến: `Reserved ≤ On hand` luôn đúng; `Reserved ≤ On hand − Safety stock` chỉ kiểm lúc giữ hàng | BR-INV-04 |
| D-44 | Mục 2.2 nói production gửi SMTP, NFR-DEMO-03 nói demo không gửi email ra ngoài | Mọi môi trường gửi SMTP; dev và demo trỏ tới Mailpit nội bộ, không công khai; trang xem email đọc nội dung đã lưu | Mục 2.2, 4.11, NFR-DEMO-03 |
| D-45 | `STAFF` được tạo sản phẩm nhưng không được đặt giá, mà sản phẩm cần giá | `STAFF` tạo và sửa nội dung ở trạng thái nháp; `ADMIN` đặt giá, khuyến mãi, đăng bán, ngừng bán | Mục 2.1, FR-ADM-PRD-02, 03, 08, 09 |
| D-46 | Sản phẩm có những trạng thái nào, "sắp mở bán" có phải trạng thái riêng? | `DRAFT`, `PUBLISHED`, `ARCHIVED`. Đang bán và sắp mở bán được suy ra từ giờ mở bán, nên tới giờ là tự mở bán mà không cần tác vụ nền | Mục 6.5, FR-ADM-PRD-01, 09 |
| D-47 | "Nổi bật", "hàng mới", gợi ý size dựa vào dữ liệu gì? | Thứ hạng nổi bật và form dáng do nhân viên nhập; "Mới" và "−N%" suy ra, không nhập tay | FR-CAT-03, 05, 09, FR-ADM-PRD-02 |
| D-48 | Khách vãng lai mở lại trang đơn (sau khi đặt, từ email) bằng cách nào mà không lộ đơn người khác? | Link chứa mã bí mật 128 bit, server chỉ lưu bản băm, hiệu lực 90 ngày | FR-ORD-03, FR-ORD-07, NFR-SEC-03 |
| D-49 | Tài khoản dùng thử công khai: ai cũng đổi được mật khẩu và khoá người khác ra ngoài | Tài khoản demo không đổi được mật khẩu | NFR-DEMO-01, FR-ADM-06 |
| D-50 | Trên trang danh mục có lọc theo danh mục khác không? | Không; bộ lọc danh mục chỉ có ở trang tất cả sản phẩm và trang tìm kiếm | FR-CAT-01, FR-CAT-02 |
| D-51 | "Huỷ trước khi giao" có gồm giao thất bại (hàng đã rời kho)? | Trả lượt voucher mỗi khi đơn bị huỷ, với mọi lý do | BR-VCH-06 |
| D-52 | `FAILED → PENDING` (thanh toán lại) chỉ nằm trong chú thích | Thành một dòng của bảng 6.2 | Mục 6.2 |
| D-53 | Database có ô đồng ý nhận tin ở checkout mà SRS không yêu cầu | Bỏ khỏi đơn hàng. Đồng ý nhận tin chỉ có lúc đăng ký (FR-ACC-01) | `03-database.md` |
| D-54 | Mật khẩu nhân viên theo quy tắc nào? Nhân viên có tự đổi mật khẩu được không? | Cùng quy tắc FR-ACC-01; thêm FR-ADM-06 (mức M vì FR-ADM-05 bắt đổi mật khẩu ở lần đầu) | FR-ADM-04, FR-ADM-06 |
| D-55 | NFR-SEO-03 bắt buộc nhưng lấy dữ liệu từ FR-ADM-PRD-05 (mức C) | Sinh tiêu đề và mô tả mặc định; FR-ADM-PRD-05 chỉ để ghi đè | NFR-SEO-03, FR-ADM-PRD-05 |
| D-56 | Voucher miễn phí giao hàng dùng trên đơn đã được miễn phí giao Tiêu chuẩn? | Không áp dụng, báo lý do, không tốn lượt | BR-VCH-01 |
| D-57 | Email đầu tiên của đơn COD gửi lúc đặt hay lúc xác nhận? Đơn VNPay có email lúc đặt không? | COD: lúc đặt. VNPay: chỉ khi thanh toán thành công | FR-NTF-01 |
| D-58 | "Trừ số đã hoàn" trong tổng chi tiêu và doanh thu có cần không? | Không: đơn bị hoàn tiền luôn ở `CANCELLED` nên đã bị loại. Bỏ câu chữ thừa | FR-ACC-05, FR-ADM-01, D-05, D-30 |
| D-59 | Đổi slug của sản phẩm đã đăng bán? | Không cho đổi, để link và thứ hạng tìm kiếm không gãy | BR-CAT-02, NFR-SEO-02 |
| D-60 | Trang danh mục có tham số lọc có được index riêng không? | Không; canonical trỏ về trang không có tham số | NFR-SEO-02 |
