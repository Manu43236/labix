# LABIX: Database Schema

Version 0.1 (draft). MySQL. Conventions below apply to every table unless stated.

## Conventions

- Primary key `id` (UUID, stored as BINARY(16) or CHAR(36); pick one and use it everywhere).
- Tenant-owned tables have `tenant_id` (FK to `tenants`), indexed.
- Columns on all tables: `created_at`, `updated_at` (UTC), `created_by`, `updated_by`.
- Soft delete (`deleted_at`) on patients, bills, reports, visits.
- Money in integer paise. Enums stored as VARCHAR with a CHECK constraint (MySQL 8.0.16+) or ENUM. `*_json` columns use the MySQL JSON type. Engine InnoDB, charset utf8mb4.

## 1. Platform tables (not tenant-owned)

**plans**: `code` (WEB, MOB_COLLECT, MOB_FULL), `name`, `price_paise`, `limits_json`, `features_json`.

**tenants**: `name`, `slug`, `status` (ACTIVE, SUSPENDED), `phone`, `email`, `address`, `gstin`, `gst_enabled`, `logo_file_id`, `letterhead_json`.

**tenant_subscriptions**: `tenant_id`, `plan_code`, `limits_override_json`, `features_override_json`, `start_date`, `end_date`, `status`, `discount_note`.

**super_admins**: `name`, `email`, `password_hash`.

**support_access_logs**: `super_admin_id`, `tenant_id`, `reason`, `started_at`, `ended_at`.

## 2. Users and access

**users**: `tenant_id`, `name`, `phone`, `email`, `password_hash`, `status`, `signature_file_id` (pathologists).

**user_roles**: `user_id`, `role` (LAB_ADMIN, RECEPTIONIST, TECHNICIAN, PATHOLOGIST, COLLECTOR). A user can have several roles.

**branches**: `tenant_id`, `name`, `address`, `phone`, `is_main`.

**user_branches**: `user_id`, `branch_id`.

## 3. Catalogue

**tests**: `tenant_id`, `code`, `name`, `category`, `sample_type`, `price_paise`, `tat_hours`, `is_active`.

**test_parameters**: `test_id`, `name`, `unit`, `value_type` (NUMBER, TEXT, CHOICE), `choices_json`, `decimals`, `sort_order`. (A test like CBC has many parameters.)

**reference_ranges**: `parameter_id`, `gender` (M, F, ANY), `age_min_days`, `age_max_days`, `low`, `high`, `critical_low`, `critical_high`, `range_text`.

**packages**: `tenant_id`, `name`, `price_paise`, `is_active`.

**package_tests**: `package_id`, `test_id`.

## 4. People

**patients**: `tenant_id`, `patient_no` (per-tenant sequence), `name`, `dob`, `age_years` (if dob unknown), `gender`, `phone`, `email`, `address`.

**doctors**: `tenant_id`, `name`, `phone`, `clinic`, `commission_type` (NONE, PERCENT, FLAT), `commission_value`.

## 5. Visits, billing, samples

**visits**: `tenant_id`, `branch_id`, `patient_id`, `doctor_id`, `visit_no`, `visit_type` (WALK_IN, HOME), `visit_date`, `status`.

**visit_tests**: `visit_id`, `test_id`, `package_id` (nullable), `price_paise`, `status` (PENDING, RECEIVED, ENTERED, VERIFIED, RELEASED).

**bills**: `tenant_id`, `visit_id`, `bill_no`, `subtotal_paise`, `discount_paise`, `tax_paise`, `total_paise`, `paid_paise`, `status` (PAID, PARTIAL, DUE).

**bill_items**: `bill_id`, `description`, `amount_paise`.

**payments**: `bill_id`, `amount_paise`, `mode` (CASH, UPI, CARD, OTHER), `paid_at`, `reference`.

**samples**: `tenant_id`, `visit_id`, `sample_no`, `barcode`, `sample_type`, `status` (PENDING, COLLECTED, RECEIVED, REJECTED, RECOLLECT), `collected_by`, `collected_at`, `received_by`, `received_at`, `reject_reason`.

**collection_requests** (home collection): `tenant_id`, `visit_id`, `address`, `contact_phone`, `slot_start`, `slot_end`, `collector_id`, `status` (REQUESTED, ASSIGNED, COLLECTED, DELIVERED, CANCELLED), `notes`.

## 6. Results and reports

**results**: `visit_test_id`, `parameter_id`, `value_text`, `value_number`, `unit`, `range_low`, `range_high`, `range_text`, `flag` (NORMAL, LOW, HIGH, CRITICAL), `range_edited` (bool), `entered_by`, `entered_at`.

Note: `range_*` columns store the range actually used for this result. Editing a range at report time changes only this row, never the catalogue.

**reports**: `tenant_id`, `visit_id`, `version`, `status` (DRAFT, VERIFIED, RELEASED, SUPERSEDED), `snapshot_json`, `pdf_file_id`, `verified_by`, `verified_at`, `released_at`.

**report_templates**: `tenant_id`, `name`, `config_json` (header, footer, layout, fonts), `is_default`.

## 7. Files and audit

**files**: `tenant_id`, `storage_key`, `mime_type`, `size_bytes`, `purpose`.

**audit_logs**: `tenant_id`, `user_id`, `entity_type`, `entity_id`, `action`, `before_json`, `after_json`, `at`.

## 8. Key constraints

- `patient_no`, `visit_no`, `bill_no`, `sample_no`: unique per tenant (and per branch if numbering is branch-based).
- A report can be RELEASED only if every visit_test in it is VERIFIED.
- Only one non-superseded report per visit at a time.
- `user_roles` must contain at least one role per active user.

## 9. Indexes (start with these)

- `(tenant_id, patient_id)`, `(tenant_id, visit_date)`, `(tenant_id, status)` on visits.
- `(tenant_id, phone)` and `(tenant_id, name)` on patients.
- `(tenant_id, collector_id, status)` on collection_requests.
- `(tenant_id, barcode)` on samples.

## 10. Open items

- Numbering format for patient, visit and bill numbers.
- Whether numbering is per branch.
- Final field list for `letterhead_json` and `config_json` (see report-template-spec.md).