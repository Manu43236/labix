# LABIX: Report Template Spec

Version 0.1 (draft). Reports are the main output of the product. Accuracy and clarity come first.

## 1. Page

- A4 portrait. Margins adjustable per tenant (many labs print on pre-printed letterhead).
- Option per tenant: print with header/footer, or leave header/footer space blank for letterhead paper.

## 2. Header (configurable per tenant)

- Logo, lab name, address, phone, email.
- Optional registration/accreditation line (free text).
- Branch name if the lab has several.

## 3. Patient and visit block

- Patient name, age, gender, patient no.
- Referring doctor.
- Visit/bill no, sample ID(s), sample type.
- Collected, received and reported date-times.
- Collection type (walk-in or home).

## 4. Results table

Columns: Test/parameter, Result, Unit, Reference range, Flag.

- Group by test or category (for example Haematology, Biochemistry).
- Low and high values are marked with H/L and styled (bold or colour). Critical values stand out more and are never shown by colour alone.
- If the range was edited for this report, show the range actually used. Mark it in the data (not necessarily visible to the patient).
- Text results (for example impressions) have a notes area.
- Decimals and units follow the parameter definition.

## 5. Footer

- "End of report" line.
- Pathologist name, qualification, registration number, e-signature image.
- Technician name (optional).
- Disclaimer line (configurable text), page numbers, report ID.
- Optional QR code linking to report verification (later).

## 6. Rules

1. Reports are generated from a stored snapshot, not from live data.
2. A released report is read-only. A correction creates a new version and marks the old one SUPERSEDED, with an audit log entry.
3. Only approved (VERIFIED) results can appear on a released report. No signature, no release.
4. Every report shows the range used for each value.
5. Abnormal flags are computed by the server.

## 7. Template config (stored in `report_templates.config_json`)

Suggested fields: `showHeader`, `showFooter`, `marginTopMm`, `marginBottomMm`, `logoPosition`, `fontFamily`, `fontSizePt`, `accentColor`, `disclaimerText`, `showTechnician`, `showSignature`, `groupByCategory`.

## 8. Multi-test and package reports

- One report per visit, all tests together.
- Packages show as one section with the package name, listing its tests.

## 9. Open items

- Final layout mockups (get 2 or 3 sample reports from pilot labs).
- Regional or compliance fields required by pilot labs.
- Pathologist review of the default layout.