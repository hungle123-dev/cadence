# Kế hoạch kiểm thử — Cadence

| | |
|---|---|
| **Phiên bản tài liệu** | 1.1 |
| **Trạng thái** | Đã duyệt (baseline). Mọi thay đổi sau này ghi vào lịch sử thay đổi |
| **Căn cứ** | [`01-srs.md`](01-srs.md) v1.5, [`02-architecture.md`](02-architecture.md) v1.4, [`03-database.md`](03-database.md) v1.2 |

### Lịch sử thay đổi

| Phiên bản | Ngày | Thay đổi |
|---|---|---|
| 0.1 | 2026-11-24 | Bản đầu |
| 0.2 | 2026-11-24 | Theo SRS v1.4: sửa TC-PRC-08 (FREESHIP tối đa bằng phí Tiêu chuẩn), TC-CART-02, 03, TC-CHK-02, TC-SEC-05, 08, TC-PAY-14, TC-CAT-08; thêm test cho FR-CHK-12, 13, FR-CART-08, FR-ADM-05, FR-CNT, NFR-DEMO, C-09 và các quyết định D-09…D-38 |
| 1.0 | 2026-11-24 | Chốt baseline |
| 1.1 | 2026-11-24 | Theo SRS v1.5: sửa TC-CAT-04, 06, 08, 11, TC-CART-04, TC-VCH-04, TC-INV-09, TC-SEC-06, TC-ADM-06, TC-ACC-01, TC-DEMO-01, TC-NTF-01, TC-SEO-02, 03, 05; thêm TC-CAT-13…18 (khuyến mãi có thời hạn, vòng đời sản phẩm, khoá ảnh, nhãn, slug, bộ lọc danh mục), TC-VCH-05, TC-SEC-18, 19, TC-SEO-06 |

### Cách đọc

- Mục 1–3: test cái gì, bằng cách nào, ở tầng nào.
- Mục 4: bảng đối chiếu yêu cầu SRS → test case. Đây là phần dùng hằng ngày khi code.
- Mục 5: thiết kế chi tiết các test khó (đồng thời, IPN, tính tiền, máy trạng thái, bảo mật).
- Mục 6–10: dữ liệu test, môi trường, CI, tiêu chí hoàn thành, xử lý lỗi.

---

## 1. Mục tiêu và phạm vi

### 1.1 Mục tiêu

Chứng minh bằng test tự động rằng các mục tiêu của SRS đạt được, nhất là ba mục tiêu khó:

| Mục tiêu SRS | Test chứng minh |
|---|---|
| **G2.** Không bán quá tồn kho | TC-CON-01, TC-CON-02, TC-INV-* (mục 5.1) |
| **G3.** Tiền luôn khớp | TC-PRC-*, TC-PAY-*, TC-CON-03…05 (mục 5.2, 5.3) |
| **G4.** Chất lượng như sản phẩm thật | Toàn bộ kế hoạch này chạy trong CI |

### 1.2 Trong phạm vi

Mọi yêu cầu mức **M** và **S** của SRS; các NFR có số đo (NFR-01…07, NFR-SEC, NFR-SEO, NFR-UX); luật kiến trúc (ArchUnit); ràng buộc DB.

### 1.3 Ngoài phạm vi

| Không test | Vì sao |
|---|---|
| VNPay production, giao dịch tiền thật | SRS C-05: chỉ dùng sandbox |
| Email có vào hộp thư thật hay bị lọc spam không | Phụ thuộc nhà cung cấp SMTP; chỉ test email được **gửi đi** đúng nội dung (Mailpit) |
| Trình duyệt cũ, Firefox | Test trên Chromium (máy tính và giả lập điện thoại) và WebKit (gần với Safari trên iPhone) |
| Yêu cầu mức **C** và giai đoạn 2 | Test khi làm tới |
| Test xâm nhập chuyên sâu | Ngoài khả năng của dự án; thay bằng test bảo mật tự động (mục 5.6) và quét phụ thuộc |

---

## 2. Chiến lược

### 2.1 Nguyên tắc

| # | Nguyên tắc | Vì sao |
|---|---|---|
| T1 | **Test luật nghiệp vụ, không test getter.** Mỗi test gắn với một mã yêu cầu hoặc tiêu chí chấp nhận trong SRS | Test có giá trị là test bảo vệ một quy tắc; test getter chỉ làm chậm việc sửa code |
| T2 | **Nhiều test ở domain, ít test qua trình duyệt** (kim tự tháp test) | Test domain chạy trong mili giây và chỉ ra đúng chỗ sai; test E2E chậm và dễ chập chờn |
| T3 | **Không dùng H2 thay PostgreSQL.** Test tích hợp chạy trên PostgreSQL thật bằng Testcontainers | SQL, ràng buộc `CHECK`, index một phần, `SKIP LOCKED`, cơ chế khoá đều khác nhau giữa H2 và PostgreSQL. Bài toán đồng thời chỉ kiểm được trên DB thật |
| T4 | **Đóng băng thời gian bằng `Clock.fixed(…)`** | Test các mốc 15 phút, 48 giờ, 7 ngày, 14:00 mà không phải chờ, và kết quả không đổi theo giờ chạy |
| T5 | **Mỗi test tự tạo dữ liệu của nó**, không dựa vào dữ liệu test khác để lại | Test chạy được độc lập và theo thứ tự bất kỳ |
| T6 | **Test đồng thời kiểm bằng con số, không bằng thời gian** | "Đúng 5 đơn thành công" luôn đúng hoặc luôn sai; "chạy xong trong 2 giây" thì chập chờn |
| T7 | **Không có test chập chờn được chấp nhận.** Test chập chờn bị cách ly trong 24 giờ và có issue theo dõi; không bật "thử lại tới khi xanh" | Test thỉnh thoảng đỏ làm mọi người quen bỏ qua màu đỏ |

### 2.2 Các tầng test

| Tầng | Kiểm cái gì | Công cụ | Chạy khi | Tốc độ |
|---|---|---|---|---|
| **Domain** | Aggregate, value object, hàm tính tiền, máy trạng thái, chính sách phân quyền | JUnit 5, AssertJ | Mỗi lần lưu file, mỗi PR | Mili giây |
| **Service** | Use case điều phối đúng: tải → gọi domain → lưu → phát sự kiện | JUnit 5, Mockito (giả port) | Mỗi PR | Mili giây |
| **Adapter** | SQL, mapping JPA, ràng buộc DB, lệnh ghi có điều kiện | Testcontainers PostgreSQL + migration Flyway thật | Mỗi PR | Giây |
| **HTTP** | Status, JSON, mã lỗi, phân quyền, CSRF | `@SpringBootTest` + MockMvc | Mỗi PR | Giây |
| **Đồng thời** | NFR-04…07 | Testcontainers + `ExecutorService` | Mỗi PR | Vài giây |
| **Kiến trúc** | Luật R1–R7, không vòng phụ thuộc giữa module | ArchUnit | Mỗi PR | Giây |
| **Tích hợp cổng thanh toán** | Chữ ký, querydr, refund | WireMock (giả VNPay) | Mỗi PR | Giây |
| **E2E** | Luồng người dùng thật qua trình duyệt | Playwright trên stack Docker Compose | Bộ rút gọn mỗi PR; bộ đầy đủ hằng đêm | Phút |
| **Hiệu năng** | NFR-01, NFR-02 | k6 | Hằng đêm, hoặc chạy tay trước khi phát hành | Phút |
| **Frontend tĩnh** | NFR-03 (LCP), NFR-SEO, NFR-UX-02 | Lighthouse CI, Playwright, axe-core | Hằng đêm | Phút |

**Không đặt ngưỡng % coverage.** Thay vào đó dùng bảng đối chiếu ở mục 4: mọi yêu cầu mức M phải có ít nhất một test. Coverage cao vẫn có thể bỏ sót luật quan trọng; bảng đối chiếu thì không.

### 2.3 Đặt tên

