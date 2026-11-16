# LABIX API (Spring Boot)

Java 21, Spring Boot. Serves web, mobile and super admin. Read ../CLAUDE.md, ../docs/API.md, ../docs/Schema.md and ../docs/rules.md first.

## Commands

- Run: `mvn spring-boot:run` (needs `.env` sourced or env vars set)
- Test: `mvn test` (uses H2 in-memory, no DB needed)
- DB: `docker compose up -d` from repo root (starts MySQL on 3306)
- Env: copy `../.env.example` to `../.env` and fill in secrets

## Structure

`controller` (HTTP only) > `service` (business rules) > `repository` (tenant-scoped data access). Use DTOs, never return entities.

## Rules

- Tenant id comes from the JWT only. Filter every tenant query by it.
- Check plan, role and record ownership on every endpoint.
- Money in integer paise. Soft delete for patients, bills, visits, reports.
- Released reports are immutable; corrections create a new version plus an audit entry.
- Schema changes only through migrations.
- Do not add endpoints that are not in ../docs/API.md. Update the doc first.
- Server computes flags (low, high, critical); range edits apply to one report only.