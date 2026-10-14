#!/bin/sh
# Runs once, only when the PostgreSQL data directory is empty.
# Creates the role and database that Keycloak uses.
set -e

psql -v ON_ERROR_STOP=1 -v kc_password="$KEYCLOAK_DB_PASSWORD" \
     --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'EOSQL'
CREATE ROLE keycloak LOGIN PASSWORD :'kc_password';
CREATE DATABASE keycloak OWNER keycloak;
EOSQL