| Loại | Quy ước | Ví dụ |
|---|---|---|
| Lớp test | Tên lớp được test + loại | `OrderTest`, `PlaceOrderServiceTest`, `InventoryReservationJdbcAdapterIntegrationTest`, `PlaceOrderConcurrencyTest` |
| Method | `method_hànhVi` (template) | `cancel_rejectsAnOrderThatIsAlreadyPacked` |
| Mã test case | `TC-<NHÓM>-<số>`, ghi trong `@DisplayName` để tra ngược từ báo cáo | `@DisplayName("TC-CON-01 …")` |

---

## 3. Công cụ hỗ trợ dùng chung

| Công cụ | Để làm gì |
|---|---|
| `PostgresTestContainer` | Một container dùng chung cho cả lượt chạy (khởi động một lần), Flyway chạy migration thật |
| Test data builders | `aProduct().withVariant("42", stock(5)).build()`, `aCart().with(variant, 1)`… để mỗi test tạo dữ liệu bằng vài dòng dễ đọc |
| `MutableClock` | `Clock` có thể tua tới trong test của tác vụ nền |
| `VnpaySigner` (test) | Ký IPN giả bằng khoá bí mật dùng riêng cho test, để gửi IPN hợp lệ hoặc cố tình sai |
| `FakeMailSender` | Ghi lại email "đã gửi" trong bộ nhớ cho test tích hợp; E2E dùng Mailpit và đọc qua API của Mailpit |
| `InvariantChecker` | Chạy các câu SQL đối soát (mục 5.1.4) sau mỗi test đồng thời và test tác vụ nền |

---

## 4. Đối chiếu yêu cầu → test case

Tầng: **D** domain, **S** service, **A** adapter, **H** HTTP, **C** đồng thời, **E** E2E, **P** hiệu năng / frontend tĩnh.

### 4.1 Danh mục và tìm kiếm

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-CAT-01 | FR-CAT-01 | Phân trang 12 sản phẩm, "Xem thêm" trả đúng trang tiếp theo, không trùng, không sót | A, H |
| TC-CAT-02 | FR-CAT-02 | Lọc nhiều lựa chọn kết hợp; facet count đúng với dữ liệu mẫu | A |
| TC-CAT-03 | FR-CAT-02 | Size hết hàng **không** được đếm khi lọc; giữ hàng làm ATS về 0 thì size biến mất khỏi bộ lọc; trả hàng thì size xuất hiện lại | A, C |
| TC-CAT-04 | FR-CAT-03 | Mỗi kiểu sắp xếp cho thứ tự đúng; giá dùng giá khuyến mãi **đang hiệu lực**; "nổi bật": có thứ hạng trước, không thứ hạng sau, cùng hạng thì mới hơn trước; nhiều sản phẩm bằng nhau ở khoá sắp xếp: duyệt hết bằng "Xem thêm" không trùng, không sót | A |
| TC-CAT-05 | FR-CAT-06 | PDP: size ATS = 0 bị khoá; API thêm vào giỏ từ chối size đó | H, E |
| TC-CAT-06 | FR-CAT-07, mục 6.5 | Sản phẩm sắp mở bán: không có trong PLP, tìm kiếm, sitemap; API chi tiết trả sản phẩm kèm cờ sắp mở bán; thêm vào giỏ bị từ chối; tới đúng giờ mở bán (tua `Clock`, kiểm cả mốc `launch_at` − 1 giây và `launch_at`) thì xuất hiện và mua được | D, A, H |
| TC-CAT-07 | FR-CAT-08 | "giay chay bo" khớp "Giày chạy bộ"; "đ" ↔ "d"; "hoodie" và "ao ni" ra "Club Hoodie"; "ni" **không** khớp "organic"; xếp hạng: khớp đầu tên đứng trước | D (bỏ dấu, đồng nghĩa), A (truy vấn) |
| TC-CAT-08 | FR-ADM-PRD-02, 03, 08, 09, BR-PRC-08 | Tạo, sửa sản phẩm; giá đặt một lần cho cả sản phẩm, mọi size cùng giá. `STAFF` tạo và sửa nội dung được; `STAFF` đặt giá, đặt khuyến mãi, đăng bán, ngừng bán đều bị từ chối (403); `ADMIN` làm được | D, H |
| TC-CAT-13 | BR-PRC-01, 09, FR-CHK-12 | Khuyến mãi `[10:00, 12:00)`: lúc 09:59:59 tính giá gốc; 10:00 tính giá khuyến mãi; 11:59:59 giá khuyến mãi; 12:00 giá gốc. Không có thời điểm kết thúc: còn hiệu lực mãi. Giá khuyến mãi ≥ giá gốc, kết thúc ≤ bắt đầu bị từ chối ở domain và ở DB. Khuyến mãi hết giữa lúc xem checkout và lúc đặt: không tạo đơn, báo tổng mới. Giá lọc và sắp xếp bằng SQL khớp giá domain trên toàn bộ dữ liệu mẫu ở nhiều mốc thời gian | D, A, H |
| TC-CAT-14 | Mục 6.5, FR-ADM-PRD-09 | Test tham số hoá mọi cặp (trạng thái × hành động) theo bảng 6.5. Đăng bán sản phẩm thiếu giá, không phối màu, phối màu không ảnh, phối màu không size: bị từ chối và trả **đủ** mọi lý do một lần. `PUBLISHED` không về được `DRAFT`. Nháp và ngừng bán: API chi tiết trả 404 | D, H |
| TC-CAT-15 | BR-CAT-01 | API trả URL ảnh = gốc nguồn ảnh cấu hình + khoá; đổi cấu hình thì URL đổi, dữ liệu không đổi; ghi khoá có dạng `https://…` bị `CHECK` từ chối; dòng đơn lưu khoá, không lưu URL | D, A |
| TC-CAT-16 | FR-CAT-09 | Nhãn "Mới" xuất hiện khi giờ mở bán trong 30 ngày, mất ở ngày thứ 31 (tua `Clock`); "−N%" làm tròn xuống, chỉ khi khuyến mãi đang hiệu lực; "Hết hàng" khi mọi variant ATS = 0 | D, A |
| TC-CAT-17 | BR-CAT-02 | Đổi slug sản phẩm nháp được; đổi slug sản phẩm đã đăng bán bị từ chối; slug có dấu, chữ hoa, khoảng trắng bị từ chối; slug trùng bị từ chối | D, A |
| TC-CAT-18 | FR-CAT-01, 02 | API danh sách theo danh mục bỏ qua (từ chối) tham số lọc danh mục và không trả facet danh mục; trang tất cả sản phẩm và tìm kiếm có facet danh mục | H |
| TC-CAT-12 | FR-CAT-02 | Lọc khoảng giá theo giá đang bán (sản phẩm giảm từ 2.890.000 xuống 2.290.000 nằm ở mức 2–3 triệu); đổi cấu hình các mức thì bộ lọc đổi theo | A |
| TC-CAT-09 | FR-ADM-PRD-06 | Xoá sản phẩm đã đăng bán (đã hoặc chưa có đơn) bị từ chối, chỉ chuyển `ARCHIVED` được; xoá nháp được | D, A |
| TC-CAT-10 | FR-ADM-PRD-07 | Sửa giá không đổi dòng đơn cũ; giỏ đang có sản phẩm đó báo "Giá đã thay đổi" | A, H |

### 4.2 Giỏ hàng

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-CART-01 | FR-CART-01 | Thêm trùng variant thì cộng dồn; thiếu size thì lỗi 400 | D, H |
| TC-CART-02 | FR-CART-02, FR-CART-08, BR-ORD-06 | Thêm cái thứ 6 cùng variant bị từ chối; tổng giỏ vượt 10 bị từ chối. Dòng đang có 3 mà ATS giảm còn 2: giỏ báo "Chỉ còn 2", chặn thanh toán, **không** tự giảm số lượng | D, S |
| TC-CART-03 | FR-CART-03 | Đăng nhập: giỏ ẩn danh gộp vào giỏ tài khoản, cùng variant thì cộng dồn. Ví dụ 4 + 3 cùng variant → còn 5, kết quả gộp liệt kê dòng bị cắt; tổng vượt 10 cũng cắt và báo | D, S |
| TC-CART-04 | FR-CART-04 | Mở giỏ có dòng hết hàng, dòng của sản phẩm đã ngừng bán, dòng của sản phẩm bị dời giờ mở bán về tương lai → báo lỗi từng dòng, chặn thanh toán; có dòng đổi giá (sửa giá, khuyến mãi bắt đầu, khuyến mãi kết thúc) → báo giá cũ và giá mới | S, H |
| TC-CART-05 | FR-CART-07 | Thêm vào giỏ **không** làm thay đổi `reserved` | A |
| TC-CART-06 | Giỏ đồng thời | Hai request cùng thêm hàng vào một giỏ: một bên nhận `409 CONCURRENT_UPDATE`, giỏ không vượt giới hạn | C |

