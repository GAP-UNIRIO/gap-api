DELETE FROM users_roles user_role
USING roles role
WHERE user_role.role_id = role.id
  AND role.authority NOT IN ('ROLE_BASIC', 'ROLE_ADMIN');

DELETE FROM roles
WHERE authority NOT IN ('ROLE_BASIC', 'ROLE_ADMIN');
