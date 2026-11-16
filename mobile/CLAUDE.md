# LABIX Mobile (Flutter)

Flutter app for collectors and, on the full plan, all roles. Mirrors the web features, gated by tenant plan and user role. Read ../CLAUDE.md, ../docs/plans-permissions.md, ../docs/appflow.md and ../docs/API.md first.

## Commands (fill in once the project exists)

- Run: `flutter run`
- Test: `flutter test`
- Build: `flutter build apk`

## Rules

- One state management approach across the app (proposed: Riverpod).
- One API client layer, same error handling as web.
- After login, read `capabilities`. If the plan has no mobile access, show a clear message and stop.
- MOB_COLLECT plan: collector screens only. MOB_FULL: everything the user's role allows.
- Never lose a collector's status update silently: show retry on failure. No offline mode in v1.
- Large touch targets, clear loading and error states.
- Report PDFs are shared through the OS share sheet.