### 4.3 Tính tiền và voucher

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-PRC-01…10 | BR-PRC-01…07 | Bảng ví dụ ở mục 5.2 | D |
| TC-VCH-01 | BR-VCH-01, FR-CART-06 | Mã không tồn tại; chưa đủ giá trị tối thiểu (báo còn thiếu bao nhiêu); hết hạn; chưa tới ngày bắt đầu | D, H |
| TC-VCH-02 | BR-VCH-02 | Áp voucher thứ hai thay thế voucher thứ nhất, không cộng dồn | D |
| TC-VCH-03 | BR-VCH-05, NFR-07 | Xem TC-CON-04 | C |
| TC-VCH-04 | BR-VCH-06 | Mỗi đường huỷ (khách huỷ, nhân viên huỷ, hết hạn thanh toán, COD quá 48 giờ, giao thất bại) trả lại đúng một lượt; huỷ lặp lại không trả thêm; đơn đã giao không trả | A, C |
| TC-VCH-05 | BR-VCH-01 | `FREESHIP` trên đơn giao Tiêu chuẩn đã miễn phí (đạt mốc, hoặc hội viên): báo "Đơn đã được miễn phí giao hàng", `used_count` không đổi. Cùng đơn đổi sang Nhanh: áp được, giảm 30.000 ₫ | D, A |

### 4.4 Checkout và đặt hàng

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-CHK-01 | FR-CHK-01, 07 | Email sai định dạng, số điện thoại không đủ 10 số hoặc không bắt đầu bằng 0 → 400 kèm danh sách field | H |
| TC-CHK-02 | FR-CHK-03, C-09 | Hoả tốc: TP. HCM, VNPay, bấm đặt lúc 13:59 giờ Việt Nam được; 14:00 bị từ chối (kể cả khi trang checkout được mở lúc 13:50); Đà Nẵng bị từ chối; **COD** bị từ chối; Chủ nhật vẫn được. DB cũng chặn hoả tốc + COD bằng `CHECK` | D, A |
| TC-CHK-03 | FR-CHK-05 | Mã số thuế 10 hoặc 13 số được; 11 số bị từ chối; có tên công ty mà thiếu mã số thuế bị từ chối | D, H |
| TC-CHK-04 | FR-CHK-08 | Request gửi giá 1 ₫: đơn vẫn tính theo giá trên server | H |
| TC-CHK-05 | FR-CHK-08, 10 | Đơn 3 dòng, dòng 3 hết hàng: không tạo đơn, dòng 1 và 2 **không** bị giữ, voucher **không** bị trừ lượt; lỗi chỉ rõ SKU hết hàng | A, H |
| TC-CHK-06 | FR-CHK-09 | Gửi lại cùng idempotency key (tuần tự): trả đúng đơn cũ, HTTP 200 | H |
| TC-CHK-07 | FR-CHK-09 | Cùng key nhưng từ giỏ khác: bị từ chối, **không** lộ thông tin đơn | H |
| TC-CHK-08 | FR-CHK-11, BR-ORD-01 | Đơn lưu snapshot; đổi tên và giá sản phẩm sau đó không làm đổi đơn | A |
| TC-CHK-09 | FR-NTF-01 | Đặt hàng thành công ghi đúng một dòng outbox; đặt hàng thất bại không ghi | A |
| TC-CHK-10 | FR-CHK-12 | Admin đổi giá sau khi khách mở checkout: request gửi tổng cũ → `409 ORDERING_PRICE_CHANGED` kèm tổng mới, **không** giữ hàng, **không** tạo đơn; gửi lại với tổng mới thì thành công | S, H |
| TC-CHK-11 | FR-CHK-13 | Voucher còn 1 lượt, người khác dùng mất ngay trước khi khách bấm đặt: không tạo đơn, lỗi `ORDERING_VOUCHER_UNAVAILABLE`; không có đơn nào được tạo "không kèm voucher" | S, H |

### 4.5 Thanh toán

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-PAY-01…09 | FR-PAY-02…06 | Bảng IPN ở mục 5.3 | D, A, H |
| TC-PAY-10 | FR-PAY-07 | Thanh toán `FAILED` còn hạn: tạo giao dịch mới với `txn_ref` mới; quá hạn thì không cho | D, S |
| TC-PAY-11 | FR-PAY-08 | Trang kết quả khi chưa có IPN hiện "Đang chờ xác nhận"; có IPN thì tự chuyển | E |
| TC-PAY-12 | FR-PAY-09 | Tua `Clock` qua 15 phút: querydr (WireMock) trả "chưa thanh toán" → đơn `EXPIRED` + `CANCELLED`, hàng trả về ATS; querydr trả "đã thanh toán" → đơn `PLACED` | S, A |
| TC-PAY-13 | FR-PAY-09 | querydr timeout → đơn **giữ nguyên** (chưa biết), lượt sau hỏi lại; không huỷ nhầm | S |
| TC-PAY-14 | FR-PAY-11, BR-ORD-07 | Hoàn tiền: số tiền = số đã thu của giao dịch (gồm phí ship); refund gọi đúng một lần; lỗi mạng thì **không** tự gửi lại; bấm hoàn hai lần bị chặn bởi `UNIQUE` | S, A |
| TC-PAY-15 | FR-PAY-12 | Đối soát hằng đêm cập nhật giao dịch `PENDING` cũ theo kết quả querydr; giao dịch lệch vào danh sách việc cần làm | S |
| TC-PAY-17 | Mục 6.2 SRS | Khách huỷ đơn VNPay chưa trả: thanh toán → `EXPIRED`, hàng trả về. Sau đó IPN thành công tới: xử lý như TC-PAY-07 (hoàn tiền, không mở lại đơn) | D, S |
| TC-PAY-18 | FR-PAY-07 | Đặt lúc 10:00, thất bại lúc 10:10, thanh toán lại: hạn vẫn là 10:15, không phải 10:25 | D |
| TC-PAY-16 | Trả tiền hai lần | Hai giao dịch của cùng một đơn đều nhận IPN thành công: đơn `PLACED` một lần, giao dịch thứ hai tạo yêu cầu hoàn tiền | S, A |

### 4.6 Đơn hàng và máy trạng thái

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-ORD-01 | SRS mục 6 | Mọi bước chuyển hợp lệ thành công; mọi bước không hợp lệ ném lỗi (mục 5.4) | D |
| TC-ORD-02 | SRS 6.4 | Ràng buộc giữa các trục: lưu thẳng một tổ hợp vô lý xuống DB bị `CHECK` từ chối | A |
| TC-ORD-03 | FR-ORD-02 | Tra cứu đúng mã + đúng số điện thoại hoặc email: thấy đơn; sai một trong hai: thông báo chung "Không tìm thấy đơn", cùng status, cùng nội dung như khi mã đơn không tồn tại | H |
| TC-ORD-04 | FR-ORD-04, 05 | Khách huỷ đơn `UNFULFILLED` được; đơn `PACKED` không được; huỷ đơn đã trả tiền → `REFUND_PENDING` | D, H |
| TC-ORD-05 | BR-ORD-03 | Mỗi bước chuyển ghi đúng một `order_events` với đúng người thực hiện | A |
| TC-ORD-06 | BR-ORD-04 | Tua `Clock` 7 ngày sau khi giao → `COMPLETED`; 6 ngày 23 giờ thì chưa | S |
| TC-ORD-07 | BR-ORD-05 | Đơn COD chưa xác nhận quá 48 giờ → tự huỷ, hàng trả về; 47 giờ 59 phút thì chưa | S |
| TC-ORD-08 | FR-ADM-ORD-05 | Giao cho vận chuyển thiếu mã vận đơn → 400; đủ thì `on_hand` và `reserved` cùng giảm, sổ cái có dòng `SHIP` | D, A |
| TC-ORD-09 | FR-ADM-ORD-06 | Giao thất bại: đơn `CANCELLED`, hàng nhập lại (`RETURN_RESTOCK`), đã trả tiền thì tạo hoàn tiền | S, A |
| TC-ORD-10 | FR-ADM-ORD-08 | Xem TC-SEC-05 | D |
| TC-ORD-13 | FR-ADM-ORD-07, mục 6.3 SRS | Nhân viên huỷ đơn `PACKED`: đơn `CANCELLED`, giao hàng về `UNFULFILLED` trong cùng thao tác, hàng về ATS; khách huỷ đơn `PACKED` bị từ chối; đơn `SHIPPED` thì không ai huỷ được | D, H |
| TC-ORD-14 | FR-ORD-01, C-09 | Đơn đặt lúc 00:30 ngày 25/11 giờ Việt Nam (17:30 UTC ngày 24/11) có mã bắt đầu `CAD-261125-` | D |
| TC-ORD-11 | Huỷ đồng thời | Khách huỷ đúng lúc nhân viên bấm đóng gói: đúng một bên thành công, bên kia nhận 409 | C |

