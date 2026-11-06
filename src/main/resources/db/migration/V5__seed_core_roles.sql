INSERT INTO roles (authority) VALUES ('ROLE_BASIC'), ('ROLE_ADMIN')
ON CONFLICT (authority) DO NOTHING;

-- Usuários criados antes desta migration recebem o papel padrão.
INSERT INTO users_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
CROSS JOIN roles r
WHERE r.authority = 'ROLE_BASIC'
  AND NOT EXISTS (SELECT 1 FROM users_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
