# LABIX: API Contract

Version 0.1. All endpoints return `{"success": bool, "data": ..., "error": {"code": "...", "message": "..."}}`.
Auth: `Authorization: Bearer <access_token>` on all protected endpoints.

## Auth — public

| Method | Path | Description |
|--------|------|-------------|
| POST | /auth/login | User login (email, password, tenantSlug) |
| POST | /auth/super/login | Super admin login (email, password) |
| POST | /auth/refresh | Refresh access token (refreshToken) |

## Auth — protected

| Method | Path | Description |
|--------|------|-------------|
| GET | /auth/me | Current user info (userId, tenantId, roles) |

## Super admin — all require SUPER_ADMIN role

### Tenants

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/tenants | List all tenants (page, size) |
| POST | /admin/tenants | Create tenant + initial subscription |
| GET | /admin/tenants/{id} | Get tenant |
| PUT | /admin/tenants/{id} | Update tenant info |
| POST | /admin/tenants/{id}/suspend | Suspend tenant |
| POST | /admin/tenants/{id}/activate | Activate tenant |

### Subscriptions

| Method | Path | Description |
|--------|------|-------------|
| GET | /admin/tenants/{id}/subscriptions | List subscription history |
| POST | /admin/tenants/{id}/subscriptions | Assign new plan (cancels current active) |

### Users

| Method | Path | Description |
|--------|------|-------------|
| POST | /admin/tenants/{id}/users | Create first (or additional) lab admin |

### Support access

| Method | Path | Description |
|--------|------|-------------|
| POST | /admin/tenants/{id}/support-access | Log start of support session |
| POST | /admin/support-access/{logId}/end | End support session |
| GET | /admin/tenants/{id}/support-access | List support access logs for tenant |

## Catalogue — tenant-scoped, all roles read; LAB_ADMIN + PATHOLOGIST write

| Method | Path | Description |
|--------|------|-------------|
| GET | /catalogue/tests | List tests (q, category, page, size) |
| POST | /catalogue/tests | Create test |
| GET | /catalogue/tests/{id} | Get test with parameters |
| PUT | /catalogue/tests/{id} | Update test |
| POST | /catalogue/tests/{id}/parameters | Add parameter to test |
| PUT | /catalogue/tests/{testId}/parameters/{id} | Update parameter |
| GET | /catalogue/tests/{testId}/parameters/{id}/ranges | List ranges for parameter |
| PUT | /catalogue/tests/{testId}/parameters/{id}/ranges | Replace all ranges for parameter |
| GET | /catalogue/packages | List packages |
| POST | /catalogue/packages | Create package |
| GET | /catalogue/packages/{id} | Get package |
| PUT | /catalogue/packages/{id} | Update package |

## Patients — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| GET | /patients | List/search (q, page, size); q matches name or phone |
| POST | /patients | Create patient |
| GET | /patients/{id} | Get patient |
| PUT | /patients/{id} | Update patient |
| DELETE | /patients/{id} | Soft-delete patient (LAB_ADMIN only) |

## Doctors — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| GET | /doctors | List/search (q, page, size) |
| POST | /doctors | Create doctor |
| GET | /doctors/{id} | Get doctor |
| PUT | /doctors/{id} | Update doctor |

## Branches — LAB_ADMIN only

| Method | Path | Description |
|--------|------|-------------|
| GET | /branches | List branches |
| POST | /branches | Create branch |
| PUT | /branches/{id} | Update branch |

## Lab users — LAB_ADMIN only

| Method | Path | Description |
|--------|------|-------------|
| GET | /users | List users in tenant |
| POST | /users | Create user with role(s) |
| GET | /users/{id} | Get user |
| PUT | /users/{id} | Update user |
| PUT | /users/{id}/status | Activate or deactivate |
| POST | /users/{id}/roles | Add role to user |
| DELETE | /users/{id}/roles/{role} | Remove role from user |
| POST | /users/{id}/signature | Upload signature image (multipart) |