### 4.7 Tồn kho

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-INV-01 | FR-INV-02 | Nhập kho số âm bị từ chối; thiếu chứng từ bị từ chối | D, H |
| TC-INV-02 | FR-INV-03 | Điều chỉnh làm `on_hand` nhỏ hơn `reserved` bị từ chối | D, A |
| TC-INV-03 | BR-INV-05 | Sau một chuỗi nhập, giữ, xuất, điều chỉnh, hoàn: `on_hand` = tổng sổ cái | A |
| TC-INV-04 | P4 (database) | Ứng dụng chạy `UPDATE` hoặc `DELETE` trên `inventory_movements` bị DB từ chối vì thiếu quyền | A |
| TC-INV-05 | Kiến trúc 7.8 | ATS từ 1 về 0 phát đúng một sự kiện, cờ `available` = false; từ 0 lên 3 thì true; từ 5 về 4 thì **không** phát | A |
| TC-INV-09 | BR-INV-01, 04 | On hand 3, đang giữ 2, tăng tồn an toàn lên 3: thao tác được chấp nhận; ATS hiển thị 0 (không phải −2), size bị khoá trên storefront; 2 đơn đang giữ không bị ảnh hưởng; giữ thêm 1 bị từ chối. Điều chỉnh làm `on_hand < reserved` vẫn bị từ chối | D, A |
| TC-INV-06 | Điều chỉnh đồng thời | Nhân viên điều chỉnh đúng lúc có người giữ hàng: không ghi đè số liệu (một bên thử lại) | C |

### 4.8 Tài khoản, phân quyền, bảo mật

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-SEC-01 | FR-ACC-01, NFR-SEC-01 | Mật khẩu lưu dạng BCrypt (bắt đầu `$2`), không bao giờ là văn bản gốc; không xuất hiện trong log | A |
| TC-SEC-02 | FR-ACC-02 | Sai email và sai mật khẩu trả cùng một thông báo, cùng status | H |
| TC-SEC-03 | FR-ACC-03 | `?next=https://ke-xau.com` và `?next=//ke-xau.com` bị bỏ qua, quay về trang mặc định | H |
| TC-SEC-04 | NFR-SEC-03 | Cookie phiên của khách gọi API admin → 401; `STAFF` gọi API chỉ dành cho `ADMIN` → 403 | H |
| TC-SEC-05 | FR-ADM-ORD-08, FR-PAY-11 | `STAFF` hoàn 2.000.000 ₫ được; 2.000.001 ₫ bị từ chối; `ADMIN` hoàn 5.000.000 ₫ được. Áp dụng cả khi đánh dấu "Đã hoàn" thủ công | D, H |
| TC-SEC-06 | NFR-SEC-03 (IDOR) | Khách A xem hoặc huỷ đơn của khách B → 404, giống hệt khi đơn không tồn tại. Biết mã đơn hay id đơn mà không có phiên, link truy cập hay tra cứu thành công → 404 | H |
| TC-SEC-18 | FR-ORD-07 | Link truy cập đơn mở được đơn, huỷ được đơn; sửa một ký tự trong mã → 404; ngày 91 (tua `Clock`) → hết hạn; DB chỉ chứa bản băm, không chứa mã gốc; mã của đơn A không mở được đơn B; mã cấp từ tra cứu hết hạn sau 24 giờ, link trong email vẫn dùng được | A, H |
| TC-SEC-19 | FR-ADM-06, NFR-DEMO-01 | Nhân viên đổi mật khẩu: sai mật khẩu hiện tại bị từ chối, mật khẩu mới theo quy tắc FR-ACC-01, phiên khác bị huỷ. Tài khoản demo đổi mật khẩu → bị từ chối, mật khẩu không đổi | D, H |
| TC-SEC-07 | NFR-SEC-05 | Request `POST` thiếu CSRF token → 403 | H |
| TC-SEC-08 | NFR-SEC-02 | Đăng nhập sai lần thứ 6 trong 15 phút → 429 kèm `Retry-After`. Tra cứu đơn lần thứ 11 / 15 phút / IP → 429. Áp voucher lần thứ 11 / 15 phút / giỏ → 429 | H |
| TC-SEC-09 | FR-ADM-04 | Khoá nhân viên: request tiếp theo của người đó → 401 ngay | H |
| TC-SEC-10 | FR-ACC-06 | Đổi mật khẩu: phiên trên "máy khác" → 401; phiên hiện tại vẫn dùng được | H |
| TC-SEC-11 | NFR-SEC-08 | Session id đổi sau khi đăng nhập; phiên nhân viên hết sau 30 phút không thao tác (tua `Clock`) | H |
| TC-SEC-12 | NFR-SEC-07 | Đăng nhập thành công, sai mật khẩu, email không tồn tại đều ghi `staff_login_logs` | A |
| TC-SEC-13 | Kiến trúc 10.4 | Khách và nhân viên cùng email: khoá nhân viên không đăng xuất phiên khách | H |
| TC-SEC-14 | NFR-SEC-05 | Dữ liệu chứa `<script>` (tên, ghi chú) hiển thị dạng chữ, không chạy | E |
| TC-SEC-16 | FR-ACC-01 | Mật khẩu 7 ký tự bị từ chối, 8 và 64 được, 65 bị từ chối. Đăng ký `An@Mail.com` rồi `an@mail.com` bị báo trùng; đăng nhập bằng chữ hoa vẫn được. Ô nhận tin khuyến mãi mặc định không chọn | D, H |
| TC-SEC-17 | NFR-SEC-08 | Phiên khách còn hiệu lực sau 29 ngày nếu vẫn hoạt động đều; hết sau 30 ngày không hoạt động | H |
| TC-SEC-15 | NFR-SEC-04 | Không có khoá bí mật nào trong repo (quét bằng gitleaks trong CI) | CI |

### 4.9 Thông báo

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-NTF-01 | FR-NTF-01…04 | Mỗi sự kiện sinh đúng một email với đúng người nhận và nội dung chính. Đơn COD: email ngay khi đặt. Đơn VNPay: không có email lúc đặt, có email khi IPN `PAID`. Mọi email có link truy cập đơn. Gửi xong thì dòng outbox có tiêu đề và nội dung đã dựng | A |
| TC-NTF-02 | Database 5.4 | SMTP lỗi: thử lại theo chờ tăng dần; quá 5 lần thì `FAILED`; đơn hàng không bị ảnh hưởng | S, A |
| TC-NTF-03 | Database 5.4 | App "sập" giữa lúc gửi và lúc đánh dấu: email được nhận lại sau 5 phút | A |
| TC-NTF-04 | Database 5.4 | Hai lượt chạy chồng nhau không nhận trùng email (`SKIP LOCKED`) | C |

### 4.10 Kiến trúc

