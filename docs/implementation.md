# LABIX: Implementation Order

Version 0.1 (draft). Follow this order. Each step ends with tests passing and docs updated. Give Claude Code one step at a time.

## Step 1: Repo and tooling
- Monorepo with `api/`, `web/`, `mobile/`, `docs/`.
- Per-folder CLAUDE.md. `.gitignore` per project.
- Local run setup: MySQL via Docker, env files, migration tool.

## Step 2: API foundation
- Spring Boot project, base error format, health endpoint.
- Migrations for platform tables (plans, tenants, subscriptions, users, roles).
- JWT auth, login, refresh, tenant context filter.
- Plan and role enforcement utilities.
- Tests: login, tenant isolation, role and plan checks.

## Step 3: Super admin
- Admin login, tenants CRUD, plan assignment, suspend/reactivate.
- Create first lab admin for a tenant.
- Support session logging.

## Step 4: Catalogue and people
- Tests, parameters, ranges, packages. Seed loader for defaults.
- Patients, doctors.
- Branches, users, signature upload (S3).

## Step 5: Visits, billing, samples
- Create visit with tests, bill and samples in one transaction.
- Payments, bill status, invoice PDF.
- Sample states, labels/barcodes, reject and recollect.
- Home collection requests, assignment, status updates.

## Step 6: Results and reports
- Result entry endpoint with range selection by age and gender, flags.
- Range edit per report (no change to the catalogue).
- Submit, approve, reject workflow.
- Report snapshot and PDF generation, stored in S3.
- Audit logs.

## Step 7: Web app
Build screens against the API as each API step lands:
login, super admin, setup, catalogue, patients, billing, samples, results, approvals, reports, doctors, dashboard.

## Step 8: Mobile app
- Login and capabilities gating.
- Collector flow.
- Full-access screens mirroring web.
- Share report PDF.

## Step 9: Hardening
- Validation and error states, performance on lists, security review.
- Backups and restore test, staging deployment, production deployment.

## Step 10: Pilot
Onboard pilot labs, collect issues in Tasks.md, fix, repeat.

## Working method with Claude Code

1. Start from the repo root. Tell it the step and the folder to touch.
2. Ask it to read CLAUDE.md, the relevant docs and the folder's CLAUDE.md first.
3. Change docs (API.md, Schema.md) before code when the contract changes.
4. Review the diff, run tests, commit.
5. Update Tasks.md and memory.md.