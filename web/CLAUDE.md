# LABIX Web (React)

React + TypeScript. Used by lab admin, receptionist, technician, pathologist and super admin. Read ../CLAUDE.md, ../docs/appflow.md, ../docs/design.md and ../docs/API.md first.

## Commands

- Run: `npm run dev` (starts on :5173, proxies /api → localhost:8080)
- Test: `npm test`
- Build: `npm run build`

## Stack

- Vite 6, React 18, TypeScript, Tailwind CSS v4
- TanStack Query v5 for server state
- React Router v6
- Axios via `src/lib/api.ts`

## Structure

```
src/
  lib/        api.ts, auth.ts, strings.ts, fmt.ts
  components/
    ui/       Button, Input, Modal, Table, Card, StatusChip, FlagChip, Toast
    Layout.tsx
  pages/
    auth/     LoginPage
    dashboard/ DashboardPage
    patients/  PatientsPage
    visits/    VisitsPage, NewVisitPage, VisitDetailPage
    results/   ResultsPage
    reports/   ReportsListPage, ReportPage
    catalogue/ CataloguePage
    setup/     SetupPage
    admin/     AdminPage
  styles/     tokens.css  (CSS custom properties for all brand colours)
```

## Rules

- All API calls go through one client module. Handle 401, 403 (PLAN_NOT_INCLUDED, ROLE_NOT_ALLOWED) and network errors in one place.
- Show or hide UI using the `capabilities` from login, but never treat that as security.
- No business logic here: flags, totals and range selection come from the API.
- Keep all user-facing strings in one place for later Telugu support.
- Follow the colours and layout in ../docs/design.md.