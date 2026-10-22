ALTER TABLE courses
    ADD COLUMN department_id bigint;

ALTER TABLE courses
    ADD CONSTRAINT fk_courses_department
    FOREIGN KEY (department_id) REFERENCES org_units (id);

CREATE INDEX idx_courses_department_id ON courses (department_id);
