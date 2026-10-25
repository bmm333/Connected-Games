#!/bin/bash
#create an user and a schema per each service
#PG executes only once on the first init, with empty vol

set -euo pipefail

for service in identity catalog match tournament stats; do
    var="${service^^}_DB_PASSWORD"
    password="${!var:?manca $var nel file .env}"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<SQL
CREATE USER ${service} WITH PASSWORD '${password}';
CREATE SCHEMA ${service} AUTHORIZATION ${service};
ALTER ROLE ${service} SET search_path = ${service};
SQL
done
