# LABIX: Tasks

Live tracker. Update after every task. Format: `[ ]` open, `[x]` done.

## Now

- [ ] Decide owning company for LABIX
- [ ] Check LABIX domain, Play Store and IP India trademark
- [ ] Confirm auth choice (TRD.md)
- [ ] Confirm plan limits (plans-permissions.md)
- [ ] Get 3 to 5 labs to agree to a pilot
- [ ] Find a pathologist to review default ranges and report layout

## Phase 0: Validation and setup

- [ ] Visit 5 labs; note software used, pain points, willingness to pay
- [ ] Register domain(s)
- [x] Create the monorepo and copy docs (2026-11-13)
- [x] Create per-folder CLAUDE.md files (2026-11-13)

## Phase 1: Foundation

- [x] API skeleton and error format (2026-11-13)
- [x] Auth (login, refresh) (2026-11-13)
- [x] Multi-tenancy and tenant context (2026-11-13)
- [x] Plans and roles enforcement (2026-11-13)
- [ ] Web shell
- [x] Super admin tenants and subscriptions (2026-11-13)

## Phase 2: Catalogue and People (Step 4)

- [x] Flyway migrations: tests, parameters, reference ranges, packages, patients, doctors, branches, files (2026-11-14)
- [x] Seed service: loads default-catalogue.json on tenant creation — 21 tests, ~65 params, 4 packages; range values left blank for pathologist review (2026-11-14)
- [x] Catalogue CRUD: tests, parameters, ranges, packages; role-enforced writes (2026-11-14)
- [x] Patient CRUD: auto patientNo, soft delete (2026-11-14)
- [x] Doctor CRUD (2026-11-14)
- [x] Branch CRUD: main-branch uniqueness enforced (2026-11-14)
- [x] Lab user management: create, role assignment, status toggle, signature upload (2026-11-14)
- [x] Auth login response includes roles list (2026-11-14)
- [x] Tests: 56 passing — CatalogueControllerTest (10), PatientControllerTest (10), LabSetupControllerTest (10), plus prior tests (2026-11-14)

## Phase 3: Visits, Billing, Samples (Step 5)

- [x] Flyway migrations: visits, visit_tests, bills, bill_items, payments, samples, collection_requests (2026-11-14)
- [x] Visit creation: patient + branch + tests/packages → visit + visit_tests + bill + samples in one transaction (2026-11-14)
- [x] Bill: subtotal, discount, total, paid; status auto-calculated (DUE/PARTIAL/PAID) (2026-11-14)
- [x] Payment recording: multiple payments accumulated, bill status updated (2026-11-14)
- [x] Sample auto-creation: one per distinct sample_type across all tests in visit (2026-11-14)
- [x] Sample status transitions: COLLECTED/RECEIVED/REJECTED/RECOLLECT; reject requires reason (2026-11-14)
- [x] Home collection requests: create, assign collector, status updates (2026-11-14)
- [x] Tests: 66 passing — VisitControllerTest (10), plus all prior tests (2026-11-14)
- [ ] Invoice PDF — deferred (needs PDF library, out of scope for pilot v0.1)

## Phase 4: Results and Reports (Step 6)

- [x] Flyway migrations: results, reports, report_templates, audit_logs (2026-11-14)
- [x] ResultService: upsert results, auto-select reference range by age/gender, compute flag (NORMAL/LOW/HIGH/CRITICAL) (2026-11-14)
- [x] Range edits isolated per result (rangeEdited=true), never modifies catalogue (2026-11-14)
- [x] ResultController: enter results (TECHNICIAN/PATHOLOGIST/LAB_ADMIN), list results, edit range (PATHOLOGIST/LAB_ADMIN) (2026-11-14)
- [x] Visit_test status transitions: PENDING→ENTERED (on result entry), ENTERED→VERIFIED (on report verify) (2026-11-14)
- [x] ReportService: state machine DRAFT→VERIFIED→RELEASED→SUPERSEDED (2026-11-14)
- [x] Report verify: marks all visit_tests VERIFIED (2026-11-14)
- [x] Report release: builds immutable JSON snapshot, generates PDF, stores file (2026-11-14)
- [x] Report correction: RELEASED→SUPERSEDED + new DRAFT version (2026-11-14)
- [x] AuditLog written on every report state change (2026-11-14)
- [x] ReportPdfGenerator: OpenPDF, A4, patient block, results table with flag colour coding (2026-11-14)
- [x] Tests: 95 passing — ResultServiceTest (18 unit), ReportControllerTest (11), plus all prior (2026-11-14)

## Phase 5: Web App (Step 7)

