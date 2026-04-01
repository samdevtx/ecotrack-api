#!/bin/bash
# Database initialisation script — runs inside the Oracle XE container on first start.
# The gvenzl/oracle-xe image creates APP_USER automatically via the APP_USER /
# APP_USER_PASSWORD env vars. This script only handles extra privileges and the
# tablespace that the image doesn't create on its own.

set -euo pipefail

sqlplus -s "SYSTEM/${ORACLE_PASSWORD}@//localhost:1521/XEPDB1" <<SQL
-- Tablespace for the application
CREATE TABLESPACE ECOTRACK_DATA
DATAFILE '/opt/oracle/oradata/XE/XEPDB1/ecotrack_data01.dbf'
SIZE 100M
AUTOEXTEND ON
NEXT 10M
MAXSIZE UNLIMITED;

-- Extra privileges required by Flyway
GRANT SELECT ON DBA_OBJECTS TO ecotrack;
GRANT SELECT ON DBA_TAB_COLUMNS TO ecotrack;

ALTER USER ecotrack DEFAULT TABLESPACE ECOTRACK_DATA;

EXIT;
SQL

echo "Database initialisation complete."
