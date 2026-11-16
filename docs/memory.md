# LABIX: Project Memory

Decisions and context carried between sessions. Add dated entries. Keep it short.

## Product

- Name: LABIX (always uppercase). Domain: thelabos.com is available (final domain to confirm). Logo done (docs/assets/labix_logo.png).
- Cloud lab management SaaS for small/medium diagnostic labs. Pilot region: Vizag, Vizianagaram, Srikakulam.
- Pilot pool: a friend's network of about 30 labs (not yet committed).
- Plans (monthly, excl. GST): Web only 1200 | Web + mobile (collection) 1500 | Web + mobile (full) 2000.
- Mobile does everything web does, gated by tenant plan and user role.
- No WhatsApp/SMS automation in v1. Report PDF is shared manually from mobile.
- Reference ranges: standard defaults, editable per report (applies to that report only).
- GST configured per tenant.
- No offline mode and no data migration in v1.
- Super admin exists for onboarding and subscriptions.

## Technical

- Monorepo: api (Spring Boot), web (React), mobile (Flutter), docs.
- Built with Claude Code from the repo root.
- Database: MySQL 8 (decided). Auth approach and plan limits: proposed, to confirm.

## Open decisions

- Owning company (MandM Technologies or a new company).
- Terms with the friend who brings the labs (none agreed yet).
- Timeline and budget.
- Pathologist to review ranges and report layout.

## Decision log

- 2026-11-13: UUID stored as CHAR(36) (not BINARY(16)) for readability in logs and queries.
- 2026-11-13: Stateless JWT (no server-side refresh token store) — simple, revisit if revocation is needed.
- 2026-11-13: User login requires email + password + tenantSlug — avoids cross-tenant email collisions.
- 2026-11-13: `/auth/login`, `/auth/super/login`, `/auth/refresh` are public; all other endpoints require auth.
- 2026-11-13: Flyway disabled in test profile; Hibernate create-drop + H2 used instead.
- 2026-11-13: AccessDeniedException from @PreAuthorize must be caught in GlobalExceptionHandler (not Spring Security's access denied handler) because the catch-all handler intercepts it first.
- 2026-11-13: Plan seed data (WEB/MOB_COLLECT/MOB_FULL) inserted in test setUp since Flyway is off; the PlanRepository.existsById check in createTenant validates plan codes.
- 2026-11-14: Lombok boolean fields named `isFoo` generate setter `setFoo()` (strips `is` prefix). Avoid `is`-prefixed boolean field names in entities; use `active`, `main` etc. with explicit `@Column(name="is_active")`.
- 2026-11-14: Spring Data JPA derived method for boolean field `main` (getter `isMain()`) is `existsByTenantIdAndMain`, NOT `existsByTenantIdAndIsMain`.
- 2026-11-14: Auth login response includes `roles` list — clients need this to build role-aware UI without decoding the JWT.
- 2026-11-14: Default catalogue seeded per tenant (21 tests, 4 packages). Range values intentionally empty — must be filled by a qualified pathologist before pilot.
- 2026-11-14: Visit creation is a single transaction: visit → visit_tests → bill + bill_items → samples. Samples are auto-created one per distinct non-null sample_type across all tests in the visit.
- 2026-11-14: Package booking: tests in a package each get price_paise=0 on visit_tests; a single bill_item at package price covers them. Individual tests each get their own bill_item.
- 2026-11-14: Invoice PDF deferred — needs iText or JasperReports, out of scope for v0.1 pilot.
- 2026-11-14: Visit/bill/sample sequence numbers (visit_no, bill_no, sample_no) are per-tenant, managed in application code with MAX()+1 inside a @Transactional method.
- 2026-11-14: ReferenceRange uses BigDecimal for low/high/criticalLow/criticalHigh; Result uses Double. applyRange() calls .doubleValue() when copying range values to a result.
- 2026-11-14: Visit_test status machine: PENDING→ENTERED (when any result is entered for that test) → VERIFIED (when report is verified by pathologist). Report can only be released when all visit_tests are VERIFIED.
- 2026-11-14: Report verify step marks all visit_tests VERIFIED in bulk — no separate per-test verify endpoint needed for v1.
- 2026-11-14: Flag computation is server-side only (NORMAL/LOW/HIGH/CRITICAL). Critical check runs before high/low. No flag if no range is set.
- 2026-11-14: Report snapshot is built at RELEASE time (not at VERIFY). Snapshot is immutable JSON (LONGTEXT) frozen at release; corrections create a new DRAFT version and supersede the old report.
- 2026-11-14: ReportPdfGenerator uses OpenPDF (com.github.librepdf:openpdf:1.4.1). LineSeparator is in com.lowagie.text.pdf.draw, not com.lowagie.text.pdf.
- 2026-11-14: PDF stored on local disk under {app.uploads.dir}/reports/{tenantId}/. LabFile entity records the storage_key (absolute path) and file metadata. Future: replace with S3.
- 2026-11-14: Web app stack: Vite 6, React 18, TypeScript, Tailwind CSS v4 (no tailwind.config.js; uses @tailwindcss/vite plugin). All design tokens as CSS custom properties in src/styles/tokens.css.
- 2026-11-14: All user-facing strings in src/lib/strings.ts for Telugu translation readiness.
- 2026-11-14: Dev server on :5173 (or next available), proxies /api → localhost:8080 so the API runs separately on :8080.
- 2026-11-14: Session stored in localStorage (token + user JSON). 401 interceptor clears session and redirects to /login.
- 2026-11-14: RequireAuth wrapper in App.tsx — no route is accessible without a valid session in localStorage.
- 2026-11-14: Mobile stack: Flutter 3.41 / Dart 3.11, Riverpod 2.6, go_router 15, Dio 5, flutter_secure_storage 9, share_plus 10, path_provider 2.
- 2026-11-14: Mobile auth: JWT + user JSON persisted in FlutterSecureStorage. AuthNotifier restores state on app restart.
- 2026-11-14: Plan gating in mobile: after login, check canAccessMobile (planCode == MOB_COLLECT or MOB_FULL). If not, show NoAccessPage. MOB_COLLECT goes to /collections only. MOB_FULL gets full bottom nav.
- 2026-11-14: share_plus v10 uses Share.shareXFiles([XFile(path)], subject: ...) — not SharePlus.instance.share(ShareParams(...)). The latter is v13+.
- 2026-11-14: Flutter CardTheme was renamed to CardThemeData in recent Flutter versions. Use CardThemeData in ThemeData.
- 2026-11-14: Mobile collector flow: GET /collections/mine (list) → GET /collections/mine/:visitId (detail) → PATCH /visit-tests/:id/sample-status {status: COLLECTED}. Errors show a SnackBar with retry; never silent.
- 2026-11-14: Mobile report PDF share: GET /reports/:id/pdf (responseType: bytes) → write to temp dir → Share.shareXFiles(). Share sheet includes WhatsApp.
- 2026-11-14: `RequestContextFilter` name collides with Spring's own bean from WebMvcAutoConfiguration. Renamed to `MdcContextFilter` to avoid BeanDefinitionOverrideException.
- 2026-11-14: Rate limiter is in-memory (ConcurrentHashMap per IP). Good for single-instance v1 pilot. If multi-instance is ever needed, replace with Redis + Bucket4j.
- 2026-11-14: CORS origins configured via `CORS_ALLOWED_ORIGINS` env var (comma-separated). Default: `http://localhost:5173`. Must be set to prod web domain before go-live.
- 2026-11-14: GitHub Actions CI file at `.github/workflows/ci.yml` — runs api/web/mobile in parallel on push to main/develop and on PRs to main.