| Mã | Test case | Tầng |
|---|---|---|
| TC-ARC-01 | R1–R4, R6, R7 của template: domain không phụ thuộc framework; application không phụ thuộc infrastructure | ArchUnit |
| TC-ARC-02 | R5: không module nào import `domain` / `application` của module khác | ArchUnit |
| TC-ARC-03 | Không có vòng phụ thuộc giữa các module | ArchUnit |
| TC-ARC-04 | Không có câu SQL nào đọc schema của module khác (quét file SQL và `@Query`) | ArchUnit / quét văn bản |

### 4.11 Frontend, SEO, trải nghiệm

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-SEO-01 | NFR-SEO-01 | Tải PDP với **JavaScript tắt**: HTML có tên, giá, mô tả | E |
| TC-SEO-02 | NFR-SEO-02, 03 | Mỗi trang công khai có `<title>`, mô tả, canonical riêng; `/danh-muc/giay?size=42&sort=price_asc` có canonical là `/danh-muc/giay` | E |
| TC-SEO-03 | NFR-SEO-04 | `sitemap.xml` chứa mọi sản phẩm đang bán, không chứa nháp, ngừng bán, sắp mở bán; `robots.txt` chặn đúng đường dẫn | E |
| TC-SEO-04 | NFR-SEO-05 | JSON-LD `Product` hợp lệ (kiểm theo schema) | E |
| TC-SEO-05 | NFR-SEO-06, FR-CAT-07 | Slug không tồn tại, sản phẩm nháp, sản phẩm ngừng bán → HTTP 404 thật; sản phẩm sắp mở bán → 200, có đếm ngược và `noindex`; giỏ hàng, tài khoản, tìm kiếm có `noindex` | E |
| TC-SEO-06 | NFR-SEO-03 | Sản phẩm không nhập tiêu đề, mô tả SEO: dùng mặc định "{tên} – {danh mục} \| Cadence" và tối đa 155 ký tự đầu của mô tả; nhập ghi đè thì dùng giá trị ghi đè | D, E |
| TC-UX-01 | NFR-UX-01 | Luồng mua hàng chạy được ở màn hình rộng 360 px | E |
| TC-UX-02 | NFR-UX-02 | axe-core: luồng mua hàng không có lỗi mức *serious* hoặc *critical*; hoàn tất checkout chỉ bằng bàn phím | E |
| TC-PERF-03 | NFR-03 | Lighthouse giả lập điện thoại: LCP < 2,5 s trên trang chủ, PLP, PDP | P |
| TC-CAT-11 | FR-CAT-05 | PDP hiển thị ảnh (có `width`, `height`, `alt`), tên, giá (giá gốc gạch ngang khi có khuyến mãi đang hiệu lực), phối màu, trạng thái từng size, thông số, gợi ý chọn size theo form dáng (nhỏ, rộng có gợi ý; đúng size không có) | E |
| TC-CART-07 | FR-CART-05 | API giỏ trả tạm tính, số tiền tiết kiệm, số tiền còn thiếu tới mốc miễn phí ship; giao diện hiển thị đúng | H, E |
| TC-UX-03 | NFR-UX-03 | Mọi mã lỗi trong các `<Module>ErrorCode` của backend đều có thông báo tiếng Việt ở frontend (test đọc danh sách mã từ OpenAPI và so với bảng thông báo) | CI |

### 4.12 Admin, vận hành và các quy tắc còn lại

| Mã | Yêu cầu | Test case | Tầng |
|---|---|---|---|
| TC-ADM-01 | FR-ADM-ORD-01 | Danh sách đơn: tìm theo mã, số điện thoại, email; lọc theo từng trục trạng thái và khoảng ngày; phân trang | A, H |
| TC-ADM-02 | FR-ADM-ORD-02 | Chi tiết đơn có snapshot, phần giảm phân bổ từng dòng, ba trạng thái, lịch sử thao tác kèm người thực hiện | H |
| TC-ADM-03 | FR-ADM-ORD-03 | API trả danh sách hành động hợp lệ theo trạng thái; với mọi trạng thái, danh sách này khớp bảng chuyển trạng thái của TC-ORD-01 | D, E |
| TC-ADM-04 | FR-ADM-ORD-04, BR-ORD-02 | Xác nhận đơn COD ghi nhận cuộc gọi; đóng gói đơn COD **chưa** xác nhận bị từ chối | D, H |
| TC-ADM-05 | FR-ADM-ORD-07 | Admin huỷ đơn thiếu lý do → 400; có lý do thì khách thấy lý do trên trang đơn | H |
| TC-ADM-06 | FR-ADM-PRD-01 | Danh sách sản phẩm: tìm, lọc theo danh mục và cách hiển thị (nháp, sắp mở bán, đang bán, ngừng bán) tại thời điểm truy vấn | A, H |
| TC-ADM-07 | FR-ADM-02 | Danh sách việc cần làm đếm đúng: đơn COD chờ xác nhận, đơn chờ đóng gói, thanh toán treo, yêu cầu hoàn tiền, SKU sắp hết (gom từ 3 module) | H |
| TC-ADM-08 | FR-ADM-03 | Xem TC-SEC-04 | H |
| TC-ADM-09 | FR-ADM-05 | DB trống + biến môi trường: lần chạy đầu tạo `ADMIN`, bắt đổi mật khẩu ở lần đăng nhập đầu; khởi động lại không tạo thêm; đã có `ADMIN` thì không đụng tới; thiếu biến môi trường thì ghi log cảnh báo, không tạo | S, A |
| TC-ADM-10 | FR-ADM-01, C-09 | Doanh thu: đơn đặt lúc 23:30 giờ Việt Nam được tính vào đúng ngày đó theo giờ Việt Nam; không tính phí ship; không tính đơn huỷ hay đơn còn chờ | A |
| TC-ACC-01 | FR-ACC-05 | Tổng chi tiêu = tổng đơn `PLACED` / `COMPLETED`; đơn huỷ (kể cả đơn đã hoàn tiền) và đơn chờ thanh toán không được tính | A |
| TC-CNT-01 | FR-CNT-01, C-08 | Ba trang nội dung tồn tại và có link ở chân trang, form đăng ký, checkout | E |
| TC-CNT-02 | FR-CNT-02 | Phí giao, mốc miễn phí ship hiển thị trên giao diện lấy từ cùng cấu hình với logic tính tiền: đổi cấu hình thì cả hai đổi theo | H, E |
| TC-DEMO-01 | NFR-DEMO-01, 03, 04 | Cấu hình demo: SMTP trỏ tới Mailpit trong mạng nội bộ, cổng Mailpit không mở ra ngoài (kiểm `compose.prod.yaml`); trang "Xem email" của đơn A hiện đúng nội dung đã lưu của đơn A, mở email của đơn B khi không có quyền xem đơn B → 404; tài khoản dùng thử công khai có vai trò `STAFF`, cờ demo bật; trang có ghi chú môi trường thử | H, E |
| TC-DEMO-02 | NFR-DEMO-02 | Tác vụ dựng lại dữ liệu demo chạy xong thì DB về đúng trạng thái dữ liệu mẫu (số sản phẩm, tồn kho, không còn đơn cũ) | A |
| TC-INV-07 | FR-INV-01, BR-INV-01 | Bảng tồn hiển thị đúng on hand, đang giữ, tồn an toàn, ATS; ATS không bao giờ âm; lọc "sắp hết", "hết hàng" | A, H |
| TC-INV-08 | FR-INV-04, BR-INV-06 | Sổ cái theo SKU đủ loại, số lượng có dấu, lý do, người thực hiện, thời điểm; loại biến động sai dấu bị `CHECK` từ chối | A |
| TC-ORD-12 | FR-ORD-01 | Mã đơn đúng dạng `CAD-YYMMDD-XXXX`. Phần số ngẫu nhiên chỉ có 9.000 giá trị mỗi ngày nên **có thể trùng**: giả lập trùng `UNIQUE` thì use case chạy lại **toàn bộ** với mã mới (PostgreSQL đã huỷ transaction khi vi phạm ràng buộc, nên không thử lại được ngay trong transaction đó; chạy lại an toàn vì lần đầu đã rollback hết), không báo lỗi cho khách | D, A |
| — | FR-PAY-01, BR-INV-02, 03 | Đã có trong TC-PAY-12 (giữ 15 phút rồi trả hàng), TC-CHK-05, TC-ORD-08 (xuất kho giải phóng phần giữ) | |
| — | BR-INV-04 | TC-CON-01, TC-CON-02 và bất biến 2 của `InvariantChecker` | |
| — | BR-VCH-04 | Ba voucher mẫu được dùng trong TC-PRC-04, 07, 08; dữ liệu mẫu được nạp trong E2E | |
| TC-OPS-01 | NFR-REL-02 | Một request sinh log JSON có `requestId` trùng với header `X-Request-Id` trả về; log đặt đơn có mã đơn, không có mật khẩu hay dữ liệu thẻ | H |
| TC-OPS-02 | NFR-REL-03 | `/actuator/health` trả `UP` khi DB sống, `DOWN` khi DB mất kết nối; metrics không truy cập được từ ngoài | H, E |
| TC-OPS-03 | NFR-REL-04 | Mọi test tích hợp chạy migration Flyway thật trên DB trống; Hibernate `validate` khớp schema | A |
| TC-OPS-04 | NFR-MNT-03 | Stack E2E trong CI được dựng bằng đúng `docker compose up` như ở máy local, có dữ liệu mẫu | CI |
| TC-OPS-05 | NFR-MNT-04 | OpenAPI sinh được trong CI; kiểu TypeScript sinh từ OpenAPI biên dịch được với frontend (lệch hợp đồng thì CI đỏ) | CI |
| TC-OPS-06 | NFR-REL-01 | Mọi tác vụ nền theo khuôn 5.5 (chạy lần hai không đổi gì); TC-NTF-03 cho trường hợp app sập giữa chừng | S, A |
| TC-OPS-07 | NFR-SEC-06 | Demo: HTTP chuyển sang HTTPS; có header HSTS | E (smoke) |
| — | NFR-MNT-01 | Chính kế hoạch này | |