- [x] Vite + React + TypeScript + Tailwind CSS v4 project scaffolded (2026-11-14)
- [x] Design tokens (CSS custom properties): navy, teal, green, amber, red, status colours, flag colours (2026-11-14)
- [x] Shared UI: Button, Input, Card, Table, Modal, StatusChip, FlagChip, Toast (2026-11-14)
- [x] Left-sidebar layout with role-based nav, topbar, toast notifications (2026-11-14)
- [x] Auth: login page (lab staff + super admin toggle), session in localStorage, 401 redirect (2026-11-14)
- [x] All strings in src/lib/strings.ts for Telugu translation readiness (2026-11-14)
- [x] Dashboard page (placeholder stats) (2026-11-14)
- [x] Patients: list with search/pagination, create modal (2026-11-14)
- [x] Visits: list, new visit (patient search, test selection, bill summary, payment), visit detail (bill/payment/sample update, links to results/report) (2026-11-14)
- [x] Results: enter results per parameter, flag display (colour-coded), range edit (PATHOLOGIST/LAB_ADMIN) (2026-11-14)
- [x] Reports: list, report detail — create draft, verify, release (PDF generation), download PDF, correction flow (2026-11-14)
- [x] Catalogue: read-only list with search/pagination (2026-11-14)
- [x] Setup: branch management, user management with role assignment (2026-11-14)
- [x] Super admin: tenant list, onboard lab (create tenant + first admin), suspend/activate (2026-11-14)
- [x] Build passes, 318 KB JS / 21 KB CSS (gzipped: 99 KB / 5 KB) (2026-11-14)

## Phase 6: Mobile App (Step 8)

- [x] pubspec.yaml: flutter_riverpod, go_router, dio, flutter_secure_storage, share_plus, flutter_pdfview, path_provider (2026-11-14)
- [x] AppTheme: Material 3, navy/teal/green brand colours, CardThemeData, InputDecorationTheme, ElevatedButtonTheme (2026-11-14)
- [x] Shared widgets: StatusChip, FlagChip, LabeledField, ErrorRetry, paise() (2026-11-14)
- [x] API client: Dio provider, token interceptor (FlutterSecureStorage), apiError() helper (2026-11-14)
- [x] Auth model: AuthUser (canAccessMobile / hasFullAccess / isCollector / hasRole), AuthNotifier (persist+restore from secure storage), authProvider (2026-11-14)
- [x] All strings in api_strings.dart (S class) for Telugu translation readiness (2026-11-14)
- [x] GoRouter: auth guard, plan check, /login /no-access /dashboard /collections /patients /visits /reports (2026-11-14)
- [x] ShellPage: bottom nav — 5 tabs for MOB_FULL, single-screen (no nav) for MOB_COLLECT (2026-11-14)
- [x] Login page: lab ID + email + password, error display, redirects by plan (2026-11-14)
- [x] NoAccessPage: plan gating screen with logout (2026-11-14)
- [x] Collection home: pending collections list, pull-to-refresh (MOB_COLLECT primary screen) (2026-11-14)
- [x] Collection detail: sample list, per-sample Collect button, Mark All Collected, retry on failure (2026-11-14)
- [x] Dashboard: stats grid (today's visits, pending reports, etc.), recent visits list (2026-11-14)
- [x] Patients: list with live search (2026-11-14)
- [x] Visits: list with status chip + amount (2026-11-14)
- [x] Reports: list, share RELEASED report as PDF via OS share sheet (WhatsApp etc.) (2026-11-14)
- [x] Tests: 10 passing — AuthUser (6 unit), PendingCollection/CollectionTest fromJson (4 unit) (2026-11-14)
- [x] flutter analyze: no issues (2026-11-14)

## Phase 7: Hardening (Step 9)

- [x] Rate limiting: LoginRateLimiter — 10 attempts per IP per 15 min; 429 on breach; reset on success (2026-11-14)
- [x] Security headers: X-Frame-Options DENY, X-Content-Type-Options nosniff, Referrer-Policy strict-origin-when-cross-origin, Permissions-Policy (2026-11-14)
- [x] CORS: configurable via CORS_ALLOWED_ORIGINS env var; default localhost:5173 for dev (2026-11-14)
- [x] MDC context filter (MdcContextFilter): requestId, method, path, tenantId, userId per request; never logs request bodies (PII guard) (2026-11-14)
- [x] GlobalExceptionHandler: added Slf4j logging — log.error for 5xx, log.debug for 4xx, log.warn for data integrity; no stack traces in client responses (2026-11-14)
- [x] application-prod.yml: pool sizing, logging levels, MDC log pattern (2026-11-14)
- [x] .env.example updated: added UPLOAD_DIR, CORS_ALLOWED_ORIGINS, SPRING_PROFILES_ACTIVE (2026-11-14)
- [x] GitHub Actions CI: api (mvn test), web (npm build), mobile (flutter analyze + test) on push/PR (2026-11-14)
- [x] Tests: 99 passing — LoginRateLimiterTest (4 new unit), plus all prior tests (2026-11-14)

## Phase 8 onwards

Copy items from phases.md and implementation.md into this list as each phase starts.

## Done

(move completed items here with the date)

## Bugs and feedback from pilot

(add here)