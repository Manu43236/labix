# LABIX: Coding Rules

Version 0.1 (draft). These apply to all work, including Claude Code sessions.

## General

1. Follow the docs. Do not invent features, endpoints or tables. If something is missing, ask or update the doc first.
2. Work only in the folder named in the task (`api/`, `web/` or `mobile/`).
3. Keep changes small and focused. One feature per commit.
4. No secrets in code or git. Use environment variables.
5. Every new feature gets tests (see Testing.md).
6. Update docs/Tasks.md when a task is done and docs/memory.md when a decision is made.

## Security and data

7. Tenant id comes from the auth token only. Every query on tenant data filters by tenant id.
8. Check plan, role and ownership in the API for every endpoint.
9. Never log patient names, phone numbers or results in plain logs.
10. Money is integer paise.
11. Soft delete for patients, bills, reports, visits.
12. Released reports are immutable. Corrections create a new version and an audit log entry.

## API (Spring Boot)

13. Layers: controller, service, repository. Business logic in services only.
14. Use DTOs; never return entities.
15. Validate all input. Return the standard error format from API.md.
16. Database changes through migrations (Flyway or Liquibase), never manual edits.
17. Paginate list endpoints.

## Web (React)

18. TypeScript, functional components, hooks.
19. API calls through one client module. Handle 401, 403 (plan/role) and network errors centrally.
20. Hide UI by `capabilities`, but never rely on it for security.
21. Strings in one place for later translation.

## Mobile (Flutter)

22. One state management approach across the app (PROPOSED: Riverpod).
23. API calls through one client layer, same error handling as web.
24. Large touch targets, clear loading and error states.
25. Never lose a collector's status update silently: show retry on failure.

## Reference ranges and reports

26. Ranges are chosen by age and gender. A range edit at report time applies to that report only.
27. Flags (low, high, critical) are computed by the server.
28. Any change to range, flag or report logic needs tests.

## Git

29. Branch per feature, short descriptive commit messages.
30. Do not commit build output, `.env` files or credentials.