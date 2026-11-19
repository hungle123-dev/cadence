#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<EOSQL
  -- Thủ kho: chỉ Flyway dùng, được tạo schema (xây phòng)
  CREATE ROLE cadence_migrator LOGIN PASSWORD '$CADENCE_MIGRATOR_PASSWORD';
  GRANT CONNECT, CREATE ON DATABASE $POSTGRES_DB TO cadence_migrator;

  -- Nhân viên: ứng dụng dùng, lúc này chỉ được vào database
  CREATE ROLE cadence_app LOGIN PASSWORD '$CADENCE_APP_PASSWORD';
  GRANT CONNECT ON DATABASE $POSTGRES_DB TO cadence_app;
EOSQL