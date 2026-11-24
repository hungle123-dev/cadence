-- Migration đầu tiên: chỉ đánh dấu điểm bắt đầu lịch sử Flyway.
-- Mỗi module thêm migration trong thư mục db/migration/<module>/ (database mục 7).
-- Role cadence_owner / cadence_app được tạo ngoài Flyway (deploy/postgres/init.sql),
-- vì Flyway chạy bằng chính cadence_owner.
SELECT 1;