## Visits — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| GET | /visits | List/search (q=patient name or visit no, status, date, page, size) |
| POST | /visits | Create visit + tests + bill + samples in one transaction |
| GET | /visits/{id} | Get visit with tests, bill summary, samples |
| PUT | /visits/{id}/status | Update visit status (LAB_ADMIN, RECEPTIONIST) |

### Create visit request fields
`patientId`, `branchId`, `doctorId` (optional), `visitType` (WALK_IN, HOME), `testIds[]`, `packageIds[]`, `discountPaise` (default 0)

### Visit statuses
REGISTERED → IN_PROGRESS → COMPLETED | CANCELLED

### VisitTest statuses
PENDING → RECEIVED → ENTERED → VERIFIED → RELEASED

## Bills — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| GET | /visits/{visitId}/bill | Get full bill with items and payments |
| PUT | /visits/{visitId}/bill/discount | Update discount amount (LAB_ADMIN only) |
| POST | /visits/{visitId}/payments | Record a payment |

### Payment modes
CASH, UPI, CARD, OTHER

### Bill statuses
DUE, PARTIAL, PAID

## Samples — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| GET | /visits/{visitId}/samples | List samples for a visit |
| PUT | /samples/{id}/status | Update sample status |

### Sample statuses
PENDING → COLLECTED → RECEIVED → REJECTED | RECOLLECT

### Update sample status request fields
`status` (required), `rejectReason` (required when REJECTED)

## Home collection requests — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| POST | /visits/{visitId}/collection-request | Create home collection request |
| GET | /collection-requests | List (LAB_ADMIN sees all; COLLECTOR sees own) |
| PUT | /collection-requests/{id}/assign | Assign to a collector (LAB_ADMIN) |
| PUT | /collection-requests/{id}/status | Update status (COLLECTOR or LAB_ADMIN) |

### Collection request statuses
REQUESTED → ASSIGNED → COLLECTED → DELIVERED | CANCELLED

## Results — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| POST | /visits/{visitId}/results | Batch enter/update results (TECHNICIAN, PATHOLOGIST) |
| GET | /visits/{visitId}/results | Get all results for a visit |
| PUT | /results/{id}/range | Edit range for one result (does not change catalogue) |

### Enter result fields (per item)
`parameterId` (required), `valueNumber` (nullable), `valueText` (nullable)

### Result flags (server-computed)
NORMAL, LOW, HIGH, CRITICAL

## Reports — tenant-scoped

| Method | Path | Description |
|--------|------|-------------|
| POST | /visits/{visitId}/report | Create DRAFT report |
| GET | /visits/{visitId}/report | Get current active report |
| PUT | /reports/{id}/verify | Pathologist verifies (DRAFT → VERIFIED) |
| PUT | /reports/{id}/release | Release report (VERIFIED → RELEASED), generates PDF |
| GET | /reports/{id}/pdf | Download PDF (RELEASED reports only) |
| POST | /reports/{id}/correction | Create correction (RELEASED → SUPERSEDED, new DRAFT) |

### Report statuses
DRAFT → VERIFIED → RELEASED | SUPERSEDED

### Rules
- Only PATHOLOGIST can verify or release
- A report can only be released when all visit_tests are VERIFIED
- Released reports are immutable; corrections create a new version
- Snapshot is frozen at release time; PDF is generated from the snapshot

## Error codes

| Code | HTTP | Meaning |
|------|------|---------|
| INVALID_CREDENTIALS | 401 | Wrong email or password |
| UNAUTHORIZED | 401 | No or invalid token |
| FORBIDDEN | 403 | Role or plan check failed |
| TENANT_NOT_FOUND | 404 | Tenant not found |
| TENANT_SUSPENDED | 403 | Tenant is suspended |
| USER_INACTIVE | 403 | User account inactive |
| NO_ACTIVE_SUBSCRIPTION | 403 | No active subscription |
| PLAN_INSUFFICIENT | 403 | Plan does not include the feature |
| SLUG_TAKEN | 409 | Tenant slug already in use |
| VALIDATION_ERROR | 400 | Request body failed validation |
| INTERNAL_ERROR | 500 | Unexpected server error |
