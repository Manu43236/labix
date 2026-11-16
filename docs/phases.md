# LABIX: Phases

Version 0.1 (draft). Work happens in spare time, so durations are in "build sessions", not calendar weeks. Set a target date for the first pilot lab.

## Phase 0: Validation and setup
- Visit 5 labs, record how they work and what they pay.
- Get 3 to 5 labs to agree to a pilot.
- Decide owning company; register domain and trademark check for LABIX.
- Finish docs; set up the monorepo and repo CLAUDE.md files.

## Phase 1: Foundation
- API skeleton, auth, multi-tenancy, roles, plans, migrations.
- Web shell (login, layout, role-based menu).
- Super admin: tenants, plans, subscriptions.

## Phase 2: Core lab workflow (web)
- Tenant profile, users, branches.
- Test catalogue, ranges, packages (with seed data).
- Patients, doctors.
- Visits, billing, payments.
- Samples and labels.

## Phase 3: Results and reports
- Result entry with ranges and flags.
- Approval workflow and e-signature.
- Report templates and PDF generation.
- Audit log.

## Phase 4: Mobile
- Flutter app: login, capabilities, plan and role gating.
- Collector workflow first (assigned list, collect, status updates).
- Then full-access features (mirror web).
- Report PDF sharing from mobile.

## Phase 5: Dashboard and polish
- Dashboards, doctor summaries, due payments.
- Error handling, loading states, validation.
- Report layout polish with real lab feedback.

## Phase 6: Pilot
- Deploy to staging, then production on AWS with backups.
- Onboard 3 to 5 pilot labs, train them, collect feedback.
- Fix issues, then roll out to the rest.

## Exit criteria for each phase

- Features work end to end on the real API.
- Tests pass (see Testing.md).
- Docs updated (Tasks.md, API.md, Schema.md as needed).

## After v1

Automated WhatsApp/SMS, analyser integration, patient portal, offline mode, data migration, Telugu, inventory, multi-branch improvements.