---

## 5. Thiết kế chi tiết các test khó

### 5.1 Đồng thời (NFR-04…07)

#### 5.1.1 Cách dựng

```java
@Test
@DisplayName("TC-CON-01 200 người tranh 5 đôi: đúng 5 đơn thành công")
void placeOrder_neverOversellsUnderContention() throws Exception {
    Variant tempo42 = given.variantWithStock(5);
    List<CartId> carts = given.carts(200, tempo42, 1);          // 200 giỏ, mỗi giỏ 1 đôi

    CountDownLatch startGate = new CountDownLatch(1);
    try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
        List<Future<Result>> results = carts.stream()
                .map(cart -> pool.submit(() -> {
                    startGate.await();                          // mọi luồng chờ ở vạch xuất phát
                    return placeOrder(cart);
                }))
                .toList();
        startGate.countDown();                                  // xuất phát cùng lúc

        assertThat(count(results, SUCCESS)).isEqualTo(5);
        assertThat(count(results, OUT_OF_STOCK)).isEqualTo(195);
        assertThat(count(results, OTHER_ERROR)).isZero();       // không deadlock, không lỗi lạ
    }
    assertThat(inventory.ats(tempo42)).isZero();
    invariants.assertAllHold();                                 // 5.1.4
}
```

- **Vạch xuất phát (`CountDownLatch`):** nếu không có nó, luồng đầu có thể chạy xong trước khi luồng cuối được tạo, và test không còn kiểm được tranh chấp thật.
- **Connection pool của test để ít nhất 20 kết nối**, để nhiều transaction thật sự chạy song song trong DB, không phải xếp hàng ở pool.
- Kiểm bằng **con số** (T6), cộng thêm các bất biến DB.
- **Kiểm chứng test có tác dụng:** tạm thay lệnh ghi có điều kiện bằng "đọc rồi ghi" thì test này phải **đỏ**. Làm một lần khi viết test, để chắc test bắt được lỗi nó được viết ra để bắt.

#### 5.1.2 Danh sách test đồng thời

| Mã | Kịch bản | Phải đúng |
|---|---|---|
| TC-CON-01 | ATS = 5; 200 đơn, mỗi đơn 1 cái | Đúng 5 thành công, 195 `INVENTORY_OUT_OF_STOCK`, ATS = 0 |
| TC-CON-02 | 2 SKU A, B mỗi cái ATS = 100; 50 đơn giỏ `[A, B]` và 50 đơn giỏ `[B, A]` chạy cùng lúc | 100 đơn thành công, **0 lỗi deadlock** (nhờ giữ theo thứ tự `variant_id`) |
| TC-CON-03 | Cùng một IPN thành công gửi 10 lần đồng thời | Trạng thái đổi đúng 1 lần, 1 `order_events` "Đã thanh toán", 1 dòng outbox; 1 lần trả `00`, 9 lần trả `02` |
| TC-CON-04 | Voucher giới hạn 10 lượt; 60 đơn dùng voucher cùng lúc, đủ hàng | Đúng 10 đơn có voucher; `used_count` = 10; 50 đơn còn lại nhận `ORDERING_VOUCHER_UNAVAILABLE` |
| TC-CON-05 | Cùng idempotency key gửi 10 lần đồng thời | Đúng 1 đơn; cả 10 response trả cùng mã đơn |
| TC-CON-06 | Khách huỷ đơn đúng lúc tác vụ hết hạn thanh toán chạy trên cùng đơn | Đơn `CANCELLED` một lần; `reserved` giảm đúng một lần; lượt voucher trả đúng một lần |
| TC-CART-06, TC-ORD-11, TC-INV-06, TC-NTF-04 | Như mục 4 | Như mục 4 |

#### 5.1.3 Vì sao chạy trên PostgreSQL thật, trong CI

Các test này kiểm **cơ chế khoá của PostgreSQL**. Giả lập bằng mock hay H2 sẽ cho kết quả xanh mà không chứng minh được gì. Chúng chạy trong vài giây, nên được chạy ở mọi PR như test bình thường.

#### 5.1.4 Bất biến DB (`InvariantChecker`)

Chạy sau mọi test đồng thời, test tác vụ nền và test E2E:

```sql
-- 1. Sổ cái khớp tồn kho (BR-INV-05): phải trả về 0 dòng
SELECT i.variant_id FROM inventory.inventory_items i
LEFT JOIN inventory.inventory_movements m ON m.variant_id = i.variant_id
GROUP BY i.variant_id, i.on_hand
HAVING i.on_hand <> COALESCE(SUM(m.quantity), 0);

-- 2. Con số reserved khớp các dòng đang giữ: phải trả về 0 dòng
SELECT i.variant_id FROM inventory.inventory_items i
LEFT JOIN inventory.stock_reservations r ON r.variant_id = i.variant_id AND r.status = 'RESERVED'
GROUP BY i.variant_id, i.reserved
HAVING i.reserved <> COALESCE(SUM(r.quantity), 0);

-- 3. Tổng đơn khớp các dòng: phải trả về 0 dòng
SELECT o.id FROM ordering.orders o JOIN ordering.order_lines l ON l.order_id = o.id
GROUP BY o.id, o.subtotal, o.discount
HAVING o.subtotal <> SUM(l.line_total) OR o.discount <> SUM(l.allocated_discount);

-- 4. Lượt dùng voucher khớp số lần dùng chưa trả: phải trả về 0 dòng
SELECT v.id FROM ordering.vouchers v
LEFT JOIN ordering.voucher_redemptions r ON r.voucher_id = v.id AND r.released_at IS NULL
GROUP BY v.id, v.used_count
HAVING v.used_count <> COUNT(r.id);

-- 5. Cờ available khớp ATS
--    (so sánh catalog.variants.available với ATS > 0, đọc từ hai schema; chỉ dùng trong test)
```

Truy vấn 5 đọc chéo hai schema, điều mà code ứng dụng bị cấm (P2 trong tài liệu database). Ở đây được phép vì đây là **test kiểm tra**, không phải code sản phẩm.

### 5.2 Tính tiền (BR-PRC, BR-VCH)

