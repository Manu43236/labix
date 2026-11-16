# LABIX: Deployment

Version 0.1 (draft). Hosting on AWS. Keep it simple for v1; items marked PROPOSED can change.

## 1. Environments

| Env | Purpose |
|---|---|
| local | Developer machine (Docker for MySQL) |
| staging | Test with pilot-like data (fake data only) |
| production | Real labs |

## 2. AWS setup (PROPOSED)

- API: containerised, run on AWS App Runner or ECS (or a single EC2 to start).
- Database: Amazon RDS for MySQL, automated backups on.
- Files: S3 bucket per environment, private, access via signed URLs.
- Web: React build on S3 + CloudFront (or the same host as the API).
- Secrets: AWS Secrets Manager or SSM Parameter Store.
- DNS: thelabos.com or the final LABIX domain; HTTPS everywhere (ACM certificates).
- Region: Mumbai (ap-south-1) to keep data in India and latency low (PROPOSED).

## 3. Configuration

- All config via environment variables (database URL, JWT secret, S3 bucket, etc.).
- `.env.example` committed, real `.env` never committed.

## 4. CI/CD (PROPOSED)

- GitHub Actions: on push, run tests; on main, build and deploy to staging; manual approval to production.
- Database migrations run automatically on deploy (Flyway or Liquibase).

## 5. Mobile release

- Android first: Play Console account under the owning company. Internal testing track for pilot labs.
- iOS later: Apple Developer account under the owning company.
- Decide the owning company before creating these accounts.

## 6. Backups and recovery

- RDS automated daily backups, retained 30 days (PROPOSED).
- S3 versioning on the reports bucket.
- Test a restore before the first pilot.
- Tenant data export available on request.

## 7. Monitoring

- Basic logs in CloudWatch (no patient data in logs).
- Health endpoint and uptime alert.
- Error alerts to email or WhatsApp.

## 8. Security

- Database not publicly accessible.
- Least-privilege IAM roles.
- Regular dependency updates.
- Support access to tenant data is logged.

## 9. Cost note

Keep v1 small. Estimate monthly AWS cost before launch and compare it to revenue per lab (1200 to 2000 per month).

## 10. Go-live checklist

- [ ] Domain and HTTPS working
- [ ] Backups on and restore tested
- [ ] Privacy policy and terms published
- [ ] Super admin account secured
- [ ] Pilot labs onboarded and trained
- [ ] Support contact ready