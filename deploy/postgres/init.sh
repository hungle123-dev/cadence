#!/bin/sh
# Runs once, when the PostgreSQL data volume is created (docker-entrypoint-initdb.d).
# Creates the two roles of database doc section 6 (least privilege):
#   cadence_owner: owns the database and every schema; Flyway migrates with it.
#   cadence_app:   what the application uses at runtime; data access only, cannot create or drop tables.
set -eu

: "${DB_OWNER_PASSWORD:?DB_OWNER_PASSWORD is required}"
: "${DB_APP_PASSWORD:?DB_APP_PASSWORD is required}"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
     -v owner_pw="$DB_OWNER_PASSWORD" -v app_pw="$DB_APP_PASSWORD" <<'SQL'
CREATE ROLE cadence_owner LOGIN PASSWORD :'owner_pw';
CREATE ROLE cadence_app   LOGIN PASSWORD :'app_pw';
CREATE DATABASE cadence OWNER cadence_owner;
SQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname cadence <<'SQL'
-- Nobody but the owner creates objects in the public schema.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
ALTER SCHEMA public OWNER TO cadence_owner;

-- Tables and sequences the owner creates later (in any schema) are usable by the app.
-- Each module migration still grants USAGE on its own schema to cadence_app.
ALTER DEFAULT PRIVILEGES FOR ROLE cadence_owner
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO cadence_app;
ALTER DEFAULT PRIVILEGES FOR ROLE cadence_owner
    GRANT USAGE, SELECT ON SEQUENCES TO cadence_app;
SQL