Test kiểu bảng (`@ParameterizedTest`), giá lấy từ dữ liệu mẫu của prototype. Mọi con số dưới đây đã được tính tay; test phải ra đúng **từng đồng**.

| Mã | Giỏ | Tình huống | Tạm tính | Giảm | Phí ship | Giảm ship | **Tổng** |
|---|---|---|---:|---:|---:|---:|---:|
| TC-PRC-01 | Daily Run Tee 690.000 | Khách vãng lai, Tiêu chuẩn | 690.000 | 0 | 30.000 | 0 | **720.000** |
| TC-PRC-02 | Như trên | **Hội viên** đăng nhập (BR-PRC-07) | 690.000 | 0 | 30.000 | 30.000 | **690.000** |
| TC-PRC-03 | Như trên | Hội viên, chọn **Nhanh** | 690.000 | 0 | 45.000 | 0 | **735.000** |
| TC-PRC-04 | Tempo 2 3.290.000 + Daily Run Tee 690.000 | `CADENCE10`, Tiêu chuẩn | 3.980.000 | 200.000 (10% = 398.000, chặn ở mức tối đa) | 30.000 | 30.000 (≥ 1.500.000) | **3.780.000** |
| TC-PRC-05 | Như TC-PRC-04 | Phân bổ 200.000 xuống 2 dòng (BR-PRC-03) | | Tempo 2: **165.327**; Tee: **34.673** (tổng 200.000) | | | |
| TC-PRC-06 | Như TC-PRC-04 | VAT hiển thị (BR-PRC-05) | | | | | VAT = **343.636** |
| TC-PRC-07 | Canvas Low 1.290.000 + Daily Run Tee 690.000 | `RUN150` | 1.980.000 | Lỗi: cần mua thêm **20.000 ₫** | | | |
| TC-PRC-08 | Như TC-PRC-04 | `FREESHIP`, chọn **Nhanh**: giảm tối đa bằng phí Tiêu chuẩn (BR-VCH-01) | 3.980.000 | 0 | 45.000 | 30.000 | **3.995.000** |
| TC-PRC-09 | Tạm tính đúng **1.500.000** | Ranh giới miễn phí ship | 1.500.000 | 0 | 30.000 | 30.000 | **1.500.000** |
| TC-PRC-10 | Tạm tính 1.234.567 (dữ liệu giả) | `CADENCE10`: làm tròn xuống (BR-VCH-03) | 1.234.567 | **123.456** | | | |
| TC-PRC-11 | Daily Run Tee 690.000 | `FREESHIP`, chọn **Hoả tốc** (đơn VNPay, TP. HCM) | 690.000 | 0 | 60.000 | 30.000 | **720.000** |

Cách tính phân bổ ở TC-PRC-05 (để người đọc kiểm lại):
- Tỷ lệ thô: Tempo 2 = 200.000 × 3.290.000 / 3.980.000 = 165.326,63…; Tee = 34.673,36…
- Làm tròn xuống: 165.326 + 34.673 = 199.999, còn dư **1** đồng.
- Đồng dư chia cho dòng có phần lẻ lớn nhất (0,63 > 0,36), nên Tempo 2 thành 165.327.

Ngoài bảng: thêm một test **ngẫu nhiên có kiểm soát** (property-based, chạy 1.000 giỏ ngẫu nhiên). Với mọi giỏ: tổng phân bổ = giảm giá; không dòng nào nhận âm; `total` đúng công thức BR-PRC-02.

### 5.3 IPN (FR-PAY-03…06)

Test HTTP gọi thẳng endpoint IPN bằng query string do `VnpaySigner` ký.

| Mã | Tình huống | Phải trả cho VNPay | Dữ liệu phải ra sao |
|---|---|---|---|
| TC-PAY-01 | Hợp lệ, thành công, đúng số tiền | `00` | Giao dịch `PAID`, đơn `PLACED` + `PAID`, có sự kiện và outbox |
| TC-PAY-02 | Sai chữ ký | `97` | Không đổi gì; IPN thô vẫn được lưu với `signature_valid = false` |
| TC-PAY-03 | `txn_ref` không tồn tại | `01` | Không đổi gì |
| TC-PAY-04 | Số tiền lệch 1 đồng | `04` | Không đổi gì; có log cảnh báo |
| TC-PAY-05 | IPN báo thất bại | `00` (đã nhận) | Giao dịch `FAILED`, đơn vẫn `PENDING_PAYMENT`, khách còn thanh toán lại được |
| TC-PAY-06 | IPN lặp (tuần tự) | `02` | Không đổi gì thêm, không email thứ hai |
| TC-PAY-07 | IPN thành công tới **sau** khi đơn hết hạn và đã huỷ | `00` | Giao dịch `PAID`; đơn vẫn `CANCELLED`, thanh toán `REFUND_PENDING`; có yêu cầu hoàn tiền; **không** giữ lại hàng |
| TC-PAY-08 | Xử lý đơn lỗi giữa chừng (giả lỗi DB) | `99` | IPN thô **vẫn còn** (transaction riêng, database 5.5); lần IPN sau xử lý bình thường |
| TC-PAY-09 | Return URL báo thành công nhưng chưa có IPN | — | Đơn **không** chuyển `PAID` (FR-PAY-03) |

### 5.4 Máy trạng thái đơn hàng

Một test tham số hoá duyệt **mọi tổ hợp** (trạng thái hiện tại × hành động):

```java
@ParameterizedTest(name = "{0} + {1} → {2}")
@MethodSource("allStatesAndActions")
void transition_followsTheTable(OrderState from, OrderAction action, Expected expected) { … }
```

- Nguồn dữ liệu là **bảng chuyển trạng thái viết lại trong test** (chép từ SRS mục 6), không đọc từ code sản phẩm. Nếu đọc từ code thì code sai, test cũng sai theo mà vẫn xanh.
- Kiểm cả hai chiều: bước hợp lệ ra đúng trạng thái mới; bước không hợp lệ ném `ORDERING_INVALID_TRANSITION` và **không** đổi gì.
- Các ràng buộc giữa trục (SRS 6.4) có test riêng ở cả domain (TC-ORD-01) và DB (TC-ORD-02).

### 5.5 Tác vụ nền

Mọi tác vụ test bằng `MutableClock`, theo cùng khuôn:

1. Tạo dữ liệu ở thời điểm T.
2. Tua tới **ngay trước** mốc → chạy tác vụ → không có gì đổi.
3. Tua tới **đúng** mốc → chạy tác vụ → đổi đúng.
4. Chạy tác vụ **lần nữa** → không đổi gì thêm (chạy lại an toàn).
5. `InvariantChecker`.

| Tác vụ | Mốc |
|---|---|
| Hết hạn thanh toán | 15 phút (TC-PAY-12, 13) |
| Huỷ COD chưa xác nhận | 48 giờ (TC-ORD-07) |
| Hoàn tất đơn | 7 ngày (TC-ORD-06) |
| Gửi email | Lease 5 phút, chờ tăng dần (TC-NTF-02, 03) |
| Đối soát hằng đêm | 48 giờ trở lại (TC-PAY-15) |

### 5.6 Bảo mật

Ngoài các test ở mục 4.8:

| Việc | Công cụ | Khi nào |
|---|---|---|
| Quét thư viện có lỗ hổng đã biết | Dependabot (Maven, npm) | Liên tục |
| Quét lộ khoá bí mật trong repo | gitleaks | Mỗi PR |
| Header bảo mật (HSTS, `X-Content-Type-Options`…) có mặt | Playwright đọc response header | Hằng đêm |
| Quét bảo mật tự động mức cơ bản trên môi trường demo | OWASP ZAP baseline | Trước mỗi lần phát hành (mức **C**) |

---

## 6. Dữ liệu test

| Loại test | Dữ liệu |
|---|---|
| Domain, service | Tạo trong test bằng builder; không có DB |
| Adapter, HTTP, đồng thời | Mỗi test tạo dữ liệu riêng bằng builder, ghi qua repository thật; xoá sạch các bảng nghiệp vụ trước mỗi lớp test |
| E2E | Dữ liệu mẫu từ `db/seed/dev` (40 sản phẩm từ prototype, 3 voucher, 2 nhân viên) dựng lại mỗi lượt chạy. Mỗi test dùng sản phẩm riêng để không giành tồn kho của nhau |
| Hiệu năng | Script sinh **1.000 sản phẩm / 10.000 SKU** (đúng quy mô NFR-01) và đủ tồn kho cho toàn bộ lượt đặt hàng |

