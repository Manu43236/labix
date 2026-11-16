# LABIX: Design Guidelines

Version 0.1 (draft).

## 1. Brand

- Name: LABIX (uppercase).
- Logo: docs/assets/labix_logo.png. Test tube forms the "I"; X has a green leaf shape and a molecule accent.
- App icon: simplified mark (test tube + X only). Create a square version.

## 2. Colours (take exact values from the logo file)

| Use | Colour |
|---|---|
| Primary / text on light | Navy (from "LAB" in the logo) |
| Accent | Teal (tube and droplet) |
| Success / positive | Green (from the X) |
| Background | White and light grey |
| Warning | Amber |
| Danger / critical values | Red |

Sample exact hex values from the logo and record them here before building.

## 3. Principles

1. Simple, fast, low-training. Lab staff are not technical.
2. Few clicks for daily tasks: new patient to bill in under a minute.
3. Large touch targets on mobile (collector uses it in the field).
4. Clear status colours: pending, collected, received, verified, released.
5. Abnormal values are obvious: low/high/critical flagged with colour and a symbol, not colour alone.

## 4. Web

- Left sidebar navigation, top bar with lab name, user menu.
- Tables with search, filters, pagination.
- Forms with inline validation.
- Responsive down to tablet width.

## 5. Mobile

- Bottom navigation. Role-based tabs.
- Collector home: today's assigned collections as cards with address, time, phone, status button.
- One primary action per screen.
- Clear offline/network error states (no offline mode in v1).

## 6. Typography and components

- One clean sans-serif font family across apps.
- Shared component set per app (buttons, inputs, tables/cards, status chips, dialogs).
- Keep web and mobile visually consistent but native in behaviour.

## 7. Report PDF design

See report-template-spec.md. Reports must look clean and professional on screen and in print.

## 8. Accessibility

- Contrast that passes basic WCAG AA.
- Text scalable.
- Never rely on colour alone.

## 9. Language

English in v1. Keep all strings in resource files so Telugu can be added later.