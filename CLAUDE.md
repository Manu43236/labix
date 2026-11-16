# LABIX

Cloud lab management SaaS for small and medium diagnostic/pathology labs in India (pilot region: Vizag, Vizianagaram, Srikakulam). Multi-tenant. Web + mobile, both backed by one API.

## Monorepo layout

```
labix/
├── CLAUDE.md      (this file)
├── docs/          (all project docs, source of truth)
├── api/           Spring Boot (Java 21)
├── web/           React
└── mobile/        Flutter
```

Each of `api/`, `web/`, `mobile/` has its own CLAUDE.md with commands and conventions. Read it before working in that folder.

## Stack

- API: Java 21, Spring Boot
- Web: React
- Mobile: Flutter
- Cloud: AWS
- Database: MySQL 8

## Product summary

- Users: super admin (us), lab admin, technician, pathologist, receptionist, sample collector.
- Core: patient, test, billing, sample collection, report entry, e-signature, PDF reports (shareable via WhatsApp from mobile), dashboard, referral doctors, roles and permissions.
- Mobile does everything web does. Access is gated by the tenant's subscription plan.
- Plans (monthly, excl. GST): Web only 1200 | Web + mobile (sample collection only) 1500 | Web + mobile (full access) 2000.
- Reference ranges: standard defaults by age and gender, editable per report (the edit applies to that report only).
- GST is configured per tenant.
- Not in v1: automated WhatsApp/SMS, analyser integration, patient portal, offline mode, data migration.

## Docs (read before building the related part)

- docs/PRD.md: goals, users, features, plans
- docs/TRD.md, docs/Architecture.md: technical decisions and system design
- docs/Schema.md: database tables
- docs/API.md: endpoint contract shared by web and mobile
- docs/plans-permissions.md: plans x features, roles x actions
- docs/report-template-spec.md: report layout, fields, PDF rules
- docs/seed-data.md: default tests and reference ranges
- docs/appflow.md, docs/design.md: flows and UI guidelines
- docs/rules.md: coding standards
- docs/phases.md, docs/implementation.md, docs/Tasks.md: build order and progress
- docs/Testing.md, docs/Deployment.md
- docs/memory.md: decisions and context across sessions

## Working rules

1. Business logic lives in the API only. Web and mobile stay thin.
2. Plan and role checks are enforced in the API, never only in the UI.
3. Every tenant-owned table carries a tenant id. Never return another tenant's data.
4. API contract changes go in docs/API.md first, then API, then web and mobile.
5. Work only in the folder named in the task. Do not edit other parts unless asked.
6. Do not invent features, endpoints or tables that are not in the docs. Ask first.
7. Report accuracy matters most: reference ranges, units and abnormal flags must be correct and covered by tests.
8. Update docs/Tasks.md after finishing a task, and docs/memory.md after any decision.
9. Commit small, one working feature per commit.

## Brand

Name: LABIX (always uppercase). Colours from the logo: navy, teal, green. Logo at docs/assets/labix_logo.png.