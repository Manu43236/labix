# LABIX: Plans and Permissions

Version 0.1 (draft). Items marked PROPOSED need a decision from the owner.

Both plan and role checks are enforced in the API. The UI only hides what the API would reject.

## 1. Plans

Prices are monthly, excluding GST.

| Plan code | Name | Price |
|---|---|---|
| WEB | Web only | 1200 |
| MOB_COLLECT | Web + Mobile (sample collection only) | 1500 |
| MOB_FULL | Web + Mobile (full access) | 2000 |

### 1.1 Plan limits (PROPOSED)

| Limit | WEB | MOB_COLLECT | MOB_FULL |
|---|---|---|---|
| Staff users (non-collector) | 5 | 5 | 10 |
| Sample collector users | 0 | 5 | 10 |
| Branches / collection centres | 1 | 1 | 3 |
| Bills per month | unlimited | unlimited | unlimited |
| Storage (reports, signatures, logos) | 2 GB | 2 GB | 5 GB |

If a limit is reached, the API blocks creating more and returns a clear error. Limits are stored per tenant (copied from the plan at assignment), so the super admin can override them for a specific lab.

## 2. Plan x feature matrix

Y = available, N = not available.

| Feature | WEB | MOB_COLLECT | MOB_FULL |
|---|---|---|---|
| Web app (all roles) | Y | Y | Y |
| Patient management (web) | Y | Y | Y |
| Test and package management (web) | Y | Y | Y |
| Billing (web) | Y | Y | Y |
| Report entry, approval, PDF (web) | Y | Y | Y |
| Referral doctors (web) | Y | Y | Y |
| Dashboard (web) | Y | Y | Y |
| Home collection scheduling (web) | N | Y | Y |
| Mobile login | N | Y | Y |
| Mobile: collector workflow (assigned list, collect, update status, sample labels) | N | Y | Y |
| Mobile: patient, billing, report entry/approval, doctors, dashboard, share report PDF | N | N | Y |

Notes:
- In MOB_COLLECT, only the sample collector role (and lab admin, for viewing the collection list) can log in on mobile.
- In MOB_FULL, every role can log in on mobile and use what their role allows.
- Report PDF sharing via WhatsApp is a mobile feature, so it needs MOB_FULL. On WEB and MOB_COLLECT, reports are downloaded from the web. (PROPOSED. Alternative: allow share in MOB_COLLECT for collectors only.)

## 3. Roles

| Role | Description |
|---|---|
| SUPER_ADMIN | LABIX team. Not inside any tenant. |
| LAB_ADMIN | Owner or manager of the lab. |
| RECEPTIONIST | Front desk: patients, billing. |
| TECHNICIAN | Enters results. |
| PATHOLOGIST | Verifies and approves reports. |
| COLLECTOR | Collects samples, mostly on mobile. |

## 4. Role x action matrix (within a tenant)

C = create, R = read, U = update, D = delete, A = approve, X = no access. A role can only act if the tenant's plan includes the feature.

| Area | LAB_ADMIN | RECEPTIONIST | TECHNICIAN | PATHOLOGIST | COLLECTOR |
|---|---|---|---|---|---|
| Tenant profile and letterhead | R U | R | X | X | X |
| Users and roles | C R U D | X | X | X | X |
| Test catalogue, ranges, packages | C R U D | R | R | R U | X |
| Patients | C R U D | C R U | R | R | R (assigned only) |
| Billing and payments | C R U D | C R U | X | X | X |
| Sample collection and labels | C R U D | C R U | R U (receive, reject) | R | R U (assigned only) |
| Home collection scheduling | C R U D | C R U | X | X | R (assigned only) |
| Result entry | R | X | C R U | R | X |
| Report approval and e-signature | R | X | X | A | X |
| Report PDF download / share | R | R | R | R | X |
| Referral doctors and commission | C R U D | R | X | X | X |
| Dashboard | R (all) | R (limited) | R (limited) | R (limited) | R (own tasks) |
| Audit log | R | X | X | X | X |

Rules:
- Only PATHOLOGIST can approve and sign a report. Released reports are read-only; any later edit creates an audit log entry and needs re-approval.
- Result entry by TECHNICIAN only, and only on samples marked received.
- Range edit at report time: TECHNICIAN and PATHOLOGIST can edit ranges for one report. Only LAB_ADMIN and PATHOLOGIST can edit default ranges in the catalogue.
- Deletes are soft deletes where the record has financial or medical history (bills, reports, patients).
- COLLECTOR sees only samples and patients assigned to them.

## 5. Super admin permissions

| Action | SUPER_ADMIN |
|---|---|
| Create, edit, suspend, reactivate tenants | Y |
| Assign or change plan | Y |
| Override per-tenant limits and feature toggles | Y |
| View subscription status and due dates | Y |
| Support access into a tenant (read-only by default, logged) | Y |
| Read patient or report data without a support reason | N |

Support access to tenant data must be logged with who, when and why, because the data is sensitive health information.

## 6. Enforcement rules (for the API)

1. Every request carries a tenant id and user id from the auth token. Never trust a tenant id sent by the client.
2. Check in this order: tenant active, plan includes the feature, role allows the action, record belongs to the tenant (and is assigned, for collectors).
3. A suspended tenant gets read-only access to existing data (PROPOSED) or is blocked, per super admin choice.
4. Return 403 for plan or role failures, with a code that tells the client why (PLAN_NOT_INCLUDED, ROLE_NOT_ALLOWED, LIMIT_REACHED).
5. Mobile and web use the same endpoints and the same checks.

## 7. Open decisions

- Confirm the plan limits in 1.1.
- Confirm whether report sharing in MOB_COLLECT is allowed for collectors.
- What happens when a subscription lapses: block everything, or read-only?
- Can one person hold more than one role (for example, owner who is also the pathologist)? Recommended: yes, multiple roles per user.