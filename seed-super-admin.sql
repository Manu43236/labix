-- Run once after the first `docker compose up` to create the super admin.
-- Password: labix123  (change after first login via direct DB update)
-- Usage: docker exec -i labix-mysql mysql -ulabix -plabix labix < seed-super-admin.sql

INSERT IGNORE INTO super_admins (id, name, email, password_hash, created_at, updated_at)
VALUES (
    UUID(),
    'LABIX Admin',
    'admin@labix.in',
    '$2b$10$Ua5EXliCa40uuPW35zU3dOpbDaGEqKbT2NJRsl7/qQXS0GwWbWJTK',
    NOW(6),
    NOW(6)
);
