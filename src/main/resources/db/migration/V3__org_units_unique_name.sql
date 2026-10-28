ALTER TABLE org_units
    ADD CONSTRAINT uk_org_units_name UNIQUE (name);
