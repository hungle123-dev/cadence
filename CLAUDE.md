# CLAUDE.md — Cadence

Website bán giày và đồ thể thao, dự án portfolio. Backend Java 25 + Spring Boot 4.2 (modular monolith, DDD + Hexagonal + Clean Architecture), frontend Next.js 16, PostgreSQL 17, Nginx, Docker, GitHub Actions.

## Tài liệu là nguồn sự thật

Đọc trước khi làm, theo thứ tự: `docs/01-srs.md` (yêu cầu) → `docs/02-architecture.md` → `docs/adr/` → `docs/03-database.md` → `docs/04-test-plan.md`.

- Code khác tài liệu thì **hỏi trước**, không tự quyết. Đổi yêu cầu thì sửa SRS trước (thêm quyết định `D-xx` ở mục 10, ghi lịch sử thay đổi), rồi mới sửa code.
- Mỗi lần thay đổi thiết kế, cập nhật tài liệu liên quan trong **cùng** nhánh với code.
- Prototype chỉ là bản phác (SRS A-05), không phải bản cuối.
- Tìm kiếm sản phẩm (FR-CAT-08) đang **tạm hoãn** (SRS D-61): sẽ dùng Elasticsearch. Không làm full-text trên PostgreSQL.

## Cách làm một use case

Theo thứ tự, không viết hết code rồi mới viết test:

1. Đọc yêu cầu và test case tương ứng (`TC-…` trong test plan).
2. Test domain → code domain.
3. Query/Command, View, port, service + test service.
4. Adapter + test trên PostgreSQL thật.
5. Controller + test HTTP.
6. Cập nhật tài liệu nếu thực tế khác thiết kế.

Mỗi test liên quan tới yêu cầu có `@DisplayName("TC-… <mô tả tiếng Việt>")`.

## Backend: cấu trúc và quy ước

Module (mỗi module một schema PostgreSQL): `account`, `catalog`, `inventory`, `ordering`, `payment`, `notification`. Dùng chung: `common`.

```
com.cadence.<module>
├── domain/          model/ (aggregate là record bất biến), valueobject/, exception/
├── application/     dto/ (*Query, *View), port/driving/ (*UseCase), port/driven/ (*RepositoryPort, *ReadPort), service/
└── infrastructure/  adapter/, persistence/entity|repository, mapper/ (MapStruct), restapi/ + restapi/uidto/, config/
```

- **Domain** chỉ dùng JDK, `common.domain` và domain của chính module (ArchUnit). Không Spring, JPA, jspecify. Lombok được phép.
- **Luật nghiệp vụ nằm trong aggregate hoặc value object**, có tên rõ (`visibilityAt(now)`, `priceAt(now)`, `isInStock()`), không nằm trong service hay SQL.
- **`*RepositoryPort`** trả aggregate ở mọi trạng thái; aggregate tự quyết định có hiển thị hay không. **`*ReadPort`** dùng SQL (`JdbcClient`) cho danh sách, lọc, phân trang; luật nghiệp vụ chép sang SQL được gom vào một class (`CatalogSql`) và có test so kết quả SQL với domain.
- **Query record** tự kiểm tra trong constructor, gom mọi lỗi thành danh sách `InvalidParam` rồi ném exception của module (ví dụ `CATALOG_INVALID_FILTER`).
- **Hai tầng kiểm tra request**: sai kiểu ở `*Request` (`@Pattern`, `@ValidEnum`, số nhận dưới dạng chuỗi) → `VALIDATION_ERROR`; sai giá trị ở Query/service → mã của module.
- **Service** lấy `now` một lần từ `Clock`; đọc phân trang dùng `REPEATABLE_READ`.
- **JPA entity** chỉ đọc khi chưa có use case ghi: `@Getter`, constructor protected, collection trả bản không sửa được.
- **REST**: `*Request` với `@ParameterObject @Valid` và `toQuery()`; `*Response` có `@Schema` và `static from(view)`; bọc trong `UIApiResponse` / `UIPageResponse`; tên bean controller có tiền tố module (`@RestController("catalogProductController")`).
- **Lỗi** trả Problem Details có `code` và `requestId` (`GlobalExceptionHandler`); exception kế thừa đúng loại (`ValidationException`, `ResourceNotFoundException`…).
- **Tiền** là `long` (VND); **thời gian** lưu UTC, giờ Việt Nam chỉ qua `BusinessTime`.
- **Ảnh** lưu khoá ảnh, không lưu URL (BR-CAT-01).

## Database

- Migration: `backend/src/main/resources/db/migration/<module>/V{yyyyMMdd}_{n}__<module>_<việc>.sql`; đã chạy thì không sửa, muốn đổi thì thêm file mới. Migration của module chỉ chạm schema của module đó (`MigrationSchemaTest`).
- Hai role: `cadence_owner` chạy Flyway, `cadence_app` chỉ có quyền đọc ghi dữ liệu. Migration tạo schema phải `GRANT USAGE ON SCHEMA <module> TO cadence_app`.
- Dữ liệu mẫu: `db/seed/dev/R__seed_<module>.sql`, chỉ nạp ở profile `dev` và `demo`; test tích hợp nạp cùng file bằng `@Sql`.
- SQL viết trong Java chỉ được nhắc schema của chính module (`NativeSqlSchemaTest`).

## Test

- Test tích hợp kế thừa `PostgresIT` (PostgreSQL thật qua Testcontainers, chạy `deploy/postgres/init.sh` nên dùng đúng hai role). Không dùng H2.
- Test của module có lớp cha riêng (ví dụ `CatalogIT`: seed + fixture, `Clock` cố định, `@Transactional`) để mọi test của module dùng chung một context.
- Cần Docker đang chạy.

## Lệnh

Chạy trong `backend/`:

```
./mvnw spotless:apply      # format code (google-java-format, AOSP)
./mvnw test                # chạy test
./mvnw verify              # như CI: test + Spotless + Checkstyle + SpotBugs + JaCoCo (≥ 90 % dòng)
```

`verify` phải xanh trước khi commit. Không thêm ngoại lệ vào cấu hình Checkstyle/SpotBugs để né lỗi: sửa code.

Chạy local: `docker compose up db mail` ở gốc repo (cổng trong `.env`, mẫu ở `.env.example`), rồi chạy backend với profile `dev`.

## Git

- Author là `hungle123-dev`. **Không** thêm dòng `Co-Authored-By` hay ghi công AI vào commit hoặc PR.
- Conventional Commits, gọn: `feat(catalog): …`, `fix(…)`, `docs: …`, `refactor(…)`.
- Làm trên nhánh, không commit thẳng `main`. Không push hay merge khi chưa được yêu cầu; hỏi trước khi viết lại lịch sử đã push.
- Các file trong `.gitignore` (ghi chú cá nhân, kế hoạch milestone, template cá nhân, prototype) chỉ giữ ở máy: không commit, không nhắc tới từ tài liệu đã commit.
