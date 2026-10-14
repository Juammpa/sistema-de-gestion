# SistemaGestion

Backend: Spring Boot (`backend/`). Frontend: Angular (`frontend/`).
Infrastructure: PostgreSQL and Keycloak with Docker Compose.

## Daily use

```powershell
docker compose up -d
```

Starts PostgreSQL (`127.0.0.1:5433`) and Keycloak (`127.0.0.1:8081`). Then run the backend
with its `local` profile and the frontend with `npm start` inside `frontend/`.

`docker compose down` keeps the data. **Never run `docker compose down -v`**: it deletes
the database volume.

## Configuration

Copy `.env.example` to `.env` and fill in the values. `.env` is not versioned and is not
part of the backups: keep a copy in a password manager. Keycloak stores
`BACKEND_ADMIN_SECRET` in its database on the first import (later imports are skipped),
so after a restore the value must be the original one.

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