ALTER TABLE approval_stages
    ADD COLUMN rejection_justification text;

CREATE TABLE users_org_units (
    user_id bigint NOT NULL,
    org_unit_id bigint NOT NULL,
    CONSTRAINT pk_users_org_units PRIMARY KEY (user_id, org_unit_id),
    CONSTRAINT fk_users_org_units_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_users_org_units_org_unit FOREIGN KEY (org_unit_id) REFERENCES org_units (id) ON DELETE CASCADE
);
