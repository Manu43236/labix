# LABIX: Seed Data

Version 0.1 (draft).

IMPORTANT: Reference ranges are medical data. Do not use ranges invented by an AI or copied from random websites. Fill the ranges only from a source approved by a qualified pathologist (for example a standard reference such as Tietz, or the lab's own validated ranges), and have the final set reviewed before the first pilot lab goes live. This file defines structure and the starting test list. Range values are left blank on purpose.

## 1. How seeding works

- On tenant creation, copy the default catalogue (tests, parameters, ranges, units) into the tenant.
- The lab can edit its copy. Edits never change the platform default or other tenants.
- Ranges are stored by gender and age band.

## 2. Range format (CSV)

```
test_code,parameter,unit,gender,age_min_days,age_max_days,low,high,critical_low,critical_high,range_text
```

- `gender`: M, F or ANY.
- Ages in days (so infants and children are handled).
- Use `range_text` for non-numeric ranges (for example "Negative").
- Keep one CSV per category in `docs/seed/`.

## 3. Starting test list (names and categories only)

**Haematology:** Complete Blood Count (Hb, RBC, PCV, MCV, MCH, MCHC, RDW, WBC, differential count, platelets), ESR, Blood Group and Rh, Peripheral smear.

**Biochemistry:** Fasting and Post-prandial Blood Sugar, Random Blood Sugar, HbA1c, Lipid Profile, Liver Function Tests, Kidney Function Tests (Urea, Creatinine, Uric Acid), Electrolytes, Calcium.

**Thyroid and hormones:** T3, T4, TSH.

**Serology and infection:** Widal, Dengue (NS1/IgM/IgG), Malaria antigen, HIV, HBsAg, HCV, CRP, RA factor.

**Urine and stool:** Urine routine, Urine culture, Stool routine.

**Vitamins:** Vitamin D, Vitamin B12.

**Packages (names only):** Basic health check, Diabetes profile, Fever profile, Full body check.

Prices are set by each lab. The default catalogue ships without prices (or with placeholders the lab must confirm).

## 4. Other seed data

- Roles and permissions (from plans-permissions.md).
- Plans (WEB, MOB_COLLECT, MOB_FULL) with limits.
- A demo tenant with sample patients for testing (fake data only, never real patient data).

## 5. Review checklist

- [ ] Source of each range recorded.
- [ ] Pathologist reviewed and signed off the set.
- [ ] Units checked (and consistent across tests).
- [ ] Age and gender bands checked.
- [ ] Critical values defined where relevant.