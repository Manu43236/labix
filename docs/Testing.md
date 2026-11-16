# LABIX: Testing

Version 0.1 (draft).

## 1. Principles

- Test what can hurt a lab: wrong reports, wrong totals, leaking data between tenants, wrong permissions.
- Every new feature ships with tests.
- Tests run before each commit that touches logic (and in CI later).

## 2. Must-pass critical flows

1. **Tenant isolation:** a user from tenant A can never read or change tenant B data (every endpoint).
2. **Plan gating:** a tenant on WEB cannot use mobile-only features; MOB_COLLECT cannot use full mobile features. Returns PLAN_NOT_INCLUDED.
3. **Role gating:** each role can do only what plans-permissions.md allows. Collector sees only assigned collections.
4. **Billing math:** totals, discounts, tax, partial and full payments in paise, status transitions.
5. **Reference ranges:** correct range chosen by age and gender, including boundary ages; correct flags (normal, low, high, critical).
6. **Range edit:** edit applies only to that report; catalogue and other reports unchanged.
7. **Report workflow:** only a pathologist can approve; cannot release unverified results; released report is immutable; correction creates a new version and audit entry.
8. **Report PDF:** generated from the snapshot, contains correct values, ranges, flags and signature.
9. **Sample states:** valid transitions only (for example, cannot enter results on a sample not received).
10. **Home collection:** assign, collect, deliver, reject and recollect flow.

## 3. Levels

| Level | Tool (PROPOSED) | Scope |
|---|---|---|
| API unit | JUnit | Services: billing, ranges, flags, permissions |
| API integration | Spring Boot Test + Testcontainers (MySQL) | Endpoints, tenant isolation, workflows |
| Web | Vitest + React Testing Library | Components, forms, role-based UI |
| Mobile | Flutter test + widget tests | Collector flow, gating, error states |
| End to end | Manual script for pilot, automate later | Visit to released report |

## 4. Test data

- Fake data only. Never use real patient data.
- A demo tenant with sample patients, tests and ranges (see seed-data.md).

## 5. Manual checklist before each pilot release

- [ ] Create tenant, login as each role
- [ ] Walk-in visit to released PDF report
- [ ] Home collection from request to released report
- [ ] Print the report on the pilot lab's letterhead
- [ ] Share a PDF from mobile
- [ ] Backup and restore tested

## 6. Definition of done

Feature works end to end, tests added and passing, docs updated, no known data-leak or wrong-report risks.