Tài khoản và khoá dùng trong test là giá trị riêng cho test, nằm trong file cấu hình test, không phải khoá sandbox thật.

---

## 7. Hiệu năng (NFR-01, NFR-02)

| Kịch bản k6 | Tải | Ngưỡng đạt (`thresholds`) |
|---|---|---|
| Duyệt và lọc PLP, xem PDP, tìm kiếm | 50 người dùng ảo trong 5 phút | p95 < 300 ms; lỗi < 0,1 % |
| Thêm vào giỏ → đặt hàng COD | 50 người dùng ảo trong 5 phút | p95 đặt hàng < 800 ms; lỗi < 0,1 % |
| Tranh hàng (kiểm tra thêm) | 200 người dùng ảo đặt cùng 1 SKU còn 20 cái | Đúng 20 đơn; `InvariantChecker` xanh |

- Chạy trên **máy cấu hình giống VPS demo**, không chạy trên runner chung của GitHub (kết quả dao động quá nhiều).
- Kết quả (p95, số request/giây) ghi vào README kèm ngày chạy và cấu hình máy.

---

## 8. Môi trường và CI

### 8.1 Môi trường

| Môi trường | Dùng cho |
|---|---|
| Máy local | Domain, service: chạy liên tục trong IDE. Tích hợp: chạy trước khi đẩy code (cần Docker) |
| GitHub Actions | Mọi test trên mỗi PR (mục 8.2) |
| Stack Docker Compose trong CI | E2E: Nginx + Next.js + Spring Boot + PostgreSQL + Mailpit + cổng thanh toán giả lập |
| Máy hiệu năng | k6 |
| Môi trường demo | Smoke test sau mỗi lần triển khai; thử tay với VNPay sandbox thật |

### 8.2 Pipeline

| Giai đoạn | Mỗi PR | Hằng đêm | Trước phát hành |
|---|:-:|:-:|:-:|
| Lint, format, `tsc` | ✓ | | |
| Domain + service | ✓ | | |
| Adapter, HTTP, đồng thời, kiến trúc (Testcontainers) | ✓ | | |
| gitleaks | ✓ | | |
| Build image | ✓ | | |
| E2E rút gọn (mục 9.1, kịch bản 1–3) | ✓ | | |
| E2E đầy đủ + SEO + axe + header bảo mật | | ✓ | ✓ |
| Lighthouse CI | | ✓ | ✓ |
| k6 | | | ✓ |
| ZAP baseline | | | ✓ |
| Smoke test trên demo | | | ✓ (sau triển khai) |

PR chỉ được merge khi mọi giai đoạn "Mỗi PR" xanh (NFR-MNT-02).

---

## 9. E2E (Playwright)

### 9.1 Kịch bản

Chỉ những luồng mà test ở tầng dưới **không** thay thế được (cần trình duyệt thật, nhiều thành phần cùng chạy):

| # | Kịch bản | Kiểm thêm |
|---|---|---|
| 1 | Tìm "giay chay" → chọn size → giỏ → áp `CADENCE10` → COD → trang kết quả | Email xác nhận có trong Mailpit; số tiền khớp TC-PRC-04 |
| 2 | Đặt VNPay → cổng giả lập trả thành công → trang kết quả chuyển "Thanh toán thành công" | FR-PAY-08 |
| 3 | Admin đăng nhập → xác nhận đơn COD → đóng gói → giao (nhập mã vận đơn) → đã giao | Trang tồn kho: `on_hand` giảm đúng |
| 4 | Hai trình duyệt riêng (hai `BrowserContext`) cùng đặt đôi cuối cùng | Một bên thành công; bên kia thấy thông báo hết hàng và "chưa có khoản tiền nào bị trừ" |
| 5 | Đặt VNPay → cổng giả lập báo thất bại → thanh toán lại → thành công | FR-PAY-07 |
| 6 | Khách tra cứu đơn không đăng nhập → huỷ đơn | FR-ORD-02, 04 |
| 7 | Khách vãng lai thêm giỏ → đăng nhập → giỏ được gộp | FR-CART-03 |
| 8 | Toàn bộ kịch bản 1 ở màn hình 360 px và chỉ dùng bàn phím | NFR-UX-01, 02 |
| 9 | Các kiểm tra SEO (TC-SEO-01…05) | |

### 9.2 Quy tắc chống chập chờn

- Chờ theo **trạng thái** (`expect(locator).toBeVisible()`), không bao giờ `waitForTimeout(…)`.
- Tìm phần tử theo vai trò và nhãn (`getByRole('button', { name: 'Đặt hàng' })`), giống cách người dùng nhìn trang. Cách này cũng gián tiếp kiểm khả năng truy cập.
- Mỗi kịch bản dùng sản phẩm và tài khoản riêng.
- Thất bại thì lưu trace, ảnh chụp màn hình và video vào artifact của CI.

---

## 10. Tiêu chí hoàn thành và xử lý lỗi

### 10.1 Definition of Done cho một use case

1. Có mã yêu cầu trong SRS và test case tương ứng ở mục 4.
2. Test domain viết **trước** hoặc cùng lúc với code domain.
3. Có test adapter trên PostgreSQL thật nếu use case đụng tới SQL mới.
4. Có test HTTP cho status, mã lỗi, phân quyền.
5. Nếu use case có tranh chấp: có test đồng thời và `InvariantChecker`.
6. Mọi giai đoạn "Mỗi PR" xanh.
7. Bảng ở mục 4 được cập nhật nếu thêm test case mới.

### 10.2 Tiêu chí phát hành MVP

| Tiêu chí | Ngưỡng |
|---|---|
| Yêu cầu mức M có test | 100 % (theo bảng mục 4) |
| Test mỗi PR | 100 % xanh, không có test bị cách ly quá 24 giờ |
| E2E đầy đủ | 100 % xanh trên 3 lượt chạy đêm liên tiếp |
| NFR-01, 02, 03 | Đạt ngưỡng ở mục 7 và TC-PERF-03 |
| Lỗi mở | Không có lỗi S1, S2 |

### 10.3 Mức độ lỗi

| Mức | Định nghĩa | Ví dụ | Xử lý |
|---|---|---|---|
| **S1** | Sai tiền, sai tồn kho, lỗ hổng bảo mật | Bán lố; tổng tiền lệch; xem được đơn người khác | Dừng mọi việc khác, sửa ngay, thêm test tái hiện lỗi |
| **S2** | Luồng chính không dùng được, không có cách khác | Không đặt được hàng VNPay | Sửa trước khi làm tính năng mới |
| **S3** | Có lỗi nhưng có cách khác | Lọc theo màu sai, vẫn tìm được bằng ô tìm kiếm | Đưa vào sprint kế tiếp |
| **S4** | Giao diện, chính tả | Lệch căn lề | Gom sửa định kỳ |

**Mọi lỗi được sửa đều đi kèm một test tái hiện lỗi đó** (đỏ trước khi sửa, xanh sau khi sửa), để lỗi không quay lại.

---

## 11. Rủi ro của việc kiểm thử

| Rủi ro | Cách giảm |
|---|---|
| Test đồng thời xanh do may mắn chứ không phải do code đúng | Kiểm chứng một lần bằng cách cố tình làm hỏng code (mục 5.1.1); dùng vạch xuất phát và đủ kết nối DB |
| Testcontainers làm CI chậm | Một container dùng chung cho cả lượt chạy; dọn bảng thay vì dựng lại DB |
| VNPay sandbox khác cổng giả lập | Thử tay trên môi trường demo với sandbox thật trước mỗi lần phát hành |
| E2E chập chờn | Quy tắc mục 9.2; cách ly trong 24 giờ (T7) |
| Bảng đối chiếu ở mục 4 lỗi thời so với code | Có trong Definition of Done (10.1, bước 7) |
