# LABIX: App Flows

Version 0.1 (draft). Flows apply to web and mobile unless stated. Mobile has the same flows, gated by plan and role.

## 1. Login

1. User enters phone/email and password.
2. API returns role(s) and plan capabilities.
3. App opens the home screen for the role. If the plan does not include mobile, the mobile app shows "Your plan does not include mobile access" and stops.

## 2. Super admin: onboard a lab

1. Create tenant (name, contact, GST details).
2. Assign plan and dates (apply pilot discount note if any).
3. Create the first LAB_ADMIN user, share login.
4. Seed default tests and ranges for the tenant.

## 3. Lab admin: setup

1. Fill lab profile, logo, letterhead.
2. Add users and roles; upload pathologist signature.
3. Review test catalogue, prices and default ranges.
4. Add referral doctors.

## 4. Walk-in visit (receptionist)

1. Search patient by phone or name, or register new.
2. Choose referring doctor.
3. Add tests and packages.
4. Apply discount, create bill, take payment (paid, partial, due).
5. Print or share bill. Samples are created with labels/barcodes.

## 5. Home collection

1. Receptionist or admin creates a visit as HOME and a collection request (address, slot).
2. Admin assigns a collector.
3. Collector (mobile) sees the assigned list, opens a request, collects samples, marks COLLECTED.
4. Collector brings samples to lab; technician marks them RECEIVED.
5. If a sample is bad: reject with reason, request recollection, collector gets a new task.

## 6. Result entry and approval

1. Technician opens received samples, enters results per parameter.
2. System applies range by patient age and gender and flags low/high/critical.
3. Technician can edit the range for this report only (marked as edited).
4. Technician submits for verification.
5. Pathologist reviews, then approves (e-signature applied) or sends back with a comment.
6. On approval the report is released and the PDF is generated.

## 7. Report sharing

- Web: download PDF.
- Mobile (full plan): share PDF through the share sheet (WhatsApp, email, etc.).

## 8. Payments after the visit

1. Open bill with due amount.
2. Record payment (mode, amount). Status updates to PARTIAL or PAID.

## 9. Dashboard

Role-based summary: today's patients, bills, collections, pending reports, due amounts, home-collection status.

## 10. Edge cases to handle

- Wrong patient details found after release: correct, create a new report version, re-approve.
- Test price changed after billing: old bills keep the price at billing time.
- User with several roles sees the combined menu.
- Plan changes mid-session: next API call returns PLAN_NOT_INCLUDED, app refreshes capabilities.
- Network failure on mobile: show retry, never silently lose a status update.