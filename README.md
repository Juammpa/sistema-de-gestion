# SistemaGestion

Backend: Spring Boot (`backend/`). Frontend: Angular (`frontend/`).
Infrastructure: PostgreSQL and Keycloak with Docker Compose.

## Daily use

```powershell
docker compose up -d
```

Starts PostgreSQL (`127.0.0.1:5433`) and Keycloak (`127.0.0.1:8081`).
Starts PostgreSQL (`127.0.0.1:5433`) and Keycloak (`127.0.0.1:8081`). Then run the backend
(see [Backend configuration](#backend-configuration)) and the frontend with `npm start`
inside `frontend/`.

`docker compose down` keeps the data. **Never run `docker compose down -v`**: it deletes
the database volume.

## Configuration

Copy `.env.example` to `.env` and fill in the values. `.env` is not versioned and is not
part of the backups: keep a copy in a password manager. Keycloak stores
`BACKEND_ADMIN_SECRET` in its database on the first import (later imports are skipped),
so after a restore the value must be the original one.

## Backend configuration

`application.yaml` reads every environment-specific value from a placeholder. For local
development, copy `backend/application-local.example.yaml` to
`backend/src/main/resources/application-local.yaml` (git-ignored) and fill in the values
marked with `<...>`. The `local` profile is active by default.

| Setting | Environment variable | Local value |
|---|---|---|
| Database URL | `DB_URL` | `jdbc:postgresql://localhost:5433/sistema_gestion` |
| Database user | `DB_USERNAME` | `POSTGRES_USER` from `.env` |
| Database password | `DB_PASSWORD` | `POSTGRES_PASSWORD` from `.env` |
| Token issuer | `KEYCLOAK_ISSUER_URI` | `http://localhost:8081/realms/sistema-gestion` |
| Keycloak base URL | `KEYCLOAK_BASE_URL` | `http://localhost:8081` |
| Admin API secret | `BACKEND_ADMIN_SECRET` | same value as in `.env` |
| Allowed CORS origins | `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` |
| Initial data owner | `INITIAL_OWNER_ID` | any valid UUID (see below) |

Outside local development, set those environment variables instead of using the local file.

`INITIAL_OWNER_ID` is only used by Flyway migration V3 (data ownership). On an empty or
restored database it has no effect, but it must be a valid UUID. When migrating a database
that already has data, use the Keycloak `sub` of the admin user (Keycloak console >
Users > Details > ID).

## Backup

```powershell
.\scripts\backup.ps1
```

Writes `backups/<database>-<timestamp>.dump` for `sistema_gestion` and `keycloak` and keeps
the last 14 of each. They live on the same disk as Docker, so copy `backups/` to an
external drive or cloud storage regularly.

## Restore (new machine or lost volume)

1. Clone the repo and recreate `.env`. `POSTGRES_USER` must be `postgres`, the owner of the
   objects inside the dumps.
2. `docker compose up -d postgres`. On an empty volume it creates the `keycloak` role and
   database (`infra/postgres/init`).
3. Copy and restore the dumps, using the timestamps of the files you have:

```powershell
docker compose cp backups\sistema_gestion-<timestamp>.dump postgres:/tmp/sg.dump
docker compose cp backups\keycloak-<timestamp>.dump postgres:/tmp/kc.dump
docker compose exec -T postgres sh -c 'pg_restore -U $POSTGRES_USER -d sistema_gestion --exit-on-error /tmp/sg.dump'
docker compose exec -T postgres sh -c 'pg_restore -U $POSTGRES_USER -d keycloak --exit-on-error /tmp/kc.dump'
docker compose up -d
```

Tested end to end: Flyway history, row counts and the Keycloak realm come back intact.