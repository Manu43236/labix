# LABIX: Product Requirements Document (PRD)

Version: 0.1 (draft) | Status: pre-development

## 1. Overview

LABIX is a cloud lab management product for small and medium diagnostic and pathology labs in India. It replaces paper, Excel and old offline desktop software with a web app, a mobile app and a shared backend.

Pilot region: Visakhapatnam, Vizianagaram, Srikakulam (Andhra Pradesh).

## 2. Problem

- Many small labs in tier-2/3 towns use paper, Word/Excel templates or offline desktop software on a single PC.
- Reports are printed or sent manually. Home collection is tracked on paper or phone calls, so samples get lost or delayed.
- Existing cloud tools are cheap but generic, and have little local onboarding, training or support.

## 3. Goals

1. Let a small lab run daily work (registration, billing, sample collection, reports) in one system.
2. Make home sample collection reliable, with the mobile app.
3. Produce clean, correct, branded reports that are easy to share.
4. Be simple enough for non-technical lab staff to adopt in a day.

### Non-goals (v1)

Automated WhatsApp/SMS, analyser (machine) integration, patient portal or login, offline mode, data migration from old software, NABL-specific features, inventory/reagent tracking.

## 3a. Success measures (pilot)

- 3 to 5 pilot labs live and using it daily.
- Labs create bills and release reports without falling back to old tools.
- Zero wrong-report incidents caused by the software.
- At least some pilot labs willing to pay full plan price after the discount period.

## 4. Users and roles

| Role | Description |
|---|---|
| Super admin | LABIX team. Onboards labs (tenants), manages plans and subscriptions, support access. |
| Lab admin | Owner/manager of a lab. Full control of their tenant. |
| Receptionist | Registers patients, creates bills, collects payments. |
| Technician | Enters test results. |
| Pathologist | Verifies results and approves/signs reports. |
| Sample collector | Collects samples (mostly home collection), mainly on mobile. |

Detailed permissions: see plans-permissions.md.

## 5. Platforms

- Web app: for lab admin, receptionist, technician, pathologist.
- Mobile app (Flutter): everything the web does, plus the collector workflow. Which features a user can access depends on the tenant's plan and the user's role.
- Super admin panel (web).
- One backend API shared by all.

## 6. Plans and pricing

Monthly, excluding GST.

| Plan | Price | Includes |
|---|---|---|
| Web only | 1200 | Web app |
| Web + Mobile (collection) | 1500 | Web + mobile app limited to sample collection |
| Web + Mobile (full) | 2000 | Web + mobile app with full access |

Pilot labs may get a discounted rate for the first 3 to 6 months. Plan limits (users, branches) to be defined in plans-permissions.md. The super admin turns features on or off per plan.

## 7. Functional requirements

### 7.1 Tenant and account management
- Super admin creates and manages tenants, assigns plan, activates/suspends.
- Each tenant has isolated data.
- Tenant profile: lab name, address, logo, contact, GST details (per tenant), letterhead settings.

### 7.2 Profile and user management
- Lab admin creates users and assigns roles.
- Each user has a profile (name, phone, role, signature image if pathologist).
- Login on web and mobile, gated by the tenant's plan and the user's role.

### 7.3 Test management
- Test catalogue per tenant: name, code, category, sample type, price, units, turnaround time.
- Default catalogue and reference ranges provided at tenant setup (see seed-data.md), editable by the lab.
- Test packages (bundles with a package price).

### 7.4 Patient management
- Register and search patients (name, age, gender, phone, address).
- Patient history: past visits and reports.
- Referring doctor on each visit.

### 7.5 Billing
- Create bills for tests and packages, with discounts.
- Payment status: paid, partial, due. Payment mode recorded.
- Printable/PDF invoice and receipt.
- GST configured per tenant.

### 7.6 Sample collection
- Generate sample IDs and barcode/labels.
- Mark samples: collected, received at lab, rejected (with reason), recollection needed.
- Home collection: schedule slots, assign collectors, track status (assigned, collected, delivered to lab).
- Collector sees their assigned list on mobile and updates status.

### 7.7 Report entry and approval
- Technician enters results per test.
- Reference ranges applied by age and gender; high/low/critical values flagged automatically.
- Range edit: ranges can be changed while generating a report, and the change applies to that report only (not to the default).
- Workflow: entered, verified by pathologist, released.
- Audit log of edits after entry.

### 7.8 Reports
- Customised report templates per tenant (header, logo, footer, layout).
- E-signature of the pathologist on released reports.
- Modern digital PDF reports, downloadable from web; shareable via WhatsApp from the mobile app (manual share, no automation in v1).

### 7.9 Referral doctors
- Doctor master list.
- Doctor-wise report and billing summary; commission tracking.

### 7.10 Dashboard
- Customisable dashboard: today's patients, bills, collections, pending reports, due payments, home-collection status.

### 7.11 Super admin panel
- Tenant list, plan assignment, subscription status, feature toggles, support access.

## 8. Mobile parity rule

Whatever the web can do, the mobile app can also do. Access is decided by the tenant's plan and the user's role, enforced in the backend.

## 9. Non-functional requirements

- Multi-tenant isolation: one lab must never see another lab's data.
- Security: authenticated access, role-based permissions, encrypted data in transit and at rest.
- Privacy: patient health data is sensitive; follow India's DPDP Act. Privacy policy and terms of use are required before launch.
- Reliability: daily backups; data export for tenants.
- Performance: normal screens load in a couple of seconds on average mobile networks.
- Language: English in v1. Telugu support later.
- Hosting: AWS.

## 10. Assumptions and risks

- A friend's network of about 30 labs is the initial pilot pool. Not yet confirmed by commitments.
- Report accuracy is the top risk: default ranges need review by a qualified pathologist.
- Collectors work in weak-network areas, but offline mode is out of v1.
- Pricing (1200 to 2000/month) sits above many cheap competitors; value must come from home collection and local support.
- The owning company for LABIX is undecided; settle before publishing apps or signing pilot agreements.

## 11. Open questions

- Which database and which hosting layout (see TRD.md).
- Plan limits: users, branches, storage per plan.
- Exact definition of "collection-only" vs "full access" mobile.
- Letterhead/print requirements from pilot labs.
- Pathologist to review default reference ranges.

## 12. Later (post-v1)

Automated WhatsApp/SMS, analyser integration, patient portal, offline mode for collectors, data migration tools, Telugu language, inventory/reagents, NABL support, outsourced test tracking, multi-branch, expense tracking.