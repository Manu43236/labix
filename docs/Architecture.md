# LABIX: Architecture

Version 0.1 (draft).

## 1. System overview

```
 React web app ─┐
 Flutter app ───┼──▶  Spring Boot API  ──▶  MySQL
 Super admin ───┘          │
                           ├──▶  S3 (logos, signatures, report PDFs)
                           └──▶  PDF generator (inside API)
```

One API, three clients. Clients are thin.

## 2. Repository layout (monorepo)

```
labix/
├── CLAUDE.md
├── docs/
├── api/       Spring Boot
├── web/       React
└── mobile/    Flutter
```

## 3. API layering (suggested)

- `controller`: HTTP, validation, auth annotations only.
- `service`: business rules, plan and role checks, workflows.
- `repository`: data access, always tenant-scoped.
- `domain/entity`: JPA entities.
- `dto`: request and response objects. Never expose entities directly.
- `security`: JWT, tenant context, permission checks.
- `report`: report snapshot and PDF generation.

## 4. Multi-tenancy

- Shared schema, `tenant_id` on every tenant-owned table.
- A tenant context is set per request from the JWT and applied to every query.
- Super admin operates outside a tenant and uses separate endpoints (`/api/v1/admin/...`).
- Support access into a tenant is time-limited and logged.

## 5. Auth and authorization

1. Login returns access token, refresh token and a `capabilities` object (plan features and role permissions).
2. Every request is checked: tenant active, plan includes feature, role allows action, record belongs to the tenant.
3. Clients use `capabilities` only to decide what to show. The API is the real gate.

## 6. Key flows (see appflow.md for UI flows)

- **Visit:** patient registered, tests chosen, bill created, samples created (walk-in or home collection).
- **Home collection:** request created, collector assigned, collector marks collected, lab marks received.
- **Result:** technician enters results on received samples, pathologist approves, report snapshot created, PDF generated and stored.
- **Share:** web downloads the PDF; mobile shares it via the OS share sheet.

## 7. Report generation

1. On approval, the API builds a report snapshot (patient, tests, values, ranges used, flags, signature, letterhead) and saves it.
2. PDF is rendered from the snapshot and stored in S3.
3. Re-download always serves the stored PDF. A correction makes a new version.

## 8. Files

- Stored in S3 under `tenant-id/...` prefixes.
- Accessed through short-lived signed URLs.

## 9. Environments

`local`, `staging`, `production`. Details in Deployment.md.

## 10. Decisions log

Record architecture decisions in memory.md with a